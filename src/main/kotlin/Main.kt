import com.rabbitmq.client.*
import kotlinx.coroutines.*
import java.io.Closeable
import java.util.concurrent.ConcurrentLinkedDeque
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

suspend fun main() {
    val queueName = "test-queue"
    val start = System.currentTimeMillis()
    Cluster(3, queueName, 1, 3).use { cluster ->
        val job = GlobalScope.launch {
            delay(1000)

            connectRabbit("queue-await").use {
                while (true) {
                    if (cluster.consumedMessages() == cluster.publishedMessages()) {
                        println("All messages have been consumed in ${System.currentTimeMillis() - start} ms. ")
                        break
                    }

                    delay(500)
                    println("${System.currentTimeMillis() - start} ms: Average time to consume: ${cluster.averageTimeToConsumeNs().printNano()}, published ${cluster.publishedMessages()}, consumed ${cluster.consumedMessages()}")
                }
            }
        }

        delay(30 * 1000)
        cluster.stopPublishing()
        println("Stopped publishing")
        job.join()
    }
}

class ClusterNode(val nodeName: String, val stopPublishing: AtomicBoolean, val queueName: String = "test", val publishingThreads: Int, val consumerThreads: Int) : Closeable {
    private val consumersExecutor = Executors.newFixedThreadPool(consumerThreads, NamedThreadFactory("$nodeName-consumer", true))
    private val connection = connectRabbit("$nodeName-queue-consume", consumersExecutor)
    val timings = ConcurrentLinkedDeque<Long>()
    val publishedMessages = AtomicLong()

    init {
        // subscribe on the queue
        repeat(consumerThreads) {
            val channel = connection.createChannel()
            channel.createQueue(queueName)

            channel.basicConsume(queueName, true, object : DefaultConsumer(channel) {
                override fun handleDelivery(
                    consumerTag: String,
                    envelope: Envelope,
                    properties: AMQP.BasicProperties,
                    body: ByteArray
                ) {
                    val sendTime = Conversions.bytesToLong(body)
                    timings.add(System.nanoTime() - sendTime)
                }
            })
        }

        // publish to the queue
        GlobalScope.launch {
            val threadPool = Executors.newFixedThreadPool(publishingThreads, NamedThreadFactory("$nodeName-publisher", true))
            val publishingContext = threadPool.asCoroutineDispatcher()
            connectRabbit("$nodeName-queue-publish").use { connection ->
                (0 until publishingThreads).map {
                    GlobalScope.async(publishingContext) {
                        val channel = connection.createChannel()
                        channel.createQueue(queueName)

                        while (!stopPublishing.get()) {
                            channel.basicPublish("", queueName, null, Conversions.longToBytes(System.nanoTime()))
                            publishedMessages.incrementAndGet()
                        }
                    }
                }.awaitAll()
            }
            threadPool.shutdown()

            println("Node $nodeName finished publishing: $publishedMessages messages")
        }
    }

    override fun close() {
        connection.close()
        consumersExecutor.shutdown()
    }
}

class NamedThreadFactory private constructor(private val name: (Int) -> String, private val daemon: Boolean) : ThreadFactory {

    private val index = AtomicInteger()

    constructor(prefix: String, daemon: Boolean = false) : this({ "$prefix$it" }, daemon)

    override fun newThread(r: Runnable) = Thread(r, name(index.incrementAndGet())).apply { isDaemon = daemon }
}

class Cluster(nodesCount: Int, queueName: String, publishersPerNode: Int, consumersPerNode: Int) : Closeable {
    private val stopPublishing = AtomicBoolean(false)
    val nodes =  (0 until nodesCount).map { ClusterNode("node#$it", stopPublishing, queueName, publishersPerNode, consumersPerNode) }

    fun stopPublishing() {
        stopPublishing.set(true)
    }

    fun averageTimeToConsumeNs(): Long {
        return nodes.flatMap { it.timings }.average().toLong()
    }

    fun publishedMessages() = nodes.map { it.publishedMessages.get() }.sum()
    fun consumedMessages() = nodes.map { it.timings.size.toLong() }.sum()

    override fun close() {
        nodes.forEach { it.close() }
    }
}

fun connectRabbit(name: String, executorService: ExecutorService? = null): Connection {
    val factory = ConnectionFactory().apply { host = "localhost" }
    return factory.newConnection(executorService, name)
}

fun Channel.createQueue(name: String): AMQP.Queue.DeclareOk {
    return queueDeclare(name, false, false, false, null)
}

fun Long.printNano(): String {
    val millis = this / 1_000_000
    val rest = this - millis * 1_000_000
    return "$millis.${rest.toString().take(2)} ms"
}
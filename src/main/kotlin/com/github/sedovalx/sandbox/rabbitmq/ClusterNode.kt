package com.github.sedovalx.sandbox.rabbitmq

import com.rabbitmq.client.*
import kotlinx.coroutines.*
import mu.KLogging
import java.io.Closeable
import java.util.concurrent.ConcurrentLinkedDeque
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong

class ClusterNode(
    private val nodeName: String,
    private val stopPublishing: AtomicBoolean,
    private val queueName: String,
    private val queueAutoAck: Boolean,
    private val queuePersistMessages: Boolean,
    private val publishingThreads: Int,
    consumerThreads: Int
) : Closeable {
    companion object : KLogging()

    private val consumersExecutor = Executors.newFixedThreadPool(
        consumerThreads,
        NamedThreadFactory("$nodeName-consumer", true)
    )
    private val connection =
        connectRabbit("$nodeName-queue-consume", consumersExecutor)
    val timings = ConcurrentLinkedDeque<Long>()
    val publishedMessages = AtomicLong()

    init {
        // subscribe on the queue
        repeat(consumerThreads) {
            val channel = connection.createChannel()
            channel.createQueue(queueName)

            channel.basicConsume(queueName, queueAutoAck, object : DefaultConsumer(channel) {
                override fun handleDelivery(
                    consumerTag: String,
                    envelope: Envelope,
                    properties: AMQP.BasicProperties,
                    body: ByteArray
                ) {
                    val sendTime = Conversions.bytesToLong(body)
                    timings.add(System.nanoTime() - sendTime)

                    if (!queueAutoAck) {
                        channel.basicAck(envelope.deliveryTag, false)
                    }
                }
            })
        }

        // publish to the queue
        GlobalScope.launch {
            val threadPool = Executors.newFixedThreadPool(
                publishingThreads,
                NamedThreadFactory("$nodeName-publisher", true)
            )
            val publishingContext = threadPool.asCoroutineDispatcher()
            connectRabbit("$nodeName-queue-publish").use { connection ->
                (0 until publishingThreads).map {
                    GlobalScope.async(publishingContext) {
                        val channel = connection.createChannel()
                        channel.createQueue(queueName)

                        while (!stopPublishing.get()) {
                            val props = if (queuePersistMessages) AMQP.BasicProperties.Builder().deliveryMode(2).build() else null
                            channel.basicPublish("", queueName, props,
                                Conversions.longToBytes(System.nanoTime())
                            )
                            publishedMessages.incrementAndGet()
                        }
                    }
                }.awaitAll()
            }
            threadPool.shutdown()

            logger.debug { "Node $nodeName finished publishing: $publishedMessages messages" }
        }
    }

    override fun close() {
        connection.close()
        consumersExecutor.shutdown()
    }

    private fun Channel.createQueue(name: String): AMQP.Queue.DeclareOk {
        return queueDeclare(name, false, false, false, null)
    }
}

fun connectRabbit(name: String, executorService: ExecutorService? = null): Connection {
    val factory = ConnectionFactory().apply { host = "localhost" }
    return factory.newConnection(executorService, name)
}

fun Long.printNano(): String {
    val millis = this / 1_000_000
    val rest = this - millis * 1_000_000
    return "$millis.${rest.toString().take(2)} ms"
}
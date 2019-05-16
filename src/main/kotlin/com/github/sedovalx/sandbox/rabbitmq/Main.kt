package com.github.sedovalx.sandbox.rabbitmq

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import mu.KotlinLogging

const val queueName = "test-queue"

suspend fun main() {
    "Enabled persistence, manual ack".also {
        runTest(it, 3, 30, false, true, 3, 6)
        runTest(it, 3, 30, false, true, 1, 3)
        runTest(it, 1, 30, false, true, 5, 15)
        runTest(it, 1, 30, false, true, 3, 6)
        runTest(it, 1, 30, false, true, 1, 3)
    }

    "Disabled persistence, auto ack".also {
        runTest(it, 3, 30, true, false, 3, 6)
        runTest(it, 3, 30, true, false, 1, 3)
        runTest(it, 1, 30, true, false, 5, 15)
        runTest(it, 1, 30, true, false, 3, 6)
        runTest(it, 1, 30, true, false, 1, 3)
    }
}

private val logger = KotlinLogging.logger {  }

suspend fun runTest(
    desc: String,
    nodes: Int,
    publishSec: Int,
    autoAck: Boolean,
    persistence: Boolean,
    publishers: Int,
    consumers: Int
) {
    val start = System.currentTimeMillis()
    Cluster(
        nodesCount = nodes,
        queueName = queueName,
        queueAutoAck = autoAck,
        queuePersistMessages = persistence,
        publishersPerNode = publishers,
        consumersPerNode = consumers
    ).use { cluster ->
        val job = GlobalScope.launch {
            delay(1000)

            connectRabbit("queue-await").use {
                while (true) {
                    if (cluster.finished()) {
                        logger.debug { "All messages have been consumed in ${System.currentTimeMillis() - start} ms. " }
                        break
                    }

                    delay(500)
                    logger.debug { "${System.currentTimeMillis() - start} ms: Average time to consume: ${cluster.averageTimeToConsumeNs().printNano()}, published ${cluster.publishedMessages()}, consumed ${cluster.consumedMessages()}" }
                }
            }
        }

        delay(publishSec * 1000L)
        cluster.stopPublishing()
        logger.debug { "Stopped publishing" }
        job.join()

        logger.info { cluster.printResults(desc, publishSec * 1000L, System.currentTimeMillis() - start) }
    }
}

fun Cluster.printResults(desc: String, publishingTime: Long, consumingTime: Long): String {
    return buildString {
        appendln(desc)
        appendln("==============================================================================================")
        appendln("Nodes                             $nodesCount")
        appendln("Publishers/consumers per node     $publishersPerNode/$consumersPerNode")
        appendln("Auto ack                          $queueAutoAck")
        appendln("Messages persistence              $queuePersistMessages")
        appendln("Publishing time                   $publishingTime ms")
        appendln("Consuming time                    $consumingTime ms")
        appendln("Consumed messages                 ${consumedMessages()}")
        appendln("Time to consume, avg              ${averageTimeToConsumeNs().printNano()}")
        appendln("Time to consume, 90pct            ${percentile90TimeToConsumeNs().printNano()}")
        appendln()
    }
}


package com.github.sedovalx.sandbox.rabbitmq

import java.io.Closeable
import java.util.concurrent.atomic.AtomicBoolean

class Cluster(
    val nodesCount: Int,
    val queueName: String,
    val queueAutoAck: Boolean,
    val queuePersistMessages: Boolean,
    val publishersPerNode: Int,
    val consumersPerNode: Int
) : Closeable {
    private val stopPublishing = AtomicBoolean(false)
    private val nodes =  (0 until nodesCount).map {
        ClusterNode(
            "node#$it",
            stopPublishing,
            queueName,
            queueAutoAck,
            queuePersistMessages,
            publishersPerNode,
            consumersPerNode
        )
    }

    fun stopPublishing() {
        stopPublishing.set(true)
    }

    fun averageTimeToConsumeNs(): Long {
        return nodes.flatMap { it.timings }.average().toLong()
    }

    fun percentile90TimeToConsumeNs(): Long {
        return nodes.flatMap { it.timings }.percentile(90)
    }

    fun publishedMessages() = nodes.map { it.publishedMessages.get() }.sum()
    fun consumedMessages() = nodes.map { it.timings.size.toLong() }.sum()

    fun finished() = publishedMessages() == consumedMessages() && consumedMessages() > 0

    override fun close() {
        nodes.forEach { it.close() }
    }

    private fun List<Long>.percentile(p: Int): Long {
        return this.sorted()[(this.size - 1) * p / 100]
    }
}
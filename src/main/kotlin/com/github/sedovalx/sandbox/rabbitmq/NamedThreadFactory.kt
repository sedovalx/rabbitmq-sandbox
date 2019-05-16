package com.github.sedovalx.sandbox.rabbitmq

import kotlinx.coroutines.Runnable
import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicInteger

class NamedThreadFactory private constructor(private val name: (Int) -> String, private val daemon: Boolean) :
    ThreadFactory {

    private val index = AtomicInteger()

    constructor(prefix: String, daemon: Boolean = false) : this({ "$prefix$it" }, daemon)

    override fun newThread(r: Runnable) = Thread(r, name(index.incrementAndGet())).apply { isDaemon = daemon }
}
package io.vertex.autoconfigure.core

import io.vertx.core.Context
import io.vertx.core.Future
import io.vertx.core.Vertx
import io.vertx.kotlin.coroutines.CoroutineVerticle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.joinAll
import org.slf4j.LoggerFactory

/**
 * Created by xiongxl in 2023/6/30
 */
open class VertexVerticle(
    val index: Int,
    private val gracefulShutdown: GracefulShutdown?,
) : CoroutineVerticle() {
    companion object {
        const val VERTICLE_INDEX = "vertex.verticle.index"
        const val VERTICLE_ID = "vertex.verticle.id"
        const val VERTICLE = "vertex.verticle"
        fun index(): Int = Vertx.currentContext().get(VERTICLE_INDEX)
        fun indexOrNull(): Int? = Vertx.currentContext()?.get(VERTICLE_INDEX)
        fun id(): String = Vertx.currentContext().get(VERTICLE_ID)
        fun idOrNull(): String? = Vertx.currentContext()?.get(VERTICLE_ID)
        fun coroutineScope(ctx: Context? = null): CoroutineScope =
            if (ctx != null) {
                ctx.get(VERTICLE)
            } else {
                Vertx.currentContext().get(VERTICLE)
            }
        fun coroutineScopeOrNull(ctx: Context? = null): CoroutineScope? =
            if (ctx != null) {
                ctx.get(VERTICLE)
            } else {
                Vertx.currentContext()?.get(VERTICLE)
            }

        fun <T : VertexVerticle> current(): T = Vertx.currentContext().get(VERTICLE) as T
        fun <T : VertexVerticle> currentOrNull(): T? = Vertx.currentContext()?.get(VERTICLE) as? T

        fun <T> put(key: String, value: T) {
            Vertx.currentContext().put(key, value)
        }

        fun <T> get(key: String): T {
            return Vertx.currentContext().get(key)
        }

        fun <T> getOrNull(key: String): T? {
            return Vertx.currentContext()?.get(key)
        }

        fun <T> getOrCreate(key: String, factory: () -> T): T {
            val ctx = Vertx.currentContext()!!
            val value = ctx.get<T>(key)
            if (value != null) return value
            val newValue = factory()
            ctx.put(key, newValue)
            return newValue
        }

        suspend fun <T> getOrCreateAwait(key: String, awaitFactory: suspend () -> T): T {
            val ctx = Vertx.currentContext()!!
            val value = ctx.get<T>(key)
            if (value != null) return value
            val newValue = awaitFactory()
            ctx.put(key, newValue)
            return newValue
        }

        fun <T> getOrCreateAsync(key: String, asyncFactory: () -> Future<T>): Future<T> {
            val ctx = Vertx.currentContext()!!
            val value = ctx.get<T>(key)
            if (value != null) return Future.future { it.complete(value) }
            return asyncFactory().andThen {
                if (it.succeeded()) {
                    ctx.put(key, it.result())
                }
            }
        }

        fun addCloser(closer: suspend () -> Unit) {
            current<VertexVerticle>().closers.add(closer)
        }

        fun addOrderedCloser(closer: suspend () -> Unit) {
            current<VertexVerticle>().orderedClosers.add(closer)
        }
    }
    private val logger = LoggerFactory.getLogger(this::class.java)
    val id by lazy(LazyThreadSafetyMode.NONE) { "${this::class.simpleName}[$index]" }
    private val closers = mutableListOf<suspend () -> Unit>()
    private val orderedClosers = mutableListOf<suspend () -> Unit>()

    override suspend fun start() {
        val ctx = Vertx.currentContext()
        ctx.put(VERTICLE_INDEX, index)
        ctx.put(VERTICLE_ID, id)
        ctx.put(VERTICLE, this)
    }

    override suspend fun stop() {
        gracefulShutdown?.awaitCompletion(this)
        logger.info("Stopping $id, closers: ${closers.size}, orderedClosers: ${orderedClosers.size}")
        val closeResults = closers.map { async { it() } }
        orderedClosers.reversed().forEach {
            try {
                it()
            } catch (e: Throwable) {
                logger.warn("Ordered closer error", e)
            }
        }
        if (closeResults.isNotEmpty()) {
            try {
                closeResults.joinAll()
            } catch (e: Throwable) {
                logger.warn("Closers error", e)
            }
        }
    }
}
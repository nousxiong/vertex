package io.vertex.autoconfigure.core

import io.vertx.core.Context
import io.vertx.core.Future
import io.vertx.core.Vertx

/**
 * Created by xiongxl in 2023/6/30 适用于Verticle的全局生命周期类型对象，每个vert.x的verticle一份
 */
class VerticleLifecycle<T>(private val name: String) {
    private var factory: (() -> T)? = null
    private var awaitFactory: (suspend () -> T)? = null
    private var asyncFactory: (() -> Future<T>)? = null
    private var defaulter: (() -> T)? = null
    private var awaitDefaulter: (suspend () -> T)? = null
    private var asyncDefaulter: (() -> Future<T>)? = null
    private var closer: (suspend (T) -> Unit)? = null
    private var orderedCloser: (suspend (T) -> Unit)? = null

    fun factory(factory: () -> T): VerticleLifecycle<T> {
        this.factory = factory
        return this
    }

    fun awaitFactory(awaitFactory: suspend () -> T): VerticleLifecycle<T> {
        this.awaitFactory = awaitFactory
        return this
    }

    fun asyncFactory(asyncFactory: () -> Future<T>): VerticleLifecycle<T> {
        this.asyncFactory = asyncFactory
        return this
    }

    fun defaulter(defaulter: () -> T): VerticleLifecycle<T> {
        this.defaulter = defaulter
        return this
    }

    fun awaitDefaulter(awaitDefaulter: suspend () -> T): VerticleLifecycle<T> {
        this.awaitDefaulter = awaitDefaulter
        return this
    }

    fun asyncDefaulter(asyncDefaulter: () -> Future<T>): VerticleLifecycle<T> {
        this.asyncDefaulter = asyncDefaulter
        return this
    }

    fun closer(closer: suspend (T) -> Unit): VerticleLifecycle<T> {
        this.closer = closer
        return this
    }

    fun orderedCloser(orderedCloser: suspend (T) -> Unit): VerticleLifecycle<T> {
        this.orderedCloser = orderedCloser
        return this
    }

    fun get(): T {
        return get(Vertx.currentContext())
    }

    /**
     * for java，param can't using default value
     */
    fun get(ctx: Context): T {
        val factory = factory
        check(factory != null) { "$name's factory is null" }
        return VertexVerticle.getOrCreate(name, ctx) {
            val result = factory()
            addClosers(result)
            result
        }
    }

    fun getOrDefault(): T {
        val defaulter = defaulter
        check(defaulter != null) { "$name's defaulter is null" }
        val ctx = Vertx.currentContext() ?: return defaulter()
        return get(ctx)
    }

    suspend fun getAwait(ctx: Context = Vertx.currentContext()): T {
        val awaitFactory = awaitFactory
        check(awaitFactory != null) { "$name's awaitFactory is null" }
        return VertexVerticle.getOrCreateAwait(name, ctx) {
            val result = awaitFactory()
            addClosers(result)
            result
        }
    }

    suspend fun getOrDefaultAwait(): T {
        val awaitDefaulter = awaitDefaulter
        check(awaitDefaulter != null) { "$name's awaitDefaulter is null" }
        val ctx = Vertx.currentContext() ?: return awaitDefaulter()
        return getAwait(ctx)
    }

    fun getAsync(): Future<T> {
        return getAsync(Vertx.currentContext())
    }

    /**
     * for java，param can't using default value
     */
    fun getAsync(ctx: Context): Future<T> {
        val asyncFactory = asyncFactory
        check(asyncFactory != null) { "$name's asyncFactory is null" }
        return VertexVerticle.getOrCreateAsync(name, ctx) {
            asyncFactory().andThen {
                if (it.succeeded()) {
                    addClosers(it.result())
                }
            }
        }
    }

    fun getOrDefaultAsync(): Future<T> {
        val asyncDefaulter = asyncDefaulter
        check(asyncDefaulter != null) { "$name's asyncDefaulter is null" }
        val ctx = Vertx.currentContext() ?: return asyncDefaulter()
        return getAsync(ctx)
    }

    private fun addClosers(value: T) {
        val closer = closer
        if (closer != null) {
            VertexVerticle.addCloser {
                closer(value)
            }
        }
        val orderedCloser = orderedCloser
        if (orderedCloser != null) {
            VertexVerticle.addOrderedCloser {
                orderedCloser(value)
            }
        }
    }
}
package io.vertex.autoconfigure.core

import io.vertx.core.Future

/**
 * Created by xiongxl in 2023/6/30 适用于Verticle的全局生命周期类型对象，每个vert.x的verticle一份
 * TODO add vert.x mutiny、reactor(mono、flux)、flow support
 */
class VerticleLifecycle<T>(private val name: String) {
    private var factory: (() -> T)? = null
    private var awaitFactory: (suspend () -> T)? = null
    private var asyncFactory: (() -> Future<T>)? = null
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

    fun closer(closer: suspend (T) -> Unit): VerticleLifecycle<T> {
        this.closer = closer
        return this
    }

    fun orderedCloser(orderedCloser: suspend (T) -> Unit): VerticleLifecycle<T> {
        this.orderedCloser = orderedCloser
        return this
    }

    fun get(): T {
        val factory = factory
        check(factory != null) { "$name's factory is null" }
        return VertexVerticle.getOrCreate(name) {
            val result = factory()
            addClosers(result)
            result
        }
    }

    suspend fun getAwait(): T {
        val awaitFactory = awaitFactory
        check(awaitFactory != null) { "$name's awaitFactory is null" }
        return VertexVerticle.getOrCreateAwait(name) {
            val result = awaitFactory()
            addClosers(result)
            result
        }
    }

    fun getAsync(): Future<T> {
        val asyncFactory = asyncFactory
        check(asyncFactory != null) { "$name's asyncFactory is null" }
        return VertexVerticle.getOrCreateAsync(name) {
            asyncFactory().andThen {
                if (it.succeeded()) {
                    addClosers(it.result())
                }
            }
        }
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
package io.vertex.autoconfigure.web.server

import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.http.HttpServerOptions
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.await
import kotlinx.coroutines.CoroutineScope

/**
 * Created by xiongxl in 2023/6/7
 */
open class VertexServerVerticle(
    val index: Int,
    private val httpServerOptions: HttpServerOptions,
    private val requestHandler: Handler<RoutingContext>,
    private val gracefulShutdown: GracefulShutdown?,
) : CoroutineVerticle() {
    companion object {
        const val VERTICLE_INDEX = "vertex.verticle.index"
        const val VERTICLE_ID = "vertex.verticle.id"
        const val VERTICLE_COROUTINE_SCOPE = "vertex.verticle.coroutine.scope"
        fun index(): Int = Vertx.currentContext().get(VERTICLE_INDEX)
        fun indexOrNull(): Int? = Vertx.currentContext()?.get(VERTICLE_INDEX)
        fun id(): String = Vertx.currentContext().get(VERTICLE_ID)
        fun idOrNull(): String? = Vertx.currentContext()?.get(VERTICLE_ID)
        fun coroutineScope(): CoroutineScope = Vertx.currentContext().get(VERTICLE_COROUTINE_SCOPE)
        fun coroutineScopeOrNull(): CoroutineScope? = Vertx.currentContext()?.get(VERTICLE_COROUTINE_SCOPE)
    }
    val id by lazy { "${this::class.simpleName}[$index]" }
    var port = 0
        private set

    override suspend fun start() {
        val router = Router.router(vertx)
        router.route().handler(requestHandler)

        val server = vertx.createHttpServer(httpServerOptions).requestHandler(router).listen().await()
        port = server.actualPort()
        val ctx = Vertx.currentContext()
        ctx.put(VERTICLE_INDEX, index)
        ctx.put(VERTICLE_ID, id)
        ctx.put(VERTICLE_COROUTINE_SCOPE, this)
    }

    override suspend fun stop() {
        gracefulShutdown?.awaitCompletion(this)
    }
}
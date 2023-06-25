package io.vertex.autoconfigure.web.server

import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.http.HttpServerOptions
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.await

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
//        private val indexer = AtomicInteger(0)
        const val VERTICLE_INDEX = "vertex.verticle.index"
        const val VERTICLE_ID = "vertex.verticle.id"
        fun getIndex(): Int = Vertx.currentContext().get(VERTICLE_INDEX)
        fun getIndexOrNull(): Int? = Vertx.currentContext()?.get(VERTICLE_INDEX)
        fun getId(): String = Vertx.currentContext().get(VERTICLE_ID)
        fun getIdOrNull(): String? = Vertx.currentContext()?.get(VERTICLE_ID)
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
    }

    override suspend fun stop() {
        gracefulShutdown?.awaitCompletion(this)
    }
}
package io.vertex.autoconfigure.web.server

import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.http.HttpServerOptions
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.await
import java.util.concurrent.atomic.AtomicInteger

/**
 * Created by xiongxl in 2023/6/7
 */
open class VertexServerVerticle(
    private val httpServerOptions: HttpServerOptions,
    private val requestHandler: Handler<RoutingContext>,
    private val gracefulShutdown: GracefulShutdown?,
) : CoroutineVerticle() {
    companion object {
        private val idr = AtomicInteger(0)
        const val CONTEXT_ID = "vertex.contextId"
    }
    val id = idr.getAndIncrement()
    var port = 0
        private set

    override suspend fun start() {
        val router = Router.router(vertx)
        router.route().handler(requestHandler)

        val server = vertx.createHttpServer(httpServerOptions).requestHandler(router).listen().await()
        port = server.actualPort()
        val ctx = Vertx.currentContext()
        ctx.put(CONTEXT_ID, id)
    }

    override suspend fun stop() {
        gracefulShutdown?.awaitCompletion(this)
    }
}
package io.vertex.autoconfigure.web.server

import io.vertex.autoconfigure.core.GracefulShutdown
import io.vertex.autoconfigure.core.VertexVerticle
import io.vertx.core.Handler
import io.vertx.core.http.HttpServerOptions
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.coroutines.await

/**
 * Created by xiongxl in 2023/6/7
 */
open class VertexServerVerticle(
    index: Int,
    private val httpServerOptions: HttpServerOptions,
    private val requestHandler: Handler<RoutingContext>,
    gracefulShutdown: GracefulShutdown?,
) : VertexVerticle(index, gracefulShutdown) {
    var port = 0
        private set

    override suspend fun start() {
        super.start()
        val router = Router.router(vertx)
        router.route().handler(requestHandler)

        val server = vertx.createHttpServer(httpServerOptions).requestHandler(router).listen().await()
        port = server.actualPort()
    }

    override suspend fun stop() {
        super.stop()
    }
}
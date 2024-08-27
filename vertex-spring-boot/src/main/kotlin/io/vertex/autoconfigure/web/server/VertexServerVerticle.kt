package io.vertex.autoconfigure.web.server

import io.vertex.autoconfigure.core.GracefulShutdown
import io.vertex.autoconfigure.core.VertexVerticle
import io.vertx.core.Handler
import io.vertx.core.http.HttpServerOptions
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.coroutines.coAwait
import org.springframework.http.HttpStatus

/**
 * Created by xiongxl in 2023/6/7
 */
open class VertexServerVerticle(
    instances: Int,
    index: Int,
    private val httpServerOptions: HttpServerOptions,
    private val requestHandler: Handler<RoutingContext>,
    gracefulShutdown: GracefulShutdown?,
) : VertexVerticle(instances, index, gracefulShutdown) {
    var port = 0
        private set

    override suspend fun start() {
        super.start()
        val router = Router.router(vertx)
        router.route().handler(requestHandler).failureHandler {
            val srverr = HttpStatus.INTERNAL_SERVER_ERROR
            val errmsg = it.failure()?.stackTraceToString() ?: srverr.reasonPhrase
            if (logger.isInfoEnabled) {
                logger.info("handle request failed: $errmsg")
            }
            it.response().setStatusCode(srverr.value()).end(errmsg)
        }

        val server = vertx.createHttpServer(httpServerOptions).requestHandler(router).listen().coAwait()
        port = server.actualPort()
    }

    override suspend fun stop() {
        super.stop()
    }
}
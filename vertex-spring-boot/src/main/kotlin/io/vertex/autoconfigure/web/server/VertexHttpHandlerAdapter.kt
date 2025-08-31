package io.vertex.autoconfigure.web.server

import io.vertex.util.BufferConverter
import io.vertx.core.Handler
import io.vertx.ext.web.RoutingContext
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.server.reactive.HttpHandler

/**
 * Created by xiongxl in 2023/6/15
 */
class VertexHttpHandlerAdapter(private val httpHandler: HttpHandler) : Handler<RoutingContext> {
    companion object {
        private val logger = LoggerFactory.getLogger(VertexHttpHandlerAdapter::class.java)
    }
    private val bufferConverter = BufferConverter()

    override fun handle(context: RoutingContext) {
        logger.debug("Adapting Vert.x server request to WebFlux request")

        val webFluxRequest = VertexServerHttpRequest(context, bufferConverter)
        val webFluxResponse = VertexServerHttpResponse(context, bufferConverter)

        httpHandler.handle(webFluxRequest, webFluxResponse)
            .doOnSuccess {
                logger.debug("Completed server request handling")
                if (!context.response().ended()) {
                    context.response()
                        .end()
                }
            }
            .doOnError { throwable: Throwable ->
                logger.debug("Completed server request handling with an error '${throwable}'")
                context.response()
                    .setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .end()
            }
            .doOnTerminate {
                logger.debug("Finished server request handling")
            }
            .subscribe()
    }
}
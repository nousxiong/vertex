package io.vertex.autoconfigure.web.server

import io.vertex.autoconfigure.common.VertexWebSocketSession
import io.vertex.util.BufferConverter
import io.vertx.core.http.HttpServerRequest
import io.vertx.core.http.ServerWebSocket
import org.slf4j.LoggerFactory
import org.springframework.http.server.reactive.AbstractServerHttpRequest
import org.springframework.web.reactive.socket.HandshakeInfo
import org.springframework.web.reactive.socket.WebSocketHandler
import org.springframework.web.reactive.socket.server.RequestUpgradeStrategy
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import java.util.function.Supplier

/**
 * Created by xiongxl in 2023/6/16
 */
class VertexRequestUpgradeStrategy(
    private val maxWebSocketFrameSize: Int,
    private val maxWebSocketMessageSize: Int
) : RequestUpgradeStrategy {
    private val bufferConverter = BufferConverter()
    companion object {
        private val logger = LoggerFactory.getLogger(VertexRequestUpgradeStrategy::class.java)
    }
    override fun upgrade(
        exchange: ServerWebExchange,
        webSocketHandler: WebSocketHandler,
        subProtocol: String?,
        handshakeInfoFactory: Supplier<HandshakeInfo>
    ): Mono<Void> {
        logger.debug("Upgrading request to web socket")

        val request = exchange.request
        val response = exchange.response
        val vertxRequest = (request as AbstractServerHttpRequest).getNativeRequest<HttpServerRequest>()
        val handshakeInfo = handshakeInfoFactory.get()
        val uri = exchange.request.uri

        return Mono.fromCompletionStage(vertxRequest.toWebSocket().toCompletionStage())
            .flatMap { ws: ServerWebSocket ->
                val session =
                    VertexWebSocketSession(
                        ws,
                        handshakeInfo,
                        bufferConverter,
                        maxWebSocketFrameSize,
                        maxWebSocketMessageSize
                    )
                webSocketHandler.handle(session).checkpoint("$uri [VertexRequestUpgradeStrategy]")
            }.then(response.setComplete())
    }
}
package io.vertex.autoconfigure.common

import io.vertex.utils.BufferConverter
import io.vertx.core.buffer.Buffer
import io.vertx.core.http.WebSocketBase
import org.reactivestreams.Publisher
import org.reactivestreams.Subscriber
import org.slf4j.LoggerFactory
import org.springframework.util.ObjectUtils
import org.springframework.web.reactive.socket.CloseStatus
import org.springframework.web.reactive.socket.HandshakeInfo
import org.springframework.web.reactive.socket.WebSocketMessage
import org.springframework.web.reactive.socket.adapter.AbstractWebSocketSession
import reactor.core.publisher.Flux
import reactor.core.publisher.FluxSink
import reactor.core.publisher.Mono
import reactor.core.publisher.MonoSink

/**
 * Created by xiongxl in 2023/6/16
 */
class VertexWebSocketSession(
    delegate: WebSocketBase,
    handshakeInfo: HandshakeInfo,
    private val bufferConverter: BufferConverter,
    maxWebSocketFrameSize: Int,
    maxWebSocketMessageSize: Int,
) : AbstractWebSocketSession<WebSocketBase>(
    delegate,
    ObjectUtils.getIdentityHexString(delegate),
    handshakeInfo,
    bufferConverter.dataBufferFactory
) {
    companion object {
        private val logger = LoggerFactory.getLogger(VertexWebSocketSession::class.java)
    }
    init {
        require(!(maxWebSocketMessageSize < 1 || maxWebSocketFrameSize < 1)) {
            "Max web socket frame and message sizes cannot be less than 1"
        }
    }
    private val requestLimit: Long = maxWebSocketMessageSize / maxWebSocketFrameSize + 1L
    override fun receive(): Flux<WebSocketMessage> {
        return Flux.create { sink: FluxSink<WebSocketMessage> ->
            logger.debug("${logPrefix}Connecting to a web socket read stream")
            val socket = delegate
            socket.pause()
                .textMessageHandler { payload: String ->
                    logger.debug("${logPrefix}Received text '${payload}' from a web socket read stream")
                    sink.next(textMessage(payload))
                }
                .binaryMessageHandler { payload: Buffer ->
                    logger.debug("${logPrefix}Received binary '${payload}' from a web socket read stream")
                    sink.next(binaryMessage(payload))
                }
                .pongHandler { payload: Buffer ->
                    logger.debug("${logPrefix}Received pong '${payload}' from a web socket read stream")
                    sink.next(pongMessage(payload))
                }
                .exceptionHandler { throwable: Throwable? ->
                    logger.debug("${logPrefix}Received exception '${throwable}' from a web socket read stream")
                    sink.error(throwable!!)
                }
                .endHandler {
                    logger.debug("${logPrefix}Web socket read stream ended")
                    sink.complete()
                }
            sink.onRequest { i: Long ->
                logger.debug("${logPrefix}Fetching '${i}' entries from a web socket read stream")
                socket.fetch(i)
            }
        }
    }

    override fun send(messages: Publisher<WebSocketMessage>): Mono<Void> {
        return Mono.create { sink: MonoSink<Void> ->
            logger.debug("${logPrefix}Subscribing to messages publisher")
            val subscriber: Subscriber<WebSocketMessage> =
                WriteStreamSubscriber<WebSocketBase, WebSocketMessage>(
                    delegate,
                    this::messageHandler,
                    sink,
                    requestLimit
                )
            messages.subscribe(subscriber)
        }
    }

    override fun isOpen(): Boolean {
        return !delegate.isClosed
    }

    override fun close(status: CloseStatus): Mono<Void> {
        logger.debug("${logPrefix}Closing web socket with status '${status}'")
        return Mono.create { sink: MonoSink<Void> ->
            delegate
                .closeHandler {
                    logger.debug("${logPrefix}Web socket closed")
                    sink.success()
                }
                .close(status.code.toShort(), status.reason)
        }
    }

    override fun closeStatus(): Mono<CloseStatus> {
        val code = delegate.closeStatusCode() ?: return Mono.empty()
        val reason = delegate.closeReason() ?: return Mono.just(CloseStatus(code.toInt()))
        return Mono.just(CloseStatus(code.toInt(), reason))
    }

    private fun messageHandler(socket: WebSocketBase, message: WebSocketMessage) {
        if (message.type == WebSocketMessage.Type.TEXT) {
            val payload = message.payloadAsText
            socket.writeTextMessage(payload)
        } else {
            val buffer = bufferConverter.toBuffer(message.payload)
            when (message.type) {
                WebSocketMessage.Type.PING -> {
                    socket.writePing(buffer)
                }
                WebSocketMessage.Type.PONG -> {
                    socket.writePong(buffer)
                }
                else -> {
                    socket.writeBinaryMessage(buffer)
                }
            }
        }
    }

    private fun binaryMessage(payloadBuffer: Buffer): WebSocketMessage {
        val payload = bufferConverter.toDataBuffer(payloadBuffer)
        return WebSocketMessage(WebSocketMessage.Type.BINARY, payload)
    }

    private fun pongMessage(payloadBuffer: Buffer): WebSocketMessage {
        val payload = bufferConverter.toDataBuffer(payloadBuffer)
        return WebSocketMessage(WebSocketMessage.Type.PONG, payload)
    }
}
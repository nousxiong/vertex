package io.vertex.autoconfigure.web.client

import io.vertex.autoconfigure.common.VertexWebSocketSession
import io.vertex.util.BufferConverter
import io.vertx.core.AsyncResult
import io.vertx.core.Vertx
import io.vertx.core.http.HttpClientOptions
import io.vertx.core.http.WebSocket
import io.vertx.core.http.WebSocketConnectOptions
import io.vertx.core.http.impl.headers.HeadersMultiMap
import org.springframework.http.HttpHeaders
import org.springframework.web.reactive.socket.HandshakeInfo
import org.springframework.web.reactive.socket.WebSocketHandler
import org.springframework.web.reactive.socket.client.WebSocketClient
import reactor.core.publisher.Mono
import reactor.core.publisher.MonoSink
import java.net.URI

/**
 * Created by xiongxl in 2023/6/21
 */
class VertexWebSocketClient(
    private val vertx: Vertx,
    private val clientOptions: HttpClientOptions = HttpClientOptions(),
) : WebSocketClient {
    private val bufferConverter = BufferConverter()
    override fun execute(url: URI, handler: WebSocketHandler): Mono<Void> {
        return execute(url, HttpHeaders(), handler)
    }

    override fun execute(url: URI, headers: HttpHeaders, handler: WebSocketHandler): Mono<Void> {
        val vertxHeaders = convertHeaders(headers)

        return Mono.create { sink: MonoSink<Void> ->
            connect(
                url,
                vertxHeaders,
                handler,
                sink
            )
        }
    }

    private fun connect(uri: URI, headers: HeadersMultiMap, handler: WebSocketHandler, callback: MonoSink<Void>) {
        val client = vertx.createHttpClient(clientOptions)
        val options = WebSocketConnectOptions()
            .setPort(uri.port)
            .setHost(uri.host)
            .setURI(uri.path)
            .setHeaders(headers)
        client.webSocket(
            options
        ) { result: AsyncResult<WebSocket> ->
            if (result.failed()) {
                callback.error(result.cause())
            } else {
                handler.handle(initSession(uri, result.result()))
                    .doOnSuccess { value: Void? ->
                        callback.success(
                            value
                        )
                    }
                    .doOnError { e: Throwable -> callback.error(e) }
                    .doFinally { client.close() }
                    .subscribe()
            }
        }
    }

    private fun convertHeaders(headers: HttpHeaders): HeadersMultiMap {
        val vertxHeaders = HeadersMultiMap()
        headers.forEach { name: String, values: List<String> ->
            vertxHeaders.add(
                name,
                values
            )
        }
        return vertxHeaders
    }

    private fun initSession(uri: URI, socket: WebSocket): VertexWebSocketSession {
        // Vert.x handshake doesn't return headers so passing an empty collection
        val handshakeInfo = HandshakeInfo(uri, HttpHeaders(), Mono.empty(), socket.subProtocol())
        return VertexWebSocketSession(
            socket, handshakeInfo, bufferConverter,
            clientOptions.maxWebSocketFrameSize, clientOptions.maxWebSocketMessageSize
        )
    }
}
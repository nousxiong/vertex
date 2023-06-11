package io.vertex.autoconfigure.web.server

import io.vertex.utils.BufferConverter
import io.vertex.utils.WriteStreamSubscriber
import io.vertx.core.AsyncResult
import io.vertx.core.http.HttpServerResponse
import io.vertx.ext.web.RoutingContext
import org.reactivestreams.Publisher
import org.reactivestreams.Subscriber
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.http.HttpHeaders
import org.springframework.http.ZeroCopyHttpOutputMessage
import org.springframework.http.server.reactive.AbstractServerHttpResponse
import reactor.core.publisher.Mono
import reactor.core.publisher.MonoSink
import java.nio.file.Path
import java.util.function.Consumer

/**
 * Created by xiongxl in 2023/6/11
 */
class VertexServerHttpResponse(
    private val context: RoutingContext,
    private val bufferConverter: BufferConverter,
) : AbstractServerHttpResponse(
    bufferConverter.dataBufferFactory,
    initHeaders(context.response())),
    ZeroCopyHttpOutputMessage {
    private val delegate: HttpServerResponse = context.response()

    override fun writeWith(file: Path, position: Long, count: Long): Mono<Void> {
        val writeCompletion = Mono.create<Void?> { sink: MonoSink<Void?> ->
            logger.debug("Sending file '${file}' pos='${position}' count='${count}'")
            delegate.sendFile(
                file.toString(), position, count
            ) { result: AsyncResult<Void?> ->
                if (result.succeeded()) {
                    sink.success()
                } else {
                    sink.error(result.cause())
                }
            }
        }

        return doCommit { writeCompletion }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> getNativeResponse(): T {
        return delegate as T
    }

    override fun writeWithInternal(body: Publisher<out DataBuffer>): Mono<Void> {
        return Mono.create { sink: MonoSink<Void> ->
            logger.debug("Subscribing to body publisher")
            val subscriber: Subscriber<DataBuffer> =
                WriteStreamSubscriber<HttpServerResponse, DataBuffer>(
                    delegate,
                    { stream, value -> stream.write(bufferConverter.toBuffer(value)) },
                    sink,
        1L,
                )
            body.subscribe(subscriber)
        }
    }

    override fun writeAndFlushWithInternal(body: Publisher<out Publisher<out DataBuffer>>): Mono<Void> {
        TODO("Not yet implemented")
    }

    override fun applyStatusCode() {
        TODO("Not yet implemented")
    }

    override fun applyHeaders() {
        TODO("Not yet implemented")
    }

    override fun applyCookies() {
        TODO("Not yet implemented")
    }

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(VertexServerHttpResponse::class.java)
        private fun initHeaders(response: HttpServerResponse): HttpHeaders {
            val headers = HttpHeaders()
            response.headers()
                .forEach(Consumer { (key, value): Map.Entry<String, String?> ->
                    headers.add(key, value)
                })
            return headers
        }
    }

}
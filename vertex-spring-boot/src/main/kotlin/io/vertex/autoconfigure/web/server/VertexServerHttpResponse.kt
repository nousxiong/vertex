package io.vertex.autoconfigure.web.server

import io.vertex.utils.BufferConverter
import io.vertex.utils.CookieConverter
import io.vertex.autoconfigure.common.WriteStreamSubscriber
import io.vertx.core.AsyncResult
import io.vertx.core.http.Cookie
import io.vertx.core.http.HttpServerResponse
import io.vertx.ext.web.RoutingContext
import org.reactivestreams.Publisher
import org.reactivestreams.Subscriber
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatusCode
import org.springframework.http.ResponseCookie
import org.springframework.http.ZeroCopyHttpOutputMessage
import org.springframework.http.server.reactive.AbstractServerHttpResponse
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.MonoSink
import java.nio.file.Path
import java.util.function.Consumer
import java.util.function.Function

/**
 * Created by xiongxl in 2023/6/11
 */
class VertexServerHttpResponse(
    context: RoutingContext,
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
        return writeWithInternal(Flux.from(body).flatMap(Function.identity()))
    }

    override fun applyStatusCode() {
        if (delegate.headWritten()) return
        val statusCode: HttpStatusCode? = statusCode
        if (statusCode != null) {
            delegate.setStatusCode(statusCode.value())
        }
    }

    override fun applyHeaders() {
        if (delegate.headWritten()) return // 当websocket情况时，握手完成后就会设置headWritten=true，所以这里要判断
        val headers = headers
        if (!headers.containsKey(HttpHeaders.CONTENT_LENGTH)) {
            logger.debug("Setting chunked response")
            delegate.setChunked(true)
        }
        headers.forEach { name: String, values: List<String> ->
            delegate.putHeader(
                name,
                values
            )
        }
    }

    override fun applyCookies() {
        if (delegate.headWritten()) return
        cookies
            .values
            .stream()
            .flatMap { obj: List<ResponseCookie> -> obj.stream() }
            .map(CookieConverter::toCookie)
            .forEach { cookie: Cookie -> delegate.addCookie(cookie) }
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
package io.vertex.autoconfigure.web.client

import io.vertex.autoconfigure.common.WriteStreamSubscriber
import io.vertex.util.BufferConverter
import io.vertx.core.http.HttpClientRequest
import org.reactivestreams.Publisher
import org.reactivestreams.Subscriber
import org.slf4j.LoggerFactory
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.core.io.buffer.DataBufferFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.client.reactive.AbstractClientHttpRequest
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.MonoSink
import java.net.URI
import java.util.function.Function

/**
 * Created by xiongxl in 2023/6/20
 */
class VertexClientHttpRequest(
    private val delegate: HttpClientRequest,
    private val bufferConverter: BufferConverter,
) : AbstractClientHttpRequest() {
    companion object {
        private val logger = LoggerFactory.getLogger(VertexClientHttpRequest::class.java)
    }
    override fun bufferFactory(): DataBufferFactory {
        return bufferConverter.dataBufferFactory
    }

    override fun writeWith(body: Publisher<out DataBuffer>): Mono<Void> {
        val writeCompletion = Mono.create { sink: MonoSink<Void> ->
            logger.debug("Subscribing to body publisher")
            val subscriber: Subscriber<DataBuffer> =
                WriteStreamSubscriber<HttpClientRequest, DataBuffer>(
                    delegate,
                    { stream, value -> stream.write(bufferConverter.toBuffer(value)) },
                    sink,
                    1L
                )
            body.subscribe(subscriber)
        }
        val endCompletion = Mono.create { sink: MonoSink<Void> ->
            logger.debug("Completing request after writing")
            delegate.end()
            sink.success()
        }
        return doCommit { writeCompletion.then(endCompletion) }
    }

    override fun writeAndFlushWith(body: Publisher<out Publisher<out DataBuffer>>): Mono<Void> {
        return writeWith(Flux.from(body).flatMap(Function.identity()))
    }

    override fun setComplete(): Mono<Void> {
        return doCommit {
            Mono.create { sink: MonoSink<Void> ->
                logger.debug("Completing empty request")
                delegate.end()
                sink.success()
            }
        }
    }

    override fun getMethod(): HttpMethod {
        return HttpMethod.valueOf(delegate.method.name())
    }

    override fun getURI(): URI {
        return URI.create(delegate.absoluteURI())
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> getNativeRequest(): T {
        return delegate as T
    }

    override fun applyHeaders() {
        val headers = headers
        if (!headers.containsKey(HttpHeaders.CONTENT_LENGTH)) {
            logger.debug("Setting chunked request")
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
        cookies
            .values
            .stream()
            .flatMap { it.stream() }
            .map { it.toString() }
            .forEach { delegate.putHeader("Cookie", it) }
    }
}
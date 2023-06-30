package io.vertex.autoconfigure.web.server

import io.vertex.util.BufferConverter
import io.vertex.util.CookieConverter
import io.vertex.autoconfigure.common.ReadStreamFluxBuilder
import io.vertx.core.buffer.Buffer
import io.vertx.core.http.HttpServerRequest
import io.vertx.ext.web.RoutingContext
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.http.HttpCookie
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.server.reactive.AbstractServerHttpRequest
import org.springframework.http.server.reactive.SslInfo
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import reactor.core.publisher.Flux
import java.net.InetSocketAddress
import java.net.URI
import java.util.function.Consumer

/**
 * Created by xiongxl in 2023/6/9
 */
class VertexServerHttpRequest(
    context: RoutingContext,
    bufferConverter: BufferConverter,
) : AbstractServerHttpRequest(
    HttpMethod.valueOf(context.request().method().name()),
    initUri(context.request()),
    "",
    initHeaders(context.request())
) {
    private val delegate: HttpServerRequest = context.request()
    private var bodyFlux: Flux<DataBuffer> = ReadStreamFluxBuilder<Buffer, DataBuffer>(
        delegate, bufferConverter::toDataBuffer
    ).build()

    override fun getBody(): Flux<DataBuffer> = bodyFlux

    override fun getRemoteAddress(): InetSocketAddress? {
        val address = delegate.remoteAddress() ?: return null
        return InetSocketAddress(address.host(), address.port())
    }

    override fun initCookies(): MultiValueMap<String, HttpCookie> {
        val cookies: MultiValueMap<String, HttpCookie> = LinkedMultiValueMap()

        delegate.cookies()
            .stream()
            .map(CookieConverter::toHttpCookie)
            .forEach { cookie: HttpCookie -> cookies.add(cookie.name, cookie) }

        return cookies
    }

    override fun initSslInfo(): SslInfo? {
        return if (delegate.sslSession() == null) {
            null
        } else VertexSslInfo(delegate.sslSession())

    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> getNativeRequest(): T {
        return delegate as T
    }


    companion object {
        private fun initUri(request: HttpServerRequest): URI {
            return URI.create(request.absoluteURI())
        }

        private fun initHeaders(request: HttpServerRequest): HttpHeaders {
            val headers = HttpHeaders()
            request.headers()
                .forEach(Consumer { (key, value): Map.Entry<String, String?> ->
                    headers.add(key, value)
                })
            return headers
        }
    }
}
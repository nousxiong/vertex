package io.vertex.autoconfigure.web.client

import io.vertex.utils.CookieConverter
import io.vertx.core.http.HttpClientResponse
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.http.*
import org.springframework.http.client.reactive.ClientHttpResponse
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import reactor.core.publisher.Flux
import java.util.function.Consumer

/**
 * Created by xiongxl in 2023/6/20
 */
class VertexClientHttpResponse(
    private val delegate: HttpClientResponse,
    private val body: Flux<DataBuffer>,
) : ClientHttpResponse {
    override fun getHeaders(): HttpHeaders {
        val headers = HttpHeaders()
        delegate.headers()
            .forEach(Consumer { (key, value): Map.Entry<String, String?> ->
                headers.add(key, value)
            })
        return headers
    }

    override fun getBody(): Flux<DataBuffer> {
        return body
    }

    override fun getStatusCode(): HttpStatusCode {
        return HttpStatus.valueOf(delegate.statusCode())
    }

    override fun getCookies(): MultiValueMap<String, ResponseCookie> {
        val cookies: MultiValueMap<String, ResponseCookie> = LinkedMultiValueMap()
        delegate.cookies()
            .stream()
            .map(CookieConverter::toResponseCookies)
            .flatMap { it.stream() }
            .forEach { cookie: ResponseCookie -> cookies.add(cookie.name, cookie) }
        return cookies
    }
}
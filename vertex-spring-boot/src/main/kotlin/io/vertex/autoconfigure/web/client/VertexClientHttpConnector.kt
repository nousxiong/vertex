package io.vertex.autoconfigure.web.client

import io.vertex.autoconfigure.common.ReadStreamFluxBuilder
import io.vertex.utils.BufferConverter
import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer
import io.vertx.core.http.*
import org.slf4j.LoggerFactory
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.http.client.reactive.ClientHttpConnector
import org.springframework.http.client.reactive.ClientHttpRequest
import org.springframework.http.client.reactive.ClientHttpResponse
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.net.MalformedURLException
import java.net.URI
import java.util.concurrent.CompletableFuture
import java.util.function.Function

/**
 * Created by xiongxl in 2023/6/20
 */
class VertexClientHttpConnector(
    private val vertx: Vertx,
    private val clientOptions: HttpClientOptions = HttpClientOptions()
) : ClientHttpConnector {
    companion object {
        private val logger = LoggerFactory.getLogger(VertexClientHttpConnector::class.java)
    }
    private val bufferConverter = BufferConverter()
    override fun connect(
        method: org.springframework.http.HttpMethod,
        uri: URI,
        requestCallback: Function<in ClientHttpRequest, Mono<Void>>
    ): Mono<ClientHttpResponse> {
        logger.debug("Connecting to '${uri}' with '${method}")

        if (!uri.isAbsolute) {
            return Mono.error(
                IllegalArgumentException(
                    "URI is not absolute: $uri"
                )
            )
        }

        val responseFuture = CompletableFuture<ClientHttpResponse>()
        val client = vertx.createHttpClient(clientOptions)

        // New way to create absolute requests is via RequestOptions.
        // More info in https://github.com/vert-x3/vertx-4-migration-guide/issues/61.
        val requestOptions = RequestOptions()
        try {
            requestOptions.setAbsoluteURI(uri.toURL())
            requestOptions.setMethod(HttpMethod.valueOf(method.name()))
        } catch (e: MalformedURLException) {
            return Mono.error(
                java.lang.IllegalArgumentException(
                    "URI is malformed: $uri"
                )
            )
        }

        // request handler
        val requestFuture = CompletableFuture<HttpClientRequest>()
        client.request(requestOptions)
            .onFailure { ex: Throwable? -> requestFuture.completeExceptionally(ex) }
            .onSuccess { request: HttpClientRequest ->
                request.response()
                    .onSuccess { response: HttpClientResponse ->
                        val responseBody: Flux<DataBuffer> = responseToFlux(response).doFinally { client.close() }
                        responseFuture.complete(VertexClientHttpResponse(response, responseBody))
                    }
                    .onFailure { ex: Throwable? ->
                        responseFuture.completeExceptionally(
                            ex
                        )
                    }
                requestFuture.complete(request)
            }

        return Mono.fromFuture(requestFuture)
            .flatMap { request: HttpClientRequest ->
                requestCallback.apply(
                    VertexClientHttpRequest(request, bufferConverter)
                )
            }
            .then(Mono.fromCompletionStage(responseFuture))
    }

    private fun responseToFlux(response: HttpClientResponse): Flux<DataBuffer> {
        return ReadStreamFluxBuilder<Buffer, DataBuffer>(response, bufferConverter::toDataBuffer)
            .build()
    }
}
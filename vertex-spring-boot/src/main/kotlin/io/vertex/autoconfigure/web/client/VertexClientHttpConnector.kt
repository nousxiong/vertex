package io.vertex.autoconfigure.web.client

import io.vertex.autoconfigure.common.ReadStreamFluxBuilder
import io.vertex.autoconfigure.core.CondPtr
import io.vertex.autoconfigure.core.VerticleLifecycle
import io.vertex.util.BufferConverter
import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer
import io.vertx.core.http.HttpClient
import io.vertx.core.http.HttpClientOptions
import io.vertx.core.http.HttpClientRequest
import io.vertx.core.http.HttpClientResponse
import io.vertx.core.http.HttpMethod
import io.vertx.kotlin.core.http.requestOptionsOf
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
    private val httpClient = VerticleLifecycle<CondPtr<HttpClient>>("vertex.http.client.cache")
        .factory {
            CondPtr(vertx.createHttpClient(clientOptions), false)
        }
        .closer {
            it.get().close()
        }
        .defaulter {
            CondPtr(vertx.createHttpClient(clientOptions), true)
        }

    override fun connect(
        method: org.springframework.http.HttpMethod,
        uri: URI,
        requestCallback: Function<in ClientHttpRequest, Mono<Void>>
    ): Mono<ClientHttpResponse> {
        logger.debug("Connecting to '${uri}' with '${method}'")

        if (!uri.isAbsolute) {
            return Mono.error(
                IllegalArgumentException(
                    "URI is not absolute: $uri"
                )
            )
        }

        val client = httpClient.getOrDefault()

        // New way to create absolute requests is via RequestOptions.
        // More info in https://github.com/vert-x3/vertx-4-migration-guide/issues/61.
        val requestOptions = requestOptionsOf(
            connectTimeout = clientOptions.connectTimeout.toLong(),
            idleTimeout = clientOptions.idleTimeoutUnit.toMillis(clientOptions.idleTimeout.toLong()),
        )
        try {
            requestOptions.setAbsoluteURI(uri.toURL())
            requestOptions.setMethod(HttpMethod.valueOf(method.name()))
        } catch (_: MalformedURLException) {
            client.getWithCond()?.close()
            return Mono.error(
                java.lang.IllegalArgumentException(
                    "URI is malformed: $uri"
                )
            )
        }

        // request handler
        val requestFuture = CompletableFuture<HttpClientRequest>()
        // response handler
        val responseFuture = CompletableFuture<ClientHttpResponse>()
        // 统一处理结束回调
        val cleaner = fun () {
            client.getWithCond()?.close()
            logger.debug("Request to '${uri}' with '${method}' finished")
        }
        client.get().request(requestOptions)
            .onFailure { ex: Throwable? ->
                cleaner()
                requestFuture.completeExceptionally(ex)
            }
            .onSuccess { request: HttpClientRequest ->
                request.response()
                    .onSuccess { response: HttpClientResponse ->
                        val responseBody: Flux<DataBuffer> = responseToFlux(response).doFinally { cleaner() }
                        responseFuture.complete(VertexClientHttpResponse(response, responseBody))
                    }
                    .onFailure { ex: Throwable? ->
                        cleaner()
                        responseFuture.completeExceptionally(ex)
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
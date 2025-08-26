package io.vertex.autoconfigure.web.client

import io.vertex.autoconfigure.web.client.properties.HttpClientOptionsCustomizer
import io.vertex.autoconfigure.web.client.properties.HttpClientProperties
import io.vertex.autoconfigure.web.client.properties.WebSocketClientProperties
import io.vertx.core.Vertx
import io.vertx.core.http.HttpClient
import io.vertx.core.http.HttpClientOptions
import io.vertx.core.http.WebSocketClientOptions
import org.springframework.boot.autoconfigure.AutoConfigureBefore
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.http.client.reactive.ClientHttpConnectorAutoConfiguration
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

/**
 * Created by xiongxl in 2023/6/20
 */
@Configuration
@ConditionalOnProperty(name = ["vertex.http.client.enabled"])
@ConditionalOnClass(WebClient::class, HttpClient::class)
@ConditionalOnBean(Vertx::class)
@AutoConfigureBefore(ClientHttpConnectorAutoConfiguration::class)
@EnableConfigurationProperties(HttpClientProperties::class, WebSocketClientProperties::class)
class VertexClientAutoConfiguration(
    private val httpClientProperties: HttpClientProperties,
    private val webSocketClientProperties: WebSocketClientProperties,
    private val customizers: Set<HttpClientOptionsCustomizer>,
) {

    @Bean
    @ConditionalOnMissingBean
    fun vertexClientHttpConnector(vertx: Vertx): VertexClientHttpConnector {
        val httpClientOptions = customizeHttpClientOptions(httpClientProperties, customizers)
        return VertexClientHttpConnector(vertx, httpClientOptions)
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(name = ["vertex.http.client.websocket.enabled"])
    fun vertexWebSocketClient(vertx: Vertx): VertexWebSocketClient {
        val webSocketClientOptions = WebSocketClientOptions(webSocketClientProperties)
        return VertexWebSocketClient(vertx, webSocketClientOptions)
    }

    private fun customizeHttpClientOptions(
        original: HttpClientOptions,
        customizers: Set<HttpClientOptionsCustomizer>
    ): HttpClientOptions {
        var customized = HttpClientOptions(original)
        for (customizer in customizers) {
            customized = customizer.apply(customized)
        }
        return customized
    }
}
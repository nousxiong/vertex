package io.vertex.autoconfigure.web.client

import io.vertex.autoconfigure.web.client.properties.HttpClientOptionsCustomizer
import io.vertex.autoconfigure.web.client.properties.HttpClientProperties
import io.vertx.core.Vertx
import io.vertx.core.http.HttpClient
import io.vertx.core.http.HttpClientOptions
import org.springframework.boot.autoconfigure.AutoConfigureBefore
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.web.reactive.function.client.ClientHttpConnectorAutoConfiguration
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.reactive.ClientHttpConnector
import org.springframework.web.reactive.function.client.WebClient

/**
 * Created by xiongxl in 2023/6/20
 */
@Configuration
@ConditionalOnClass(WebClient::class, HttpClient::class)
@ConditionalOnBean(Vertx::class)
@ConditionalOnMissingBean(ClientHttpConnector::class)
@AutoConfigureBefore(ClientHttpConnectorAutoConfiguration::class)
@EnableConfigurationProperties(HttpClientProperties::class)
class VertexClientAutoConfiguration(
    httpClientProperties: HttpClientProperties,
    customizers: Set<HttpClientOptionsCustomizer>,
) {
    private val httpClientOptions = customizeHttpClientOptions(httpClientProperties, customizers)

    @Bean
    fun vertexClientHttpConnector(vertx: Vertx): VertexClientHttpConnector {
        return VertexClientHttpConnector(vertx, httpClientOptions)
    }

    @Bean
    fun vertexWebSocketClient(vertx: Vertx): VertexWebSocketClient {
        return VertexWebSocketClient(vertx, httpClientOptions)
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
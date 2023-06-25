package io.vertex.autoconfigure.web.test

import io.vertex.autoconfigure.web.client.VertexClientHttpConnector
import io.vertex.autoconfigure.web.server.VertexReactiveWebServerFactory
import io.vertex.autoconfigure.web.server.properties.HttpServerProperties
import org.springframework.boot.web.codec.CodecCustomizer
import org.springframework.context.ApplicationContext
import org.springframework.http.codec.ClientCodecConfigurer
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.client.ExchangeStrategies
import java.util.function.Consumer
import java.util.function.Supplier

/**
 * Created by xiongxl in 2023/6/25
 */
class VertexWebTestClientSupplier(private val applicationContext: ApplicationContext) : Supplier<WebTestClient> {
    private var webTestClient: WebTestClient? = null
    override fun get(): WebTestClient {
        return webTestClient ?: let {
            val webTestClient = createWebTestClient()
            this.webTestClient = webTestClient
            webTestClient
        }
    }

    private fun createWebTestClient(): WebTestClient {
        val connector: VertexClientHttpConnector = applicationContext.getBean(VertexClientHttpConnector::class.java)
        val builder = WebTestClient.bindToServer(connector)
        val baseUrl = "${getProtocol()}://localhost:${getPort()}"
        builder.baseUrl(baseUrl)
        customizeWebTestClientCodecs(builder)
        return builder.build()
    }

    private fun getProtocol(): String {
        val factory: VertexReactiveWebServerFactory =
            applicationContext.getBean(VertexReactiveWebServerFactory::class.java)
        val isSsl: Boolean = if (factory.ssl != null) {
            factory.ssl.isEnabled
        } else {
            val serverProperties: HttpServerProperties = applicationContext.getBean(HttpServerProperties::class.java)
            serverProperties.isSsl
        }
        return if (isSsl) "https" else "http"
    }

    private fun getPort(): String {
        return applicationContext.environment.getProperty("local.server.port", "8080")
    }

    private fun customizeWebTestClientCodecs(builder: WebTestClient.Builder) {
        val customizers: Collection<CodecCustomizer> = applicationContext.getBeansOfType(
            CodecCustomizer::class.java
        ).values
        val strategies = ExchangeStrategies.builder()
            .codecs { codecs: ClientCodecConfigurer? ->
                customizers.forEach(
                    Consumer { customizer: CodecCustomizer -> customizer.customize(codecs) })
            }
            .build()
        builder.exchangeStrategies(strategies)
    }
}
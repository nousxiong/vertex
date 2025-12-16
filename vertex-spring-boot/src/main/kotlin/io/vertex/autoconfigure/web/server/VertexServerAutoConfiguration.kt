package io.vertex.autoconfigure.web.server

import io.vertex.autoconfigure.core.GracefulShutdown
import io.vertex.autoconfigure.web.server.properties.HttpServerOptionsCustomizer
import io.vertex.autoconfigure.web.server.properties.HttpServerProperties
import io.vertex.autoconfigure.web.server.properties.ServerDeploymentProperties
import io.vertx.core.Vertx
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.getBean
import org.springframework.boot.autoconfigure.AutoConfigureBefore
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.boot.autoconfigure.web.reactive.ReactiveWebServerFactoryAutoConfiguration
import org.springframework.boot.autoconfigure.web.reactive.WebFluxAutoConfiguration
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.web.reactive.server.ReactiveWebServerFactory
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.ReactiveHttpInputMessage
import org.springframework.web.reactive.config.WebFluxConfigurer
import org.springframework.web.reactive.socket.server.WebSocketService
import org.springframework.web.reactive.socket.server.support.HandshakeWebSocketService

/**
 * Created by xiongxl in 2023/6/7
 */
@Configuration
@AutoConfigureBefore(ReactiveWebServerFactoryAutoConfiguration::class, WebFluxAutoConfiguration::class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
@ConditionalOnClass(ReactiveHttpInputMessage::class)
@ConditionalOnMissingBean(ReactiveWebServerFactory::class)
@EnableConfigurationProperties(HttpServerProperties::class, ServerDeploymentProperties::class)
class VertexServerAutoConfiguration : WebFluxConfigurer {
    @Autowired
    lateinit var applicationContext: ApplicationContext

    @Bean
    fun vertexReactiveWebServerFactory(
        vertx: Vertx,
        httpServerProperties: HttpServerProperties,
        serverDeploymentProperties: ServerDeploymentProperties,
        verticleFactory: VertexServerVerticleFactory,
    ): VertexReactiveWebServerFactory {
        return VertexReactiveWebServerFactory(
            vertx,
            httpServerProperties,
            serverDeploymentProperties,
            verticleFactory,
        )
    }

    @Bean
    fun vertexWebServerFactoryCustomizer(
        userDefinedCustomizers: Set<HttpServerOptionsCustomizer>?
    ): VertexReactiveWebServerFactoryCustomizer? {
        return VertexReactiveWebServerFactoryCustomizer(userDefinedCustomizers)
    }

    @Bean
    @ConditionalOnMissingBean
    fun vertexServerVerticleFactory(): VertexServerVerticleFactory {
        return object : VertexServerVerticleFactory {
            override fun create(
                instances: Int,
                index: Int,
                gracefulShutdown: GracefulShutdown?,
            ): VertexServerVerticle {
                return VertexServerVerticle(instances, index, gracefulShutdown)
            }
        }
    }

    /**
     * vertx的websocket支持
     */
    override fun getWebSocketService(): WebSocketService? {
        val webServerFactory = applicationContext.getBean<VertexReactiveWebServerFactory>()
        val httpServerOptions = webServerFactory.customizeHttpServerOptions()
        val requestUpgradeStrategy = VertexRequestUpgradeStrategy(
            httpServerOptions.maxWebSocketFrameSize, httpServerOptions.maxWebSocketMessageSize
        )
        return HandshakeWebSocketService(requestUpgradeStrategy)
    }
}
package io.vertex.autoconfigure.web.server

import io.vertex.autoconfigure.core.GracefulShutdown
import io.vertex.autoconfigure.web.server.properties.HttpServerOptionsCustomizer
import io.vertex.autoconfigure.web.server.properties.HttpServerProperties
import io.vertex.autoconfigure.web.server.properties.ServerDeploymentProperties
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.http.HttpServerOptions
import io.vertx.ext.web.RoutingContext
import org.springframework.boot.autoconfigure.AutoConfigureBefore
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.boot.autoconfigure.web.reactive.WebFluxAutoConfiguration
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.web.reactive.server.ReactiveWebServerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.ReactiveHttpInputMessage
import org.springframework.web.reactive.config.DelegatingWebFluxConfiguration
import org.springframework.web.reactive.socket.server.WebSocketService
import org.springframework.web.reactive.socket.server.support.HandshakeWebSocketService

/**
 * Created by xiongxl in 2023/6/7
 */
@Configuration
@AutoConfigureBefore(WebFluxAutoConfiguration::class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
@ConditionalOnClass(ReactiveHttpInputMessage::class)
@ConditionalOnMissingBean(ReactiveWebServerFactory::class)
@EnableConfigurationProperties(HttpServerProperties::class, ServerDeploymentProperties::class)
class VertexServerAutoConfiguration : DelegatingWebFluxConfiguration() {

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
                httpServerOptions: HttpServerOptions,
                handler: Handler<RoutingContext>,
                gracefulShutdown: GracefulShutdown?,
            ): VertexServerVerticle {
                return VertexServerVerticle(instances, index, httpServerOptions, handler, gracefulShutdown)
            }
        }
    }

//    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
//        registry.addResourceHandler("/**")
//            .addResourceLocations(
//                "classpath:/static/",
//                "classpath:/public/",
//                "classpath:/resources/",
//                "classpath:/META-INF/resources/",
//            )
//        super.addResourceHandlers(registry)
//    }

    /**
     * 选择重载getWebSocketService方法，而非提供对应的bean，是因为：
     *  报Error creating bean with name 'webFluxWebSocketHandlerAdapter'，
     *      springboot是2.7.2时不依赖jakarta.websocket:jakarta.websocket-api，
     *      而springboot-3.1.0需要（可能从某个版本就开始）；
     *  另外：webFluxWebSocketHandlerAdapter这个bean无论是否用户提供了自己的bean，都会创建
     */
    override fun getWebSocketService(): WebSocketService? {
        val webServerFactory = applicationContext?.getBean(VertexReactiveWebServerFactory::class.java) ?: return null
        val httpServerOptions = webServerFactory.customizeHttpServerOptions()
        val requestUpgradeStrategy = VertexRequestUpgradeStrategy(
            httpServerOptions.maxWebSocketFrameSize, httpServerOptions.maxWebSocketMessageSize
        )
        return HandshakeWebSocketService(requestUpgradeStrategy)
    }
}
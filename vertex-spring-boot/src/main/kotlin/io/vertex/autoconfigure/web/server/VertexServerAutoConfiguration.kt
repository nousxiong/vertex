package io.vertex.autoconfigure.web.server

import io.vertex.autoconfigure.web.server.properties.HttpServerOptionsCustomizer
import io.vertex.autoconfigure.web.server.properties.HttpServerProperties
import io.vertex.autoconfigure.web.server.properties.ServerDeploymentProperties
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.http.HttpServerOptions
import io.vertx.ext.web.RoutingContext
import org.springframework.boot.autoconfigure.AutoConfigureOrder
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.web.reactive.server.ReactiveWebServerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.core.Ordered
import org.springframework.http.ReactiveHttpInputMessage

/**
 * Created by xiongxl in 2023/6/7
 */
@Configuration(proxyBeanMethods = false)
@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
@ConditionalOnClass(ReactiveHttpInputMessage::class)
@ConditionalOnMissingBean(ReactiveWebServerFactory::class)
@EnableConfigurationProperties(HttpServerProperties::class, ServerDeploymentProperties::class)
class VertexServerAutoConfiguration {

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
                httpServerOptions: HttpServerOptions,
                handler: Handler<RoutingContext>,
                gracefulShutdown: GracefulShutdown?,
            ): VertexServerVerticle {
                return VertexServerVerticle(httpServerOptions, handler, gracefulShutdown)
            }
        }
    }
}
package io.vertex.autoconfigure.web.server

import io.vertex.autoconfigure.web.server.properties.HttpServerOptionsCustomizer
import io.vertex.autoconfigure.web.server.properties.HttpServerProperties
import io.vertex.autoconfigure.web.server.properties.ServerDeploymentProperties
import io.vertx.core.Vertx
import io.vertx.core.http.HttpServerOptions
import org.springframework.boot.web.reactive.server.AbstractReactiveWebServerFactory
import org.springframework.boot.web.server.WebServer
import org.springframework.http.server.reactive.HttpHandler
import java.util.function.Supplier

/**
 * Created by xiongxl in 2023/6/7
 */
class VertexReactiveWebServerFactory(
    private val vertx: Vertx,
    private val httpServerProperties: HttpServerProperties,
    private val serverDeploymentProperties: ServerDeploymentProperties,
    private val verticleSupplier: Supplier<VertexServerVerticle>,
) : AbstractReactiveWebServerFactory() {
    private val httpServerOptionsCustomizers = mutableListOf<HttpServerOptionsCustomizer>()

    override fun getWebServer(httpHandler: HttpHandler): WebServer {
        val httpServerOptions = customizeHttpServerOptions(HttpServerOptions(httpServerProperties))
        TODO("Not yet implemented")
    }

    fun registerHttpServerOptionsCustomizer(customizer: HttpServerOptionsCustomizer) {
        httpServerOptionsCustomizers.add(customizer)
    }

    private fun customizeHttpServerOptions(httpServerOptions: HttpServerOptions): HttpServerOptions {
        var resultOptions = httpServerOptions
        for (customizer in httpServerOptionsCustomizers) {
            resultOptions = customizer.apply(resultOptions)
        }
        return resultOptions
    }
}
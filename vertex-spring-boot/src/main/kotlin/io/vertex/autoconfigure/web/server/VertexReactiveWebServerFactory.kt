package io.vertex.autoconfigure.web.server

import io.vertex.autoconfigure.web.server.properties.HttpServerProperties
import io.vertex.autoconfigure.web.server.properties.ServerDeploymentProperties
import io.vertx.core.Verticle
import io.vertx.core.Vertx
import org.springframework.boot.web.reactive.server.AbstractReactiveWebServerFactory
import org.springframework.boot.web.server.WebServer
import org.springframework.http.server.reactive.HttpHandler
import java.util.function.Supplier

/**
 * Created by xiongxl in 2023/6/7
 */
class VertexReactiveWebServerFactory(
    val vertx: Vertx,
    val httpServerProperties: HttpServerProperties,
    val serverDeploymentProperties: ServerDeploymentProperties,
    val verticleSupplier: Supplier<Verticle>,
) : AbstractReactiveWebServerFactory() {
    override fun getWebServer(httpHandler: HttpHandler): WebServer {
        TODO("Not yet implemented")
    }
}
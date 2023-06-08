package io.vertex.autoconfigure.web.server

import io.vertx.core.DeploymentOptions
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.http.HttpServerOptions
import io.vertx.ext.web.RoutingContext
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.web.server.GracefulShutdownCallback
import org.springframework.boot.web.server.WebServer

/**
 * Created by xiongxl in 2023/6/8
 */
class VertexWebServer(
    private val vertx: Vertx,
    private val httpServerOptions: HttpServerOptions,
    private val deploymentsOptions: DeploymentOptions,
    private val requestHandler: Handler<RoutingContext>
) : WebServer {
    companion object {
        private val logger: Logger = LoggerFactory.getLogger(VertexWebServer::class.java)
    }

    private var deploymentId = ""

    override fun start() {
        if (deploymentId.isNotEmpty()) {
            return
        }
    }

    override fun stop() {
        TODO("Not yet implemented")
    }

    override fun getPort(): Int {
        TODO("Not yet implemented")
    }

    override fun shutDownGracefully(callback: GracefulShutdownCallback) {

    }
}
package io.vertex.autoconfigure.web.server

import io.vertx.core.AsyncResult
import io.vertx.core.DeploymentOptions
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.http.HttpServerOptions
import io.vertx.ext.web.RoutingContext
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.web.server.GracefulShutdownCallback
import org.springframework.boot.web.server.GracefulShutdownResult
import org.springframework.boot.web.server.Shutdown
import org.springframework.boot.web.server.WebServer
import reactor.core.publisher.Mono
import reactor.core.publisher.MonoSink
import java.time.Duration
import java.util.concurrent.atomic.AtomicReferenceArray

/**
 * Created by xiongxl in 2023/6/8
 */
class VertexWebServer(
    private val vertx: Vertx,
    private val httpServerOptions: HttpServerOptions,
    private val deploymentsOptions: DeploymentOptions,
    private val verticleFactory: VertexServerVerticleFactory,
    private val requestHandler: Handler<RoutingContext>,
    shutdown: Shutdown,
) : WebServer {
    companion object {
        private val logger: Logger = LoggerFactory.getLogger(VertexWebServer::class.java)
    }

    private var gracefulShutdown: GracefulShutdown? = if (shutdown == Shutdown.GRACEFUL) {
        GracefulShutdown(deploymentsOptions.instances)
    } else {
        null
    }
    private var deploymentId = ""
    private var undeployed = false
    private val verticles = AtomicReferenceArray<VertexServerVerticle>(deploymentsOptions.instances)

    override fun start() {
        if (deploymentId.isNotEmpty()) {
            return
        }

        Mono.create { sink: MonoSink<Void?> ->
            logger.info("Vertex HTTP server verticle deploying with ${deploymentsOptions.instances} instances")
            vertx.deployVerticle({
                val verticle = verticleFactory.create(httpServerOptions, requestHandler, gracefulShutdown)
                verticles[verticle.index] = verticle
                logger.info("Vertex HTTP ${verticle.id} deployed")
                verticle
            }, deploymentsOptions).onComplete { ar: AsyncResult<String> ->
                if (ar.succeeded()) {
                    deploymentId = ar.result()
                    logger.info("Vertex HTTP server<${port}> verticle deploy completed")
                    sink.success()
                } else {
                    sink.error(ar.cause())
                }
            }
        }.block(Duration.ofSeconds(10))
    }

    override fun stop() {
        if (deploymentId.isEmpty()) {
            return
        }

        if (undeployed) {
            return
        }
        undeployed = true

        gracefulShutdown?.abort()
        Mono.create { sink: MonoSink<Void?> ->
            vertx.undeploy(deploymentId).onComplete { ar: AsyncResult<Void?> ->
                if (ar.succeeded()) {
                    sink.success()
                } else {
                    sink.error(ar.cause())
                }
            }
        }.doOnTerminate {
            deploymentId = ""
            for (i in 0 until verticles.length()) {
                verticles[i] = null
            }
        }.block(Duration.ofSeconds(10))
    }

    override fun shutDownGracefully(callback: GracefulShutdownCallback) {
        val gracefulShutdown = gracefulShutdown
        if (gracefulShutdown == null) {
            callback.shutdownComplete(GracefulShutdownResult.IMMEDIATE)
            return
        }
        vertx.undeploy(deploymentId).onComplete {
            deploymentId = ""
            for (i in 0 until verticles.length()) {
                verticles[i] = null
            }
            gracefulShutdown.shutDownGracefully(callback)
        }
    }

    override fun getPort(): Int {
        return verticles[0]?.port ?: 0
    }
}
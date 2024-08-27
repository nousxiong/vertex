package io.vertex.autoconfigure.web.server

import io.vertex.autoconfigure.core.GracefulShutdown
import io.vertx.core.Handler
import io.vertx.core.http.HttpServerOptions
import io.vertx.ext.web.RoutingContext

/**
 * Created by xiongxl in 2023/6/15
 */
interface VertexServerVerticleFactory {
    fun create(
        instances: Int,
        index: Int,
        httpServerOptions: HttpServerOptions,
        handler: Handler<RoutingContext>,
        gracefulShutdown: GracefulShutdown?,
    ): VertexServerVerticle
}
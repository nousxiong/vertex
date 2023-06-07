package io.vertex.autoconfigure.web.server

import io.vertx.core.Handler
import io.vertx.core.http.HttpServerOptions
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.coroutines.CoroutineVerticle

/**
 * Created by xiongxl in 2023/6/7
 */
open class VertexServerVerticle(
    private val httpServerOptions: HttpServerOptions,
//    private val requestHandler: Handler<RoutingContext>,
) : CoroutineVerticle() {
    override suspend fun start() {

    }
}
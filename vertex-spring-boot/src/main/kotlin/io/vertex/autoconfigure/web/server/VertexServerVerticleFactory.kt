package io.vertex.autoconfigure.web.server

import io.vertex.autoconfigure.core.GracefulShutdown

/**
 * Created by xiongxl in 2023/6/15
 */
interface VertexServerVerticleFactory {
    fun create(
        instances: Int,
        index: Int,
        gracefulShutdown: GracefulShutdown?,
    ): VertexServerVerticle
}
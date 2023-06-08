package io.vertex.autoconfigure.web.server.properties

import io.vertx.core.http.HttpServerOptions
import java.util.function.Function

/**
 * Created by xiongxl in 2023/6/8
 */
@FunctionalInterface
interface HttpServerOptionsCustomizer : Function<HttpServerOptions, HttpServerOptions>

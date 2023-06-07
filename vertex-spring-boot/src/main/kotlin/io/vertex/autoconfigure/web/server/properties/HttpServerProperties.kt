package io.vertex.autoconfigure.web.server.properties

import io.vertx.core.http.HttpServerOptions
import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * Created by xiongxl in 2023/6/7
 */
@ConfigurationProperties(prefix = HttpServerProperties.PROPERTIES_PREFIX)
class HttpServerProperties : HttpServerOptions() {
    companion object {
        const val PROPERTIES_PREFIX = "vertex.http.server"
    }
}
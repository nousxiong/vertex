package io.vertex.autoconfigure.web.server.properties

import io.vertx.core.http.HttpServerOptions
import org.springframework.boot.web.server.AbstractConfigurableWebServerFactory

/**
 * Created by xiongxl in 2023/6/8
 */
class CompressionCustomizer(private val factory: AbstractConfigurableWebServerFactory) : HttpServerOptionsCustomizer {
    override fun apply(options: HttpServerOptions): HttpServerOptions {
        val compression = factory.compression

        if (compression != null) {
            options.setCompressionSupported(compression.enabled)
        }

        return options
    }
}
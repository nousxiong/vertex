package io.vertex.autoconfigure.web.server.properties

import io.vertx.core.http.HttpServerOptions
import org.springframework.boot.web.server.AbstractConfigurableWebServerFactory

/**
 * Created by xiongxl in 2023/6/8
 */
class PortCustomizer(private val factory: AbstractConfigurableWebServerFactory) : HttpServerOptionsCustomizer {
    override fun apply(options: HttpServerOptions): HttpServerOptions {
        if (factory.port >= 0) {
            options.setPort(factory.port)
        }

        return options
    }
}
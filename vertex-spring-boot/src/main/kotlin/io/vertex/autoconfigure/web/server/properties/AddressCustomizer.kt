package io.vertex.autoconfigure.web.server.properties

import io.vertx.core.http.HttpServerOptions
import org.springframework.boot.web.server.AbstractConfigurableWebServerFactory

/**
 * Created by xiongxl in 2023/6/8
 */
class AddressCustomizer(private val factory: AbstractConfigurableWebServerFactory) : HttpServerOptionsCustomizer {
    override fun apply(options: HttpServerOptions): HttpServerOptions {
        val address = factory.address

        if (address != null && address.hostAddress != null) {
            options.setHost(address.hostAddress)
        }

        return options
    }
}
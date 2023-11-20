package io.vertex.autoconfigure.web.client.properties

import io.vertx.core.http.WebSocketClientOptions
import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * Created by xiongxl in 2023/11/20
 */
@ConfigurationProperties(prefix = WebSocketClientProperties.PROPERTIES_PREFIX)
class WebSocketClientProperties : WebSocketClientOptions() {
    companion object {
        const val PROPERTIES_PREFIX = "vertex.websocket.client"
    }
}
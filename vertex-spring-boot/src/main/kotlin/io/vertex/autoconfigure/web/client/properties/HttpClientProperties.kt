package io.vertex.autoconfigure.web.client.properties

import io.vertx.core.http.HttpClientOptions
import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * Created by xiongxl in 2023/6/20
 */
@ConfigurationProperties(prefix = HttpClientProperties.PROPERTIES_PREFIX)
class HttpClientProperties : HttpClientOptions() {
    companion object {
        const val PROPERTIES_PREFIX = "vertex.http.client"
    }
}
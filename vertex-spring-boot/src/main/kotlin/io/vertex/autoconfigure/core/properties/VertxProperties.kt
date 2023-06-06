package io.vertex.autoconfigure.core.properties

import io.vertx.core.VertxOptions
import io.vertx.kotlin.core.vertxOptionsOf
import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * Created by xiongxl in 2023/6/6
 * TODO 完成所有vertx的配置项
 */
@ConfigurationProperties(prefix = VertxProperties.PROPERTIES_PREFIX)
class VertxProperties {
    companion object {
        const val PROPERTIES_PREFIX = "vertx"
    }

    var eventLoopPoolSize: Int = VertxOptions.DEFAULT_EVENT_LOOP_POOL_SIZE

    fun toVertxOptions(): VertxOptions {
        return vertxOptionsOf(eventLoopPoolSize = eventLoopPoolSize)
    }
}
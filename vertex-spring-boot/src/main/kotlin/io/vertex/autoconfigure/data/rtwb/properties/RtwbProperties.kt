package io.vertex.autoconfigure.data.rtwb.properties

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * Created by xiongxl in 2023/11/30
 */
@ConfigurationProperties(prefix = RtwbProperties.PROPERTIES_PREFIX)
class RtwbProperties {
    companion object {
        const val PROPERTIES_PREFIX = "vertex.data.rtwb"
    }
}
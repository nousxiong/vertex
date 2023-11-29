package io.vertex.autoconfigure.data.rtwb

import io.vertex.autoconfigure.data.rtwb.properties.RtwbProperties
import io.vertx.core.Vertx
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.data.repository.CrudRepository

/**
 * Created by xiongxl in 2023/11/30
 */
@Configuration
@ConditionalOnClass(CrudRepository::class)
@ConditionalOnBean(Vertx::class)
@EnableConfigurationProperties(RtwbProperties::class)
class VertexRtwbAutoConfiguration {
}
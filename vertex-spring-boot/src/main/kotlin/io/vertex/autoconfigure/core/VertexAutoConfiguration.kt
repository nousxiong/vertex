package io.vertex.autoconfigure.core

import io.vertex.autoconfigure.core.properties.VertxProperties
import io.vertx.core.Vertx
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Created by xiongxl in 2023/6/6
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(Vertx::class)
@EnableConfigurationProperties(VertxProperties::class)
class VertexAutoConfiguration {

    /**
     * 获取Vertx Bean实例
     */
    @Bean(destroyMethod = "")
    fun vertx(properties: VertxProperties): Vertx {
        return Vertx.vertx(properties)
    }

    /**
     * @see org.springframework.beans.factory.support.DisposableBeanAdapter
     */
    @Bean
    @ConditionalOnMissingBean
    fun vertexCloser(vertx: Vertx): VertexCloser {
        return object : VertexCloser {
            override fun close() {
                vertx.close()
            }
        }
    }
}
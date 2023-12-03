package io.vertex.data.simplex

import io.vertex.autoconfigure.data.rtwb.VertexRtwbAutoConfiguration
import io.vertex.autoconfigure.data.rtwb.service.PrimaryDataService
import io.vertx.core.Vertx
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.repository.CrudRepository

/**
 * Created by xiongxl in 2023/12/3
 */
@Configuration
@AutoConfiguration(before = [VertexRtwbAutoConfiguration::class])
@ConditionalOnClass(CrudRepository::class)
@ConditionalOnBean(Vertx::class)
class VertexSimplexAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    fun <T, ID> primaryDataService(): PrimaryDataService<T, ID> {
        return RedisPrimaryDataService()
    }
}
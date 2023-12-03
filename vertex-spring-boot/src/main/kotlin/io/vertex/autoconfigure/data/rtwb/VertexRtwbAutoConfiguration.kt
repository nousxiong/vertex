package io.vertex.autoconfigure.data.rtwb

import io.vertex.autoconfigure.data.rtwb.properties.RtwbProperties
import io.vertex.autoconfigure.data.rtwb.service.AssistDataService
import io.vertex.autoconfigure.data.rtwb.service.BehindDataService
import io.vertex.autoconfigure.data.rtwb.service.PrimaryDataService
import io.vertex.autoconfigure.data.rtwb.service.RtwbDataService
import io.vertex.autoconfigure.data.rtwb.service.noop.NoopAssistDataService
import io.vertex.autoconfigure.data.rtwb.service.noop.NoopBehindDataService
import io.vertex.autoconfigure.data.rtwb.service.noop.NoopPrimaryDataService
import io.vertex.autoconfigure.data.rtwb.service.noop.RtwbDataServiceImpl
import io.vertx.core.Vertx
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
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
    @Bean
    fun <T, ID> rtwbDataService(
        primaryDataService: PrimaryDataService<T, ID>,
        behindDataService: BehindDataService<T, ID>,
        assistDataService: AssistDataService<T, ID>
    ): RtwbDataService<T, ID> {
        return RtwbDataServiceImpl(
            primaryDataService,
            behindDataService,
            assistDataService
        )
    }

    @Bean
    @ConditionalOnMissingBean
    fun <T, ID> primaryDataService(): PrimaryDataService<T, ID> {
        return NoopPrimaryDataService()
    }

    @Bean
    @ConditionalOnMissingBean
    fun <T, ID> behindDataService(): BehindDataService<T, ID> {
        return NoopBehindDataService()
    }

    @Bean
    @ConditionalOnMissingBean
    fun <T, ID> assistedDataService(): AssistDataService<T, ID> {
        return NoopAssistDataService()
    }
}
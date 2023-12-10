package io.vertex.data.redisql

import io.vertex.autoconfigure.data.rtwb.VertexRtwbAutoConfiguration
import io.vertex.autoconfigure.data.rtwb.service.PrimaryDataService
import io.vertx.core.Vertx
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.keyvalue.core.KeyValueAdapter
import org.springframework.data.keyvalue.core.KeyValueOperations
import org.springframework.data.keyvalue.core.KeyValueTemplate
import org.springframework.data.map.MapKeyValueAdapter
import org.springframework.data.repository.CrudRepository


/**
 * Created by xiongxl in 2023/12/3
 */
@Configuration
@AutoConfiguration(
    before = [VertexRtwbAutoConfiguration::class],
//    beforeName = ["org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration"]
)
@ConditionalOnClass(CrudRepository::class)
@ConditionalOnBean(Vertx::class)
class VertexRedisqlAutoConfiguration {
    companion object {
        val logger: Logger = LoggerFactory.getLogger(VertexRedisqlAutoConfiguration::class.java)
    }
    @Bean
    @ConditionalOnMissingBean
    fun <T, ID> primaryDataService(): PrimaryDataService<T, ID> {
        logger.info("create redisql primaryDataService")
        return RedisPrimaryDataService()
    }

    @Bean
    @ConditionalOnMissingBean
    fun mapKeyValueTemplate2(keyValueAdapter: KeyValueAdapter): KeyValueOperations {
        logger.info("mapKeyValueTemplate")
        return KeyValueTemplate(keyValueAdapter)
    }

    @Bean
    @ConditionalOnMissingBean
    fun keyValueAdapter(): KeyValueAdapter {
        logger.info("keyValueAdapter")
        return MapKeyValueAdapter(hashMapOf<String?, MutableMap<Any, Any>?>().apply {
            put("persons", RedisqlKeyValueMap())
        })
    }
}
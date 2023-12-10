package io.vertex.data.redisql

import org.springframework.data.map.repository.config.EnableMapRepositories


@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@EnableMapRepositories(keyValueTemplateRef = "mapKeyValueTemplate2")
annotation class EnableRedisqlRepositories

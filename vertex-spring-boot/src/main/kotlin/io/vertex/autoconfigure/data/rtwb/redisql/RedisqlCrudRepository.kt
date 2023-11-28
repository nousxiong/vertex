package io.vertex.autoconfigure.data.rtwb.redisql

import org.springframework.data.annotation.Id
//import org.springframework.data.redis.core.RedisHash
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.NoRepositoryBean

/**
 * Created by xiongxl in 2023-11-27
 */
@NoRepositoryBean
interface RedisqlCrudRepository : CrudRepository<Person, Long> {
}

//@RedisHash("person")
class Person(
    @Id val id: Long,
    val name: String,
    val age: Int
)
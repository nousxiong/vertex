package io.vertex.data.redisql

import io.vertex.autoconfigure.data.rtwb.service.PrimaryDataService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

/**
 * Created by xiongxl in 2023/12/3
 */
@Service
class RedisPrimaryDataService<T, ID> : PrimaryDataService<T, ID> {
    companion object {
        val logger: Logger = LoggerFactory.getLogger(RedisPrimaryDataService::class.java)

    }


//    @Autowired
//    private lateinit var redisRepository: RedisRepository

    override fun status(entity: T) {
//        logger.info("redisRepository: $redisRepository")
        logger.info("$this entity<$entity>")
    }
}
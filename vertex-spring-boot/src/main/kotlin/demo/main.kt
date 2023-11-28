package demo

//import org.springframework.data.redis.connection.RedisConnectionFactory
//import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
//import org.springframework.data.redis.repository.configuration.EnableRedisRepositories
import io.vertex.autoconfigure.core.VertexCloser
import io.vertex.autoconfigure.core.VertexVerticle
import io.vertex.autoconfigure.core.VerticleLifecycle
import io.vertex.util.verticleScope
import io.vertx.core.Future
import io.vertx.core.Vertx
import io.vertx.ext.web.client.WebClient
import io.vertx.kotlin.coroutines.coAwait
import kotlinx.coroutines.delay
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController


/**
 * Created by xiongxl in 2023/6/16
 */
@SpringBootApplication
//@EnableRedisRepositories
@RestController
class VertexApplication(private val vertx: Vertx) {
    companion object {
        val logger: Logger = LoggerFactory.getLogger(VertexApplication::class.java)
    }

    private val webClient = VerticleLifecycle<WebClient>("demo.WebClient").factory {
        logger.info("create WebClient ctx=${VertexVerticle.id()}")
        WebClient.create(vertx)
    }.closer {
        logger.info("close WebClient ctx=${VertexVerticle.id()}")
        it.close()
    }

    private val webClient2 = VerticleLifecycle<WebClient>("demo.WebClient2").awaitFactory {
        logger.info("create WebClient2 ctx=${VertexVerticle.id()}")
        WebClient.create(vertx)
    }.closer {
        logger.info("close WebClient2 ctx=${VertexVerticle.id()}")
        it.close()
    }

    private val webClient3 = VerticleLifecycle<WebClient>("demo.WebClient3").asyncFactory {
        logger.info("create WebClient3 ctx=${VertexVerticle.id()}")
        Future.future {
            it.complete(WebClient.create(vertx))
        }
    }.closer {
        logger.info("close WebClient3 ctx=${VertexVerticle.id()}")
        it.close()
    }

    private suspend fun getUrlSizeAwait(host: String, uri: String = "/"): Int {
        val client = webClient2.getAwait()
        val req = client.get(host, uri)
        val rsp = req.send().coAwait()
        return rsp.bodyAsString().length
    }

    private fun getUrlSize(host: String, uri: String = "/"): Int {
        val client = webClient.get()
        val req = client.get(host, uri)
        val rsp = Future.await(req.send())
        return rsp.bodyAsString().length
    }

    @GetMapping("/hi")
    fun hi(): String {
        logger.info("vertex ctx=${VertexVerticle.id()}")
//        Thread.sleep(100L)
        logger.info("baidu size: ${getUrlSize("www.baidu.com")}")
        logger.info("vertex ctx=${VertexVerticle.id()}")
        return "Hello, World!"
    }

    @GetMapping("/hello")
    suspend fun hello(): String {
        logger.info("vertex ctx=${VertexVerticle.id()}")
//        delay(100L)
        logger.info("baidu size: ${getUrlSizeAwait("www.baidu.com")}")
        logger.info("vertex ctx=${VertexVerticle.id()}")
        return "Hello, World!"
    }

    @GetMapping("/verticle")
    fun verticle() = verticleScope {
        logger.info("vertex before ctx=${VertexVerticle.id()}")
        logger.info("baidu size: ${getUrlSizeAwait("www.baidu.com")}")
        logger.info("vertex after ctx=${VertexVerticle.id()}")
        delay(1 * 300L)
        logger.info("vertex delayed ctx=${VertexVerticle.id()}")
        "Hello, World!"
    }

    @Bean
    fun vertexCloser(vertx: Vertx): VertexCloser {
        return object : VertexCloser {
            override fun close() {
                logger.info("close vertex")
                vertx.close()
            }
        }
    }

//    @Bean
//    fun redisConnectionFactory(): RedisConnectionFactory {
//        return LettuceConnectionFactory()
//    }
}

fun main(args: Array<String>) {
    runApplication<VertexApplication>(*args)
}
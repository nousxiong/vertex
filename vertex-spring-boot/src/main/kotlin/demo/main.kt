package demo

import io.vertex.autoconfigure.web.server.VertexServerVerticle
import io.vertex.utils.verticleScope
import io.vertx.core.Vertx
import io.vertx.ext.web.client.WebClient
import io.vertx.kotlin.coroutines.await
import kotlinx.coroutines.delay
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController


/**
 * Created by xiongxl in 2023/6/16
 */
@SpringBootApplication
@RestController
class VertexApplication(private val vertx: Vertx) {
    companion object {
        val logger: Logger = LoggerFactory.getLogger(VertexApplication::class.java)
    }

    private suspend fun getUrlSize(host: String, uri: String = "/"): Int {
        val client = WebClient.create(vertx)
        try {
            val req = client.get(host, uri)
            val rsp = req.send().await()
            return rsp.bodyAsString().length
        } finally {
            client.close()
        }
    }

    @GetMapping("/hello")
    suspend fun hello(): String {
        logger.info("vertex ctx=${VertexServerVerticle.id()}")
//        delay(100L)
        logger.info("baidu size: ${getUrlSize("www.baidu.com")}")
        logger.info("vertex ctx=${VertexServerVerticle.id()}")
        return "Hello, World!"
    }

    @GetMapping("/verticle")
    fun verticle() = verticleScope {
        logger.info("vertex before ctx=${VertexServerVerticle.id()}")
        logger.info("baidu size: ${getUrlSize("www.baidu.com")}")
        logger.info("vertex after ctx=${VertexServerVerticle.id()}")
        delay(5 * 1000L)
        logger.info("vertex delayed ctx=${VertexServerVerticle.id()}")
        "Hello, World!"
    }

//    @Bean
//    fun handlerAdapter(): WebSocketHandlerAdapter {
//        return WebSocketHandlerAdapter(webSocketService())
//    }
//
//    @Bean
//    fun webSocketService(): WebSocketService {
//        return HandshakeWebSocketService(VertexRequestUpgradeStrategy(300, 300))
//    }
}

fun main(args: Array<String>) {
    runApplication<VertexApplication>(*args)
}
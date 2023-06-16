package demo

import io.vertex.autoconfigure.web.server.VertexRequestUpgradeStrategy
import io.vertex.autoconfigure.web.server.VertexServerVerticle
import io.vertx.core.Vertx
import io.vertx.ext.web.client.WebClient
import io.vertx.kotlin.coroutines.await
import kotlinx.coroutines.delay
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.reactive.socket.server.WebSocketService
import org.springframework.web.reactive.socket.server.support.HandshakeWebSocketService
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter


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
        logger.info("vertex ctx=${Vertx.currentContext().get<Int>(VertexServerVerticle.CONTEXT_ID)}")
//        delay(100L)
        logger.info("baidu size: ${getUrlSize("www.baidu.com")}")
        logger.info("vertex ctx=${Vertx.currentContext().get<Int>(VertexServerVerticle.CONTEXT_ID)}")
        return "Hello, World!"
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
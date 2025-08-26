@file:Suppress("SpringBootApplicationProperties")

package io.vertex.autoconfigure.web.client

import io.vertex.autoconfigure.core.VertexAutoConfiguration
import io.vertex.autoconfigure.web.server.VertexServerAutoConfiguration
import io.vertx.core.Vertx
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactor.asFlux
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.HandlerMapping
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping
import org.springframework.web.reactive.socket.WebSocketHandler
import org.springframework.web.reactive.socket.WebSocketMessage
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import java.net.URI
import java.time.Duration
import kotlin.test.assertEquals


@SpringBootTest(classes = [ // 手动指定是为了精确控制；不指定的话，让springboot自己扫描也可以
    VertexAutoConfiguration::class,
    VertexServerAutoConfiguration::class,
    VertexClientAutoConfiguration::class,
    TestApplication::class, // 如果没有这个，auto-configure不生效，无法识别Reactive程序，从而报错
    VertexWebSocketClientTest.TestConfiguration::class,
    ],
    value = [
        "vertex.http.client.enabled=true",
        "vertex.http.client.websocket.enabled=true",
    ],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
class VertexWebSocketClientTest {
    companion object {
        private val logger = LoggerFactory.getLogger(VertexWebSocketClientTest::class.java)
        const val ECHO_URL = "/echo"
    }
    @LocalServerPort
    private lateinit var port: String
    @Autowired
    private lateinit var webSocketClient: VertexWebSocketClient

    @Configuration(proxyBeanMethods = false)
    class TestConfiguration {
        @Bean
        fun handlerMapping(): HandlerMapping {
            val handlerMapping = SimpleUrlHandlerMapping(mapOf(ECHO_URL to WebSocketHandler {
                echoHandler(it)
            }))
            handlerMapping.order = -1
            return handlerMapping
        }

        private fun echoHandler(session: WebSocketSession): Mono<Void> {
            val messages = session.receive()
                .filter { it.type == WebSocketMessage.Type.TEXT }
                .asFlow()
                .map {
                    val payload = it.payloadAsText
                    session.textMessage(payload)
                }
                .asFlux(Vertx.currentContext().dispatcher())
            return session.send(messages)
        }
    }

    @Test
    fun `should echo hello`() {
        webSocketClient.execute(URI("ws://localhost:$port$ECHO_URL")) { session ->
            session.send(session.textMessage("hello").toMono())
                .then(session.receive().next().map { it.payloadAsText })
                .doOnNext {
                    assertEquals("hello", it)
                    logger.info(it)
                }
                .then(session.close())
        }.block(Duration.ofMillis(5000))
    }

}
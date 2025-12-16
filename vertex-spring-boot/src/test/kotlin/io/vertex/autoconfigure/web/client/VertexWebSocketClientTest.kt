@file:Suppress("SpringBootApplicationProperties")

package io.vertex.autoconfigure.web.client

import io.vertex.autoconfigure.core.VertexAutoConfiguration
import io.vertex.autoconfigure.web.server.VertexServerAutoConfiguration
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
import reactor.core.publisher.Flux
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
        "vertex.http.server.deployments.threadingModel=EVENT_LOOP",
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
            }), -1)
            return handlerMapping
        }

        private fun echoHandler(session: WebSocketSession): Mono<Void> {
            logger.info("Server session: ${session.id}")
            val upFlux = session.receive().filter { it.type == WebSocketMessage.Type.TEXT }
            // 模拟会话建立立刻下行消息
            val created = Flux.fromIterable(listOf(
                session.textMessage("Welcome,"),
                session.textMessage("Welcome!")
            ))

            // 使用 concatMap 或 flatMap 来处理
            val messages = upFlux.concatMap {
                val payload = it.payloadAsText
                logger.info("Server Received: $payload")
                if (payload.isEmpty()) {
                    // 上行空，则不下行
                    return@concatMap Mono.empty()
                }

                when {
                    payload.endsWith("x2") -> {
                        val downMsg = payload.removeSuffix("x2")
                        if (downMsg.isEmpty()) {
                            // 如果仅有x2后缀，直接不下行
                            Mono.empty()
                        } else {
                            // 返回2个echo
                            Flux.just(
                                session.textMessage(downMsg),
                                session.textMessage(downMsg)
                            )
                        }
                    }
                    else -> {
                        // 通用处理
                        Flux.just(session.textMessage(payload))
                    }
                }
            }
//            val messages = upFlux
//                .asFlow()
//                .map {
//                    val payload = it.payloadAsText
//                    logger.info("Server Received: $payload")
//                    session.textMessage(payload)
//                }
//                .asFlux(Vertx.currentContext().dispatcher())
            return session.send(Flux.concat(created, messages))
        }
    }

    @Test
    fun `should echo two hello and bye`() {
        webSocketClient.execute(URI("ws://localhost:$port$ECHO_URL")) { session ->
            val downFlux = session.receive()
            downFlux.next().map { it.payloadAsText }
                .doOnNext {
                    assertEquals("Welcome,", it)
                    logger.info("Client Received welcome: $it")
                }
                .then(downFlux.next().map { it.payloadAsText })
                .doOnNext {
                    assertEquals("Welcome!", it)
                    logger.info("Client Received welcome: $it")
                }
                .then(session.send(session.textMessage("hellox2").toMono()))
//            session.send(session.textMessage("hellox2").toMono())
                .then(downFlux.next().map { it.payloadAsText })
                .doOnNext {
                    assertEquals("hello", it)
                    logger.info("Client Received echo: $it")
                }
                .then(downFlux.next().map { it.payloadAsText })
                .doOnNext {
                    assertEquals("hello", it)
                    logger.info("Client Received echo: $it")
                }
                .then(session.send(session.textMessage("x2").toMono()))
                .then(session.send(session.textMessage("bye").toMono()))
                .then(downFlux.next().map { it.payloadAsText })
                .doOnNext {
                    assertEquals("bye", it)
                    logger.info("Client Received echo: $it")
                }
                .then(session.close())
        }.block(Duration.ofMillis(5000))
    }

    @Test
    fun `should echo hello and bye`() {
        webSocketClient.execute(URI("ws://localhost:$port$ECHO_URL")) { session ->
            val downFlux = session.receive()
            downFlux.next().map { it.payloadAsText }
                .doOnNext {
                    assertEquals("Welcome,", it)
                    logger.info("Client Received welcome: $it")
                }
                .then(downFlux.next().map { it.payloadAsText })
                .doOnNext {
                    assertEquals("Welcome!", it)
                    logger.info("Client Received welcome: $it")
                }
                .then(session.send(session.textMessage("hello").toMono()))
//            session.send(session.textMessage("hello").toMono())
                .then(downFlux.next().map { it.payloadAsText })
                .doOnNext {
                    assertEquals("hello", it)
                    logger.info("Client Received echo: $it")
                }
                .then(session.send(session.textMessage("bye").toMono()))
                .then(downFlux.next().map { it.payloadAsText })
                .doOnNext {
                    assertEquals("bye", it)
                    logger.info("Client Received echo: $it")
                }
                .then(session.close())
        }.block(Duration.ofMillis(5000))
    }

}
package io.vertex.autoconfigure.web.client

import io.vertex.autoconfigure.core.VertexAutoConfiguration
import io.vertex.autoconfigure.web.server.VertexServerAutoConfiguration
import io.vertex.autoconfigure.web.server.VertexServerVerticle
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.reactive.function.client.WebClient

/**
 * Created by xiongxl in 2023/6/22
 */
@SpringBootTest(classes = [
    VertexAutoConfiguration::class,
    VertexServerAutoConfiguration::class,
    VertexClientAutoConfiguration::class,
    VertexWebClientTest.TestApplication::class,
],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
class VertexWebClientTest {
    companion object {
        private val logger = LoggerFactory.getLogger(VertexWebSocketClientTest::class.java)
        const val URL = "/hello"
    }
    @LocalServerPort
    private lateinit var port: String
    @Autowired
    private lateinit var webClientBuilder: WebClient.Builder

    @RestController
    @SpringBootApplication
    class TestApplication {
        @GetMapping(URL)
        suspend fun hello(): String {
            logger.info("vertex ctx=${VertexServerVerticle.getIdOrNull()}")
            return "hello"
        }
    }

    @Test
    fun `should return hello`() {
        val webClient = webClientBuilder.baseUrl("http://localhost:$port").build()
        webClient.get().uri(URL).retrieve().bodyToMono(String::class.java).doOnNext {
            logger.info(it)
        }.block()
    }
}
package io.vertex.autoconfigure.web.server.actuator

import io.vertex.autoconfigure.core.VertexVerticle
import io.vertx.core.json.JsonObject
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.reactive.function.client.WebClient
import kotlin.test.assertEquals

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = ["management.server.port=8081"]
)
class VertexActuatorTest {
    companion object {
        private val logger = LoggerFactory.getLogger(VertexActuatorTest::class.java)
    }
    @LocalServerPort
    private lateinit var port: String
    @Autowired
    private lateinit var webClientBuilder: WebClient.Builder

    @RestController
    class TestController {
        @GetMapping("/hello")
        suspend fun hello(): String {
            logger.info("vertex ctx=${VertexVerticle.idOrNull()}")
            return "hello"
        }
    }

    @Test
    fun `should get actuator health with modified port`() {
        val webClient = webClientBuilder.baseUrl("http://localhost:8081").build()
        webClient.get().uri("/actuator/health").retrieve().bodyToMono(String::class.java).doOnNext {
            val bodyJson = JsonObject(it)
            assertEquals("UP", bodyJson.getString("status"))
        }.block()
    }
}
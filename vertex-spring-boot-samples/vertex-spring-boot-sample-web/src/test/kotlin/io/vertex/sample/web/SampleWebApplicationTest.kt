package io.vertex.sample.web

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.web.reactive.server.WebTestClient

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = ["management.server.port=8081"]
)
class SampleWebApplicationTest {
    @LocalServerPort
    private lateinit var port: String
    @Autowired
    private lateinit var webTestClient: WebTestClient

    @Test
    fun `should response 'Hello, World!'`() {
        webTestClient.get()
            .uri("/hello")
            .exchange()
            .expectBody(String::class.java)
            .isEqualTo("Hello, World!")
    }
}
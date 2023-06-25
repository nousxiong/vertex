package io.vertex.autoconfigure.web.test

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.reactive.server.WebTestClient

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class DefaultWebTestClientTest {
    @Autowired
    private lateinit var webTestClient: WebTestClient

    @Test
    fun `test access to http resource`() {
        webTestClient.get()
            .exchange()
            .expectBody(String::class.java)
            .isEqualTo("test")
    }

}
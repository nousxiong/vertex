package io.vertex.autoconfigure.web.server.properties

import io.vertex.autoconfigure.web.server.VertexServerAutoConfiguration
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import kotlin.test.assertEquals

@SpringBootTest(
    classes = [VertexServerAutoConfiguration::class],
    properties = [
        "vertex.http.server.port=10010"
    ]
)
class HttpServerPropertiesTest {
    @Autowired
    private lateinit var properties: HttpServerProperties

    @Test
    fun `should injected properties and port == 10010`() {
        assertEquals(10010, properties.port)
    }
}
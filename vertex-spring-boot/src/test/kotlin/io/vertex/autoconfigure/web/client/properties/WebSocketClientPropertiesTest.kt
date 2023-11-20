package io.vertex.autoconfigure.web.client.properties

import io.vertex.autoconfigure.core.VertexAutoConfiguration
import io.vertex.autoconfigure.web.client.VertexClientAutoConfiguration
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import kotlin.test.assertEquals

@SpringBootTest(
    classes = [
        VertexAutoConfiguration::class,
        VertexClientAutoConfiguration::class],
    properties = [
        "vertex.websocket.client.defaultPort=10010"
    ]
)
class WebSocketClientPropertiesTest{
    @Autowired
    private lateinit var properties: WebSocketClientProperties

    @Test
    fun `should injected properties and port == 10010`() {
        assertEquals(10010, properties.defaultPort)
    }
}
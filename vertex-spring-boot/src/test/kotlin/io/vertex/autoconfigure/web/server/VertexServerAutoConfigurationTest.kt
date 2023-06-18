package io.vertex.autoconfigure.web.server

import io.vertex.autoconfigure.core.VertexAutoConfiguration
import org.junit.jupiter.api.Test

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import kotlin.test.assertNotNull

@SpringBootTest(classes = [
    VertexAutoConfiguration::class,
    VertexServerAutoConfiguration::class
])
class VertexServerAutoConfigurationTest {
    @Autowired
    private var vertexReactiveWebServerFactory: VertexReactiveWebServerFactory? = null

    @Test
    fun `should injected vertexReactiveWebServerFactory`() {
        assertNotNull(vertexReactiveWebServerFactory)
    }
}
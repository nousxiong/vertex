package io.vertex.autoconfigure.core

import io.vertx.core.Vertx
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import kotlin.test.assertNotNull

@SpringBootTest(classes = [VertexAutoConfiguration::class])
class VertexAutoConfigurationTest {

    @Autowired
    private var vertx: Vertx? = null

    @Test
    fun `should inject vertx`() {
        assertNotNull(vertx)
    }
}
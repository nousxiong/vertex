package io.vertex.autoconfigure.core.properties

import io.vertex.autoconfigure.core.VertexAutoConfiguration
import org.junit.jupiter.api.Test
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import kotlin.test.assertEquals

@SpringBootTest(
    classes = [VertexAutoConfiguration::class],
    properties = [
        "vertx.event-loop-pool-size=2"
    ]
)
class VertxPropertiesTest {
    companion object {
        val logger: Logger = LoggerFactory.getLogger(VertxPropertiesTest::class.java)
    }

    @Autowired
    private lateinit var properties: VertxProperties

    @Test
    fun `should properties be`() {
        logger.info("properties: ${properties.toVertxOptions()}")
        assertEquals(2, properties.eventLoopPoolSize)
    }
}
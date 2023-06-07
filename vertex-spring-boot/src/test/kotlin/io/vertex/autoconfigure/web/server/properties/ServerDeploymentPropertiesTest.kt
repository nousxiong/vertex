package io.vertex.autoconfigure.web.server.properties

import io.vertex.autoconfigure.web.server.VertexServerAutoConfiguration
import org.junit.jupiter.api.Test
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import kotlin.test.assertEquals

@SpringBootTest(
    classes = [VertexServerAutoConfiguration::class],
    properties = [
        "vertex.http.server.deployments.instances=2"
    ]
)
class ServerDeploymentPropertiesTest {
    companion object {
        val logger: Logger = LoggerFactory.getLogger(ServerDeploymentPropertiesTest::class.java)
    }
    @Autowired
    private lateinit var properties: ServerDeploymentProperties

    @Test
    fun `should injected properties`() {
        logger.info("properties: ${properties.toJson()}")
        assertEquals(2, properties.instances)
    }
}
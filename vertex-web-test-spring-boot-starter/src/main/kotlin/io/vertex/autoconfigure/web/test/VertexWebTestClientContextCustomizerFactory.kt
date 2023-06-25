package io.vertex.autoconfigure.web.test

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.annotation.AnnotatedElementUtils
import org.springframework.test.context.ContextConfigurationAttributes
import org.springframework.test.context.ContextCustomizer
import org.springframework.test.context.ContextCustomizerFactory

/**
 * Created by xiongxl in 2023/6/25
 */
class VertexWebTestClientContextCustomizerFactory : ContextCustomizerFactory {
    override fun createContextCustomizer(
        testClass: Class<*>,
        configAttributes: MutableList<ContextConfigurationAttributes>
    ): ContextCustomizer? {
        return if (isEmbeddedSpringBootTest(testClass)) {
            VertexWebTestClientContextCustomizer()
        } else null
    }

    private fun isEmbeddedSpringBootTest(testClass: Class<*>): Boolean {
        val annotation = AnnotatedElementUtils.getMergedAnnotation(
            testClass,
            SpringBootTest::class.java
        )
        return annotation != null && annotation.webEnvironment.isEmbedded
    }
}
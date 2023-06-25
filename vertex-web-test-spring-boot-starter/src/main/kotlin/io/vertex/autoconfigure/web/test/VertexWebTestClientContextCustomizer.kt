package io.vertex.autoconfigure.web.test

import org.springframework.beans.factory.config.BeanDefinition
import org.springframework.beans.factory.support.BeanDefinitionRegistry
import org.springframework.beans.factory.support.RootBeanDefinition
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.test.context.ContextCustomizer
import org.springframework.test.context.MergedContextConfiguration

/**
 * Created by xiongxl in 2023/6/25
 */
class VertexWebTestClientContextCustomizer : ContextCustomizer {
    override fun customizeContext(context: ConfigurableApplicationContext, mergedConfig: MergedContextConfiguration) {
        val beanFactory = context.beanFactory

        if (beanFactory is BeanDefinitionRegistry) {
            registerWebTestClient((beanFactory as BeanDefinitionRegistry))
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        return javaClass == other?.javaClass
    }

    override fun hashCode(): Int {
        return javaClass.hashCode()
    }

    private fun registerWebTestClient(registry: BeanDefinitionRegistry) {
        val definition = RootBeanDefinition(VertexWebTestClientRegistrar::class.java)
        definition.role = BeanDefinition.ROLE_INFRASTRUCTURE
        registry.registerBeanDefinition(VertexWebTestClientRegistrar::class.java.name, definition)
    }
}
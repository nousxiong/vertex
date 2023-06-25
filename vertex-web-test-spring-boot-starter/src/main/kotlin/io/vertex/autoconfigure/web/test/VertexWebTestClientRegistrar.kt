package io.vertex.autoconfigure.web.test

import org.springframework.beans.factory.BeanFactory
import org.springframework.beans.factory.BeanFactoryAware
import org.springframework.beans.factory.BeanFactoryUtils
import org.springframework.beans.factory.ListableBeanFactory
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory
import org.springframework.beans.factory.support.BeanDefinitionRegistry
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor
import org.springframework.beans.factory.support.RootBeanDefinition
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.core.Ordered
import org.springframework.test.web.reactive.server.WebTestClient

/**
 * Created by xiongxl in 2023/6/25
 */
class VertexWebTestClientRegistrar :
    BeanDefinitionRegistryPostProcessor,
    Ordered,
    BeanFactoryAware,
    ApplicationContextAware {
    private lateinit var beanFactory: BeanFactory
    private lateinit var applicationContext: ApplicationContext

    override fun postProcessBeanFactory(beanFactory: ConfigurableListableBeanFactory) = Unit

    override fun postProcessBeanDefinitionRegistry(registry: BeanDefinitionRegistry) {
        if (!isWebTestClientRegistered()) {
            val supplier = VertexWebTestClientSupplier(applicationContext)
            val definition = RootBeanDefinition(WebTestClient::class.java, supplier)
            definition.isLazyInit = true
            registry.registerBeanDefinition(WebTestClient::class.java.name, definition)
        }
    }

    override fun getOrder(): Int {
        return Ordered.LOWEST_PRECEDENCE
    }

    override fun setBeanFactory(beanFactory: BeanFactory) {
        this.beanFactory = beanFactory
    }

    override fun setApplicationContext(applicationContext: ApplicationContext) {
        this.applicationContext = applicationContext
    }

    private fun isWebTestClientRegistered(): Boolean {
        return BeanFactoryUtils.beanNamesForTypeIncludingAncestors(
            (beanFactory as ListableBeanFactory),
            WebTestClient::class.java, false, false
        ).isNotEmpty()
    }
}
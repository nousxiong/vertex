package io.vertex.autoconfigure.web.server

import io.vertex.autoconfigure.web.server.properties.*
import org.springframework.boot.web.server.WebServerFactoryCustomizer
import org.springframework.core.Ordered

/**
 * Created by xiongxl in 2023/6/8
 * 被[org.springframework.boot.web.server.WebServerFactoryCustomizerBeanPostProcessor.getWebServerFactoryCustomizerBeans]获取此bean
 */
class VertexReactiveWebServerFactoryCustomizer(
    private val userDefinedCustomizers: Set<HttpServerOptionsCustomizer>?
) : WebServerFactoryCustomizer<VertexReactiveWebServerFactory>, Ordered {
    /**
     * 被[org.springframework.boot.web.server.WebServerFactoryCustomizerBeanPostProcessor.postProcessBeforeInitialization]调用
     */
    override fun customize(factory: VertexReactiveWebServerFactory) {
        factory.registerHttpServerOptionsCustomizer(PortCustomizer(factory))
        factory.registerHttpServerOptionsCustomizer(AddressCustomizer(factory))
        factory.registerHttpServerOptionsCustomizer(SslCustomizer(factory))
        factory.registerHttpServerOptionsCustomizer(CompressionCustomizer(factory))

        userDefinedCustomizers?.forEach(factory::registerHttpServerOptionsCustomizer)
    }

    override fun getOrder(): Int = 1 // Run after ReactiveWebServerFactoryCustomizer
}
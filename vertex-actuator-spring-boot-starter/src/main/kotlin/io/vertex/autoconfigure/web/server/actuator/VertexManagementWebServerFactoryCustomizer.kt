package io.vertex.autoconfigure.web.server.actuator

import io.vertex.autoconfigure.web.server.VertexReactiveWebServerFactoryCustomizer
import org.springframework.beans.factory.ListableBeanFactory
import org.springframework.boot.actuate.autoconfigure.web.server.ManagementWebServerFactoryCustomizer
import org.springframework.boot.autoconfigure.web.reactive.ReactiveWebServerFactoryCustomizer
import org.springframework.boot.web.reactive.server.ConfigurableReactiveWebServerFactory

/**
 * Created by xiongxl in 2023/6/25
 */
class VertexManagementWebServerFactoryCustomizer(beanFactory: ListableBeanFactory) :
    ManagementWebServerFactoryCustomizer<ConfigurableReactiveWebServerFactory>(
        beanFactory,
        ReactiveWebServerFactoryCustomizer::class.java,
        VertexReactiveWebServerFactoryCustomizer::class.java,
    )
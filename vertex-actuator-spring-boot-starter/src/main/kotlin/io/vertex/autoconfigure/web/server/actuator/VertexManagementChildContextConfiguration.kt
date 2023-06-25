package io.vertex.autoconfigure.web.server.actuator

import org.springframework.beans.factory.ListableBeanFactory
import org.springframework.boot.actuate.autoconfigure.web.ManagementContextConfiguration
import org.springframework.boot.actuate.autoconfigure.web.ManagementContextType
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.context.annotation.Bean

/**
 * Created by xiongxl in 2023/6/25
 */
@ManagementContextConfiguration(ManagementContextType.CHILD)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
class VertexManagementChildContextConfiguration {
    @Bean
    fun vertexManagementWebServerFactoryCustomizer(beanFactory: ListableBeanFactory): VertexManagementWebServerFactoryCustomizer {
        return VertexManagementWebServerFactoryCustomizer(beanFactory)
    }
}
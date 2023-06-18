package io.vertex.autoconfigure.web.server.properties

import io.vertx.core.DeploymentOptions
import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * Created by xiongxl in 2023/6/7
 */
@ConfigurationProperties(prefix = ServerDeploymentProperties.PROPERTIES_PREFIX)
class ServerDeploymentProperties : DeploymentOptions() {
    companion object {
        const val PROPERTIES_PREFIX = "vertex.http.server.deployments"
    }
}
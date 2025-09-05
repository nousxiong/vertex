package io.vertex.autoconfigure.web.server.properties

import io.vertx.core.DeploymentOptions
import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

/**
 * Created by xiongxl in 2023/6/7
 */
@ConfigurationProperties(prefix = ServerDeploymentProperties.PROPERTIES_PREFIX)
class ServerDeploymentProperties : DeploymentOptions() {
    companion object {
        const val PROPERTIES_PREFIX = "vertex.http.server.deployments"
    }
    var gracefulShutdownPreWaitMillis: Duration = Duration.ofSeconds(0L)
    var gracefulShutdownWaitMillis: Duration = Duration.ofSeconds(20L)
}
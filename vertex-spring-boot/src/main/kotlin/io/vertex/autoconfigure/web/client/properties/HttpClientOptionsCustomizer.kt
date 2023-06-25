package io.vertex.autoconfigure.web.client.properties

import io.vertx.core.http.HttpClientOptions
import java.util.function.Function

/**
 * Created by xiongxl in 2023/6/20
 */
@FunctionalInterface
interface HttpClientOptionsCustomizer : Function<HttpClientOptions, HttpClientOptions>
package io.vertex.utils

import io.vertex.autoconfigure.web.server.VertexServerVerticle
import io.vertx.core.Vertx
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.vertex.mono
import reactor.core.publisher.Mono
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * Created by xiongxl in 2023/6/28
 */
fun <T> verticleScope(
    context: CoroutineContext = EmptyCoroutineContext,
    block: suspend CoroutineScope.() -> T?
): Mono<T> {
    return VertexServerVerticle.coroutineScope().mono(
        Vertx.currentContext().dispatcher() + context,
        block
    )
}
package kotlinx.coroutines.vertex

import kotlinx.coroutines.*
import kotlinx.coroutines.reactor.ReactorContext
import kotlinx.coroutines.reactor.asCoroutineContext
import reactor.core.Disposable
import reactor.core.publisher.Hooks
import reactor.core.publisher.Mono
import reactor.core.publisher.MonoSink
import reactor.util.context.ContextView
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * Created by xiongxl in 2023/6/28
 * 替换kotlinx.coroutines.reactor.Monokt，原因：
 *
 * 1、spring-webflux暂时不支持构建协程时自定义CoroutineContext：
 *  https://github.com/spring-projects/spring-framework/issues/27522
 *  等待spring-webflux 6.1.x milestone完成：
 *  https://github.com/spring-projects/spring-framework/milestone/300
 *
 * 2、因为1，退一步使用kotlinx.coroutines.reactor.mono来手动创建协程从而可以设置CoroutineContext，
 *  但是其不支持CoroutineContext里加上job，无法让MonoCoroutine成为Vertx的CoroutineVerticle的子Job
 *  （原因是MonoCoroutine要自己管理生命周期，不想让别人可能调用Job的cancel和join等方法）
 *
 * 3、另外，为了实现Vertx的graceful shutdown，需要等待所有的CoroutineVerticle的子协程（子Job）完成
 *
 * P.S1：2中mono自己的生命周期问题，我们保证不手动调用Job的cancel和join等方法
 * P.S2：此方法等待spring-webflux支持构建协程时自定义CoroutineContext后会废弃
 */

/**
 * Reform kotlinx.coroutines.reactor.Monokt
 */
fun <T> CoroutineScope.mono(
    context: CoroutineContext = EmptyCoroutineContext,
    block: suspend CoroutineScope.() -> T?
): Mono<T> {
    require(context[Job] === null) { "Mono context cannot contain job in it." +
            "Its lifecycle should be managed via Disposable handle. Had $context" }
    return monoInternal(this, context, block)
}

/**
 * Copy from kotlinx.coroutines.reactor.Monokt
 */
@OptIn(InternalCoroutinesApi::class)
class MonoCoroutine<in T>(
    parentContext: CoroutineContext,
    private val sink: MonoSink<T>
) : AbstractCoroutine<T>(parentContext, true, true), Disposable {
    @Volatile
    private var disposed = false

    override fun onCompleted(value: T) {
        if (value == null) sink.success() else sink.success(value)
    }

    override fun onCancelled(cause: Throwable, handled: Boolean) {
        if (getCancellationException() !== cause || !disposed) {
            try {
                /** If [sink] turns out to already be in a terminal state, this exception will be passed through the
                 * [Hooks.onOperatorError] hook, which is the way to signal undeliverable exceptions in Reactor. */
                sink.error(cause)
            } catch (e: Throwable) {
                // In case of improper error implementation or fatal exceptions
                cause.addSuppressed(e)
                handleCoroutineException(context, cause)
            }
        }
    }

    override fun dispose() {
        disposed = true
        cancel()
    }

    override fun isDisposed(): Boolean = disposed
}

/**
 * Copy from kotlinx.coroutines.reactor.Monokt
 */
@OptIn(ExperimentalCoroutinesApi::class)
private fun <T> monoInternal(
    scope: CoroutineScope, // support for legacy mono in scope
    context: CoroutineContext,
    block: suspend CoroutineScope.() -> T?
): Mono<T> = Mono.create { sink ->
    val reactorContext = context.extendReactorContext(sink.contextView())
    val newContext = scope.newCoroutineContext(context + reactorContext)
    val coroutine = MonoCoroutine(newContext, sink)
    sink.onDispose(coroutine)
    coroutine.start(CoroutineStart.DEFAULT, coroutine, block)
}

/**
 * Copy from kotlinx.coroutines.reactor.ReactorContextKt
 */
private fun CoroutineContext.extendReactorContext(extensions: ContextView): CoroutineContext =
    (this[ReactorContext]?.context?.putAll(extensions) ?: extensions).asCoroutineContext()
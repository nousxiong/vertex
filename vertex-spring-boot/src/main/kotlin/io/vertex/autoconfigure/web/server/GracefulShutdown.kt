package io.vertex.autoconfigure.web.server

import kotlinx.coroutines.CompletableJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.job
import org.springframework.boot.web.server.GracefulShutdownCallback
import org.springframework.boot.web.server.GracefulShutdownResult
import java.time.Clock
import java.time.ZoneId
import java.util.concurrent.atomic.AtomicIntegerArray

/**
 * Created by xiongxl in 2023/6/8
 * @param minRemainJob 剩余最小job数量就退出
 * @param periodMillis 循环检查时间（毫秒）
 * @param timeoutMillis 等待超时时间（毫秒）
 */
class GracefulShutdown(
    verticleInstances: Int,
    private val minRemainJob: Int = 1, // 当在start或stop里调用时这个值必须是1
    private val periodMillis: Long = 100L,
    private val timeoutMillis: Long = 20 * 1000L,
) {
    @Volatile
    private var aborted = false
    private val shutDownResults = AtomicIntegerArray(verticleInstances)
    fun abort() {
        aborted = true
    }

    fun shutDownGracefully(callback: GracefulShutdownCallback) {
        val size = shutDownResults.length()
        val result = if ((0 until size).sumOf { shutDownResults[it] } == size) {
            GracefulShutdownResult.IDLE
        } else {
            GracefulShutdownResult.REQUESTS_ACTIVE
        }
        callback.shutdownComplete(result)
    }

    /**
     * @return true未超时结束，false超时
     */
    suspend fun awaitCompletion(verticle: VertexServerVerticle): Boolean {
        require(minRemainJob >= 0)
        require(periodMillis > 0L)
        val job = verticle.coroutineContext.job as CompletableJob
        shutDownResults[verticle.id] = 1
        if (job.isCompleted) return true

        val clock = Clock.tickMillis(ZoneId.systemDefault())
        val bt = clock.millis()
        job.complete()
        while (job.children.count() > minRemainJob) { //
            if (aborted || timeoutMillis > 0L && clock.millis() - bt >= timeoutMillis) {
                return false
            }
            delay(periodMillis)
        }
        shutDownResults[verticle.id] = 1
        return true
    }
}
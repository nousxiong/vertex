package io.vertex.utils

import io.vertx.core.streams.WriteStream
import org.reactivestreams.Subscription
import org.springframework.util.ObjectUtils
import reactor.core.publisher.BaseSubscriber
import reactor.core.publisher.MonoSink
import reactor.core.publisher.SignalType
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong
import java.util.function.BiConsumer

/**
 * Created by xiongxl in 2023/6/11
 */
class WriteStreamSubscriber<T : WriteStream<*>, U : Any>(
    private val writeStream: T,
    private val nextHandler: BiConsumer<T, U>,
    private val endHook: MonoSink<Void>,
    private val requestLimit: Long,
) : BaseSubscriber<U>() {
    companion object {
        private val logger = org.slf4j.LoggerFactory.getLogger(WriteStreamSubscriber::class.java)
    }

    private val pendingCount = AtomicLong()
    private val isActive = AtomicBoolean(false)
    private val logPrefix: String = "[${ObjectUtils.getIdentityHexString(writeStream)}] "

    init {
        writeStream.exceptionHandler {
            cancel()
        }
        writeStream.drainHandler {
            logger.debug("$logPrefix drain", logPrefix)
            requestIfNotFull()
        }
    }

    override fun hookOnSubscribe(subscription: Subscription) {
        logger.debug("${logPrefix}${writeStream} subscribed")
        isActive.set(true)
        requestIfNotFull()
    }

    override fun hookOnNext(value: U) {
        logger.debug("${logPrefix}Next: $value")
        nextHandler.accept(writeStream, value)
        pendingCount.decrementAndGet()
        requestIfNotFull()
    }

    override fun hookOnComplete() {
        logger.debug("${logPrefix}Completed")
        endHook.success()
    }

    override fun hookOnCancel() {
        logger.debug("${logPrefix}Canceled")
        endHook.success()
    }

    override fun hookOnError(throwable: Throwable) {
        logger.debug("${logPrefix}Error: $throwable")
        endHook.error(throwable)
    }

    override fun hookFinally(type: SignalType) {
        isActive.set(false)
    }

    private fun requestIfNotFull() {
        if (isActive.get() && !writeStream.writeQueueFull() && pendingCount.get() < requestLimit) {
            logger.debug("${logPrefix}Requesting more data pendingCount=${pendingCount.get()} requestLimit=${requestLimit}")
            request(requestLimit - pendingCount.getAndSet(requestLimit))
        }
    }
}
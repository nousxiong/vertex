package io.vertex.utils

import io.vertx.core.streams.ReadStream
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.util.ObjectUtils
import reactor.core.publisher.Flux
import reactor.core.publisher.FluxSink
import java.util.function.Function

/**
 * Created by xiongxl in 2023/6/9
 */
class ReadStreamFluxBuilder<T, R : Any>(
    private val readStream: ReadStream<T>,
    private val dataConverter: Function<T, R>,
) {
    companion object {
        private val logger: Logger = LoggerFactory.getLogger(ReadStreamFluxBuilder::class.java)
    }

    fun build(): Flux<R> {
        readStream.pause()
        return Flux.create { sink: FluxSink<R> ->
            val logPrefix = "[${ObjectUtils.getIdentityHexString(readStream)}] "
            readStream
                .handler { data: T ->
                    logger.debug("${logPrefix}Received '${data}'")
                    sink.next(dataConverter.apply(data))
                }
                .exceptionHandler { throwable: Throwable ->
                    logger.debug("${logPrefix}Received exception '${throwable}'")
                    sink.error(throwable)
                }
                .endHandler {
                    logger.debug("${logPrefix}Read stream ended")
                    sink.complete()
                }
            sink.onRequest { i: Long ->
                logger.debug("$logPrefix Fetching '${i}' entries from a read stream")
                readStream.fetch(i)
            }
        }
    }
}
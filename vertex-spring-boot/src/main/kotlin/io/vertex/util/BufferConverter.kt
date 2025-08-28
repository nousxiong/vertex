package io.vertex.util

import io.vertx.core.buffer.Buffer
import io.vertx.core.buffer.impl.BufferImpl
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.core.io.buffer.DefaultDataBufferFactory
import org.springframework.core.io.buffer.NettyDataBufferFactory
import java.nio.ByteBuffer

/**
 * Created by xiongxl in 2023/6/9
 * Xiongxl 20250828 [dataBufferFactory] 改为 [DefaultDataBufferFactory]
 *  NettyDataBufferFactory会导致某些其它库（例如spring.cloud）强制release NettyDataBuffer
 *  而vertx.Buffer返回的byteBuf（VertxHeapByteBuf或VertxUnsafeHeapByteBuf）其release固定返回false
 * org.springframework.cloud.gateway.support.ServerWebExchangeUtils#clearCachedRequestBody
 */
class BufferConverter(val dataBufferFactory: DefaultDataBufferFactory) {
    constructor() : this(DefaultDataBufferFactory())

    fun toDataBuffer(buffer: Buffer): DataBuffer {
        // 这里我们假设 buffer 是 BufferImpl 类型，直接引用其ByteBuf
        require(buffer is BufferImpl)
        val byteBuf = buffer.byteBuf()
        // vertx.Buffer返回的byteBuf（VertxHeapByteBuf或VertxUnsafeHeapByteBuf）必然使用array
        require(byteBuf.hasArray())
        return dataBufferFactory.wrap(
            ByteBuffer.wrap(
                byteBuf.array(),
                0,
                byteBuf.writerIndex()
            )
        )
    }

    fun toBuffer(dataBuffer: DataBuffer): Buffer {
        val byteBuf = NettyDataBufferFactory.toByteBuf(dataBuffer)
        return BufferImpl.buffer(byteBuf)
    }
}
package io.vertex.util

import io.netty.buffer.ByteBufAllocator
import io.vertx.core.buffer.Buffer
import io.vertx.core.buffer.impl.BufferImpl
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.core.io.buffer.NettyDataBufferFactory

/**
 * Created by xiongxl in 2023/6/9
 */
class BufferConverter(val dataBufferFactory: NettyDataBufferFactory) {
    constructor() : this(NettyDataBufferFactory(ByteBufAllocator.DEFAULT))

    fun toDataBuffer(buffer: Buffer): DataBuffer {
        // 这里我们假设 buffer 是 BufferImpl 类型，直接引用其ByteBuf
        require(buffer is BufferImpl)
        val byteBuf = buffer.byteBuf()
        return dataBufferFactory.wrap(byteBuf)
    }

    fun toBuffer(dataBuffer: DataBuffer): Buffer {
        val byteBuf = NettyDataBufferFactory.toByteBuf(dataBuffer)
        return BufferImpl.buffer(byteBuf)
    }
}
package io.vertex.utils

import io.netty.buffer.ByteBufAllocator
import io.vertx.core.buffer.Buffer
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.core.io.buffer.NettyDataBufferFactory

/**
 * Created by xiongxl in 2023/6/9
 */
class BufferConverter(val dataBufferFactory: NettyDataBufferFactory) {
    constructor() : this(NettyDataBufferFactory(ByteBufAllocator.DEFAULT))

    fun toDataBuffer(buffer: Buffer): DataBuffer {
        val byteBuf = buffer.byteBuf
        return dataBufferFactory.wrap(byteBuf)
    }

    fun toBuffer(dataBuffer: DataBuffer): Buffer {
        val byteBuf = NettyDataBufferFactory.toByteBuf(dataBuffer)
        return Buffer.buffer(byteBuf)
    }
}
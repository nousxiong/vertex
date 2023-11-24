package io.vertex.autoconfigure.core

/**
 * Created by xiongxl in 2023/7/5
 * @see org.springframework.beans.factory.support.DisposableBeanAdapter
 */
interface VertexCloser {
    fun close()
}
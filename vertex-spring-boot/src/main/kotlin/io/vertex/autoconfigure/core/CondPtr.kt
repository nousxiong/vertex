package io.vertex.autoconfigure.core

/**
 * Created by xiongxl in 2025/8/26
 */
class CondPtr<T>(private val obj: T, val cond: Boolean) {
    fun get(): T = obj
    fun getWithCond(): T? = if (cond) obj else null
}
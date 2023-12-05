package io.vertex.data.redisql

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

/**
 * Created by xiongxl in 2023/12/6
 */
class RedisqlKeyValueMap<K, V> : ConcurrentMap<K, V>  {
    override val size: Int = 0
    override val entries: MutableSet<MutableMap.MutableEntry<K, V>> = ConcurrentHashMap.newKeySet()
    override val keys: MutableSet<K> = ConcurrentHashMap.newKeySet()
    override val values: MutableCollection<V> = ConcurrentHashMap.newKeySet()
    companion object {
        val logger: Logger = LoggerFactory.getLogger(RedisqlKeyValueMap::class.java)
    }
    override fun put(key: K, value: V): V? {
        logger.info("RedisqlKeyValueMap put key<$key> value<$value>")
        return null
    }

    override fun clear() {
        TODO("Not yet implemented")
    }

    override fun remove(key: K): V? {
        TODO("Not yet implemented")
    }

    override fun isEmpty(): Boolean {
        TODO("Not yet implemented")
    }

    override fun replace(key: K, value: V): V? {
        TODO("Not yet implemented")
    }

    override fun replace(key: K, oldValue: V, newValue: V): Boolean {
        TODO("Not yet implemented")
    }

    override fun putIfAbsent(key: K, value: V): V? {
        TODO("Not yet implemented")
    }

    override fun get(key: K): V? {
        TODO("Not yet implemented")
    }

    override fun containsValue(value: V): Boolean {
        TODO("Not yet implemented")
    }

    override fun containsKey(key: K): Boolean {
        TODO("Not yet implemented")
    }

    override fun remove(key: K, value: V): Boolean {
        TODO("Not yet implemented")
    }

    override fun putAll(from: Map<out K, V>) {
        TODO("Not yet implemented")
    }
}
package io.vertex.autoconfigure.data.rtwb.service

/**
 * Created by xiongxl in 2023/11/30
 */
interface RtwbDataService<T, ID> {
    fun <S : T> save(entity: S): S

    fun <S : T> saveAll(entities: Iterable<S>): List<S>

    fun findByIdOrNull(id: ID): T?

    fun existsById(id: ID): Boolean

    fun findAllById(ids: Iterable<ID>): List<T>

    fun count(): Long

    fun deleteById(id: ID)

    fun delete(entity: T)

    fun deleteAllById(ids: Iterable<ID>)

    fun deleteAll(entities: Iterable<T>)
}
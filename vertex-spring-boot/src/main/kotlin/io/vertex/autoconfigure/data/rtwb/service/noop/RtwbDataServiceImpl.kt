package io.vertex.autoconfigure.data.rtwb.service.noop

import io.vertex.autoconfigure.data.rtwb.service.AssistDataService
import io.vertex.autoconfigure.data.rtwb.service.BehindDataService
import io.vertex.autoconfigure.data.rtwb.service.PrimaryDataService
import io.vertex.autoconfigure.data.rtwb.service.RtwbDataService

/**
 * Created by xiongxl in 2023/12/3
 */
class RtwbDataServiceImpl<T, ID>(
    private val primaryDataService: PrimaryDataService<T, ID>,
    private val behindDataService: BehindDataService<T, ID>,
    private val assistDataService: AssistDataService<T, ID>
) : RtwbDataService<T, ID> {
    override fun <S : T> save(entity: S): S {
        TODO("Not yet implemented")
    }

    override fun <S : T> saveAll(entities: Iterable<S>): List<S> {
        TODO("Not yet implemented")
    }

    override fun findByIdOrNull(id: ID): T? {
        TODO("Not yet implemented")
    }

    override fun existsById(id: ID): Boolean {
        TODO("Not yet implemented")
    }

    override fun findAllById(ids: Iterable<ID>): List<T> {
        TODO("Not yet implemented")
    }

    override fun count(): Long {
        TODO("Not yet implemented")
    }

    override fun deleteAll(entities: Iterable<T>) {
        TODO("Not yet implemented")
    }

    override fun deleteAllById(ids: Iterable<ID>) {
        TODO("Not yet implemented")
    }

    override fun delete(entity: T) {
        TODO("Not yet implemented")
    }

    override fun deleteById(id: ID) {
        TODO("Not yet implemented")
    }
}
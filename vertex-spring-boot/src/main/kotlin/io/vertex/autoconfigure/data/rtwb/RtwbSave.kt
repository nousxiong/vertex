package io.vertex.autoconfigure.data.rtwb

import org.slf4j.Logger

/**
 * Created by xiongxl in 2023-11-28
 */
interface RtwbSave<T> {
    fun <S : T> save(entity: S): S
}

class RtwbSaveImpl<T> : RtwbSave<T> {
    companion object {
        val logger: Logger = org.slf4j.LoggerFactory.getLogger(RtwbSaveImpl::class.java)
    }
    override fun <S : T> save(entity: S): S {
        logger.info("RtwbSaveImpl.save")
        return entity
    }
}
package io.vertex.autoconfigure.data.rtwb

/**
 * Created by xiongxl in 2023-11-28 使用RTWB的仓库，实体需要从这里继承
 */
interface RtwbEntity {
    /**
     * 删除时间戳，用于处理缓存删除操作时判断是否需要从数据库中真正删除
     * 此值仅存放在缓存中，不会持久化到数据库
     */
    val delts: Long
}
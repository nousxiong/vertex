package io.vertex.autoconfigure.data.rtwb

import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.NoRepositoryBean

/**
 * Created by xiongxl in 2023-11-27 ReadThrough+WriteBehind缓存模式CRUD仓库接口
 */
@NoRepositoryBean
interface RtwbCrudRepository<T, ID> : CrudRepository<T, ID> {
}
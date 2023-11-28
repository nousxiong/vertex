package io.vertex.data

import io.vertex.autoconfigure.data.rtwb.RtwbCrudRepository
import org.springframework.data.repository.NoRepositoryBean

/**
 * Created by xiongxl in 2023-11-28
 */
@NoRepositoryBean
interface VertexDataRepository<T, ID> : RtwbCrudRepository<T, ID>

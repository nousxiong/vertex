package io.vertex.sample.data.simplex;

import java.util.Optional;

public interface SampleCrud<T, ID> {
    <S extends T> S save(S entity);

    Optional<T> findById(ID id);

    boolean existsById(ID id);

    long count();
}

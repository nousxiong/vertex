package io.vertex.sample.data.simple;

import java.util.Optional;

public interface SampleCrud<T, ID> {
    <S extends T> S save(S entity);

    Optional<T> findById(ID id);

    boolean existsById(ID id);

    long count();
}

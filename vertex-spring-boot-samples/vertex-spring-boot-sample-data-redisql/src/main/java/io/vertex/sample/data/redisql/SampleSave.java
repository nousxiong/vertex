package io.vertex.sample.data.redisql;

public interface SampleSave<T> {
    <S extends T> S save(S entity);
}

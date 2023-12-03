package io.vertex.sample.data.simplex;

public interface SampleSave<T> {
    <S extends T> S save(S entity);
}

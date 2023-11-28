package io.vertex.sample.data.simple;

public interface SampleSave<T> {
    <S extends T> S save(S entity);
}

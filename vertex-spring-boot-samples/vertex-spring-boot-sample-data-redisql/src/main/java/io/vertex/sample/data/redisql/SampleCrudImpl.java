package io.vertex.sample.data.redisql;

import java.util.Optional;

public class SampleCrudImpl<T, ID> implements SampleCrud<T, ID> {
    public SampleCrudImpl(SampleCrud<T, ID> sampleCrud) {
        this.wrapped = sampleCrud;
    }
    private final SampleCrud<T, ID> wrapped;

    @Override
    public <S extends T> S save(S entity) {
        return wrapped.save(entity);
    }

    @Override
    public Optional<T> findById(ID id) {
        return wrapped.findById(id);
    }


    @Override
    public boolean existsById(ID id) {
        return wrapped.existsById(id);
    }

    @Override
    public long count() {
        return wrapped.count();
    }
}

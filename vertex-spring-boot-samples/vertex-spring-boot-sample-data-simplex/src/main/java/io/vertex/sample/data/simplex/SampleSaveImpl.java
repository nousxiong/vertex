package io.vertex.sample.data.simplex;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SampleSaveImpl<T> implements SampleSave<T> {
    public SampleSaveImpl(SampleSave<T> sampleSave) {
        this.wrapped = sampleSave;
    }
    private final SampleSave<T> wrapped;

    private final static Logger logger = LoggerFactory.getLogger(SampleSaveImpl.class);
    @Override
    public <S extends T> S save(S entity) {
        logger.info("SampleSaveImpl.save()");
        return wrapped.save(entity);
    }
}

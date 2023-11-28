package io.vertex.sample.data.simple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SampleSaveImpl<T> implements SampleSave<T> {
    private final static Logger logger = LoggerFactory.getLogger(SampleSaveImpl.class);
    @Override
    public <S extends T> S save(S entity) {
        logger.info("SampleSaveImpl.save()");
        return entity;
    }
}

package org.ovirt.engine.api.restapi.types;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.ovirt.engine.api.model.Qos;
import org.ovirt.engine.api.model.QosType;

public class StorageQosMapperTest extends QosMapperTest {

    @Override
    protected void verify(Qos model, Qos transform) {
        super.verify(model, transform);

        // storage limits:
        assertEquals(model.getMaxThroughput(), transform.getMaxThroughput());
        assertEquals(model.getMaxReadThroughput(), transform.getMaxReadThroughput());
        assertEquals(model.getMaxWriteThroughput(), transform.getMaxWriteThroughput());
        assertEquals(model.getMaxIops(), transform.getMaxIops());
        assertEquals(model.getMaxReadIops(), transform.getMaxReadIops());
        assertEquals(model.getMaxWriteIops(), transform.getMaxWriteIops());
    }

    @Override
    protected Qos postPopulate(Qos model) {
        model = super.postPopulate(model);
        model.setType(QosType.STORAGE);
        return model;

    }
}

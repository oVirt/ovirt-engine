package org.ovirt.engine.api.restapi.types;

import org.ovirt.engine.api.model.QoS;
import org.ovirt.engine.api.model.QosType;

public class StorageQosMapperTest extends QosMapperTest {

    @Override
    protected void verify(QoS model, QoS transform) {
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
    protected QoS postPopulate(QoS model) {
        model = super.postPopulate(model);
        model.setType(QosType.STORAGE.name().toLowerCase());
        return model;

    }
}

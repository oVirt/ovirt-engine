package org.ovirt.engine.api.restapi.types;

import org.ovirt.engine.api.model.QoS;
import org.ovirt.engine.api.model.QosType;
import org.ovirt.engine.core.common.businessentities.qos.QosBase;

public class QosMapperTest extends AbstractInvertibleMappingTest<QoS, QosBase, QosBase> {

    public QosMapperTest() {
        super(QoS.class,
                QosBase.class,
                QosBase.class);
    }

    @Override
    protected void verify(QoS model, QoS transform) {
        assertNotNull(transform);
        assertEquals(model.getName(), transform.getName());
        assertEquals(model.getId(), transform.getId());
        assertNotNull(transform.getDataCenter());
        assertEquals(model.getDataCenter().getId(), transform.getDataCenter().getId());
        assertEquals(model.getType(), transform.getType());
        assertEquals(model.getDescription(), transform.getDescription());

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
        model.setType(MappingTestHelper.shuffle(QosType.class).name().toLowerCase());
        return model;
    }
}

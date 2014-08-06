package org.ovirt.engine.api.restapi.types;

import org.ovirt.engine.api.model.QoS;
import org.ovirt.engine.core.common.businessentities.qos.QosBase;

public abstract class QosMapperTest extends AbstractInvertibleMappingTest<QoS, QosBase, QosBase> {

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
    }

}

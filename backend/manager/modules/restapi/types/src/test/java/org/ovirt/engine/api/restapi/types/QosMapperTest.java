package org.ovirt.engine.api.restapi.types;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.ovirt.engine.api.model.Qos;
import org.ovirt.engine.core.common.businessentities.qos.QosBase;

public abstract class QosMapperTest extends AbstractInvertibleMappingTest<Qos, QosBase, QosBase> {

    public QosMapperTest() {
        super(Qos.class,
                QosBase.class,
                QosBase.class);
    }

    @Override
    protected void verify(Qos model, Qos transform) {
        assertNotNull(transform);
        assertEquals(model.getName(), transform.getName());
        assertEquals(model.getId(), transform.getId());
        assertNotNull(transform.getDataCenter());
        assertEquals(model.getDataCenter().getId(), transform.getDataCenter().getId());
        assertEquals(model.getType(), transform.getType());
        assertEquals(model.getDescription(), transform.getDescription());
    }

}

package org.ovirt.engine.api.restapi.types;

import org.ovirt.engine.api.model.QoS;
import org.ovirt.engine.api.model.QosType;

public class CpuQosMapperTest extends QosMapperTest {

    @Override
    protected void verify(QoS model, QoS transform) {
        super.verify(model, transform);

        // cpu limits:
        assertEquals(model.getCpuLimit(), transform.getCpuLimit());
    }

    @Override
    protected QoS postPopulate(QoS model) {
        model = super.postPopulate(model);
        model.setType(QosType.CPU.name().toLowerCase());
        return model;

    }
}

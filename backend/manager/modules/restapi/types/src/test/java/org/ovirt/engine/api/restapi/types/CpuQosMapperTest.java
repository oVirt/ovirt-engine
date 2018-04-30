package org.ovirt.engine.api.restapi.types;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.ovirt.engine.api.model.Qos;
import org.ovirt.engine.api.model.QosType;

public class CpuQosMapperTest extends QosMapperTest {

    @Override
    protected void verify(Qos model, Qos transform) {
        super.verify(model, transform);

        // cpu limits:
        assertEquals(model.getCpuLimit(), transform.getCpuLimit());
    }

    @Override
    protected Qos postPopulate(Qos model) {
        model = super.postPopulate(model);
        model.setType(QosType.CPU);
        return model;

    }
}

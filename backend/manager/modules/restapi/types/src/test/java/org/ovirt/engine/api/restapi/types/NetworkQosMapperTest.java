package org.ovirt.engine.api.restapi.types;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.ovirt.engine.api.model.Qos;
import org.ovirt.engine.api.model.QosType;

public class NetworkQosMapperTest extends QosMapperTest {

    @Override
    protected void verify(Qos model, Qos transform) {
        super.verify(model, transform);

        // network limits:
        assertEquals(model.getInboundAverage(), transform.getInboundAverage());
        assertEquals(model.getInboundPeak(), transform.getInboundPeak());
        assertEquals(model.getInboundBurst(), transform.getInboundBurst());
        assertEquals(model.getOutboundAverage(), transform.getOutboundAverage());
        assertEquals(model.getOutboundPeak(), transform.getOutboundPeak());
        assertEquals(model.getOutboundBurst(), transform.getOutboundBurst());
    }

    @Override
    protected Qos postPopulate(Qos model) {
        model = super.postPopulate(model);
        model.setType(QosType.NETWORK);
        return model;

    }
}

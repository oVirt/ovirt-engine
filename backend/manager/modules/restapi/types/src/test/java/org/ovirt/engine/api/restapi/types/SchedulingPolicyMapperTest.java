package org.ovirt.engine.api.restapi.types;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.ovirt.engine.api.model.SchedulingPolicy;
import org.ovirt.engine.api.restapi.utils.CustomPropertiesParser;
import org.ovirt.engine.core.common.scheduling.ClusterPolicy;

public class SchedulingPolicyMapperTest extends AbstractInvertibleMappingTest<SchedulingPolicy, ClusterPolicy, ClusterPolicy> {

    public SchedulingPolicyMapperTest() {
        super(SchedulingPolicy.class,
                ClusterPolicy.class,
                ClusterPolicy.class);
    }

    @Override
    protected void verify(SchedulingPolicy model, SchedulingPolicy transform) {
        assertNotNull(transform);
        assertEquals(model.getName(), transform.getName());
        assertEquals(model.getId(), transform.getId());
        assertEquals(model.getDescription(), transform.getDescription());

        assertEquals(model.isLocked(), transform.isLocked());
        assertEquals(model.isDefaultPolicy(), transform.isDefaultPolicy());
        assertNotNull(model.getProperties());
        assertEquals(CustomPropertiesParser.toMap(model.getProperties()),
                CustomPropertiesParser.toMap(transform.getProperties()));
    }

}

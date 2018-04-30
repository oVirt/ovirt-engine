package org.ovirt.engine.api.restapi.types;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.ovirt.engine.api.model.CpuProfile;

public class CpuProfileMapperTest extends AbstractInvertibleMappingTest<CpuProfile,
        org.ovirt.engine.core.common.businessentities.profiles.CpuProfile,
        org.ovirt.engine.core.common.businessentities.profiles.CpuProfile> {

    public CpuProfileMapperTest() {
        super(CpuProfile.class,
                org.ovirt.engine.core.common.businessentities.profiles.CpuProfile.class,
                org.ovirt.engine.core.common.businessentities.profiles.CpuProfile.class);
    }

    @Override
    protected void verify(CpuProfile model, CpuProfile transform) {
        assertNotNull(transform);
        assertEquals(model.getName(), transform.getName());
        assertEquals(model.getId(), transform.getId());
        assertEquals(model.getDescription(), transform.getDescription());
        assertNotNull(transform.getCluster());
        assertEquals(model.getCluster().getId(), transform.getCluster().getId());
        assertNotNull(transform.getQos());
        assertEquals(model.getQos().getId(), transform.getQos().getId());
    }
}

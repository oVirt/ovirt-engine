package org.ovirt.engine.api.restapi.types;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.ovirt.engine.api.model.DiskProfile;

public class DiskProfileMapperTest extends AbstractInvertibleMappingTest<DiskProfile,
        org.ovirt.engine.core.common.businessentities.profiles.DiskProfile,
        org.ovirt.engine.core.common.businessentities.profiles.DiskProfile> {

    public DiskProfileMapperTest() {
        super(DiskProfile.class,
                org.ovirt.engine.core.common.businessentities.profiles.DiskProfile.class,
                org.ovirt.engine.core.common.businessentities.profiles.DiskProfile.class);
    }

    @Override
    protected void verify(DiskProfile model, DiskProfile transform) {
        assertNotNull(transform);
        assertEquals(model.getName(), transform.getName());
        assertEquals(model.getId(), transform.getId());
        assertEquals(model.getDescription(), transform.getDescription());
        assertNotNull(transform.getStorageDomain());
        assertEquals(model.getStorageDomain().getId(), transform.getStorageDomain().getId());
        assertNotNull(transform.getQos());
        assertEquals(model.getQos().getId(), transform.getQos().getId());
    }
}

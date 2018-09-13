package org.ovirt.engine.api.restapi.types;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.ovirt.engine.api.model.HostStorage;
import org.ovirt.engine.api.model.StorageType;
import org.ovirt.engine.api.model.VolumeGroup;


public class StorageVolumeGroupMapperTest extends AbstractInvertibleMappingTest<HostStorage, org.ovirt.engine.core.common.businessentities.StorageDomain, org.ovirt.engine.core.common.businessentities.StorageDomain> {

    public StorageVolumeGroupMapperTest() {
        super(HostStorage.class, org.ovirt.engine.core.common.businessentities.StorageDomain.class, org.ovirt.engine.core.common.businessentities.StorageDomain.class);
    }

    @Override
    protected HostStorage postPopulate(HostStorage from) {
        from.setType(StorageType.FCP);
        from.setVolumeGroup(new VolumeGroup());
        from.getVolumeGroup().setId(from.getId());
        return from;
    }

    @Override
    protected void verify(HostStorage model, HostStorage transform) {
        assertNotNull(transform);
        assertEquals(model.getId(), transform.getId());
        assertEquals(model.getType(), transform.getType());
        assertNotNull(model.getVolumeGroup());
        assertEquals(model.getVolumeGroup().getId(), model.getVolumeGroup().getId());
    }
}

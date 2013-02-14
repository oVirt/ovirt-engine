package org.ovirt.engine.api.restapi.types;

import org.ovirt.engine.api.model.Storage;
import org.ovirt.engine.api.model.StorageType;
import org.ovirt.engine.api.model.VolumeGroup;


public class StorageVolumeGroupMapperTest extends AbstractInvertibleMappingTest<Storage, org.ovirt.engine.core.common.businessentities.StorageDomain, org.ovirt.engine.core.common.businessentities.StorageDomain> {

    public StorageVolumeGroupMapperTest() {
        super(Storage.class, org.ovirt.engine.core.common.businessentities.StorageDomain.class, org.ovirt.engine.core.common.businessentities.StorageDomain.class);
    }

    @Override
    protected Storage postPopulate(Storage from) {
        from.setType(MappingTestHelper.shuffle(StorageType.class).value());
        from.setVolumeGroup(new VolumeGroup());
        from.getVolumeGroup().setId(from.getId());
        return from;
    }

    @Override
    protected void verify(Storage model, Storage transform) {
        assertNotNull(transform);
        assertEquals(model.getId(), transform.getId());
        assertEquals(model.getType(), transform.getType());
        assertNotNull(model.getVolumeGroup());
        assertEquals(model.getVolumeGroup().getId(), model.getVolumeGroup().getId());
    }
}

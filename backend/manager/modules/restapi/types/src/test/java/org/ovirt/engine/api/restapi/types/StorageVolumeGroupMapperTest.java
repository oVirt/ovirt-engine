package org.ovirt.engine.api.restapi.types;

import org.ovirt.engine.api.model.VolumeGroup;
import org.ovirt.engine.api.model.Storage;
import org.ovirt.engine.api.model.StorageType;
import org.ovirt.engine.core.common.businessentities.storage_domains;

public class StorageVolumeGroupMapperTest extends AbstractInvertibleMappingTest<Storage, storage_domains, storage_domains> {

    protected StorageVolumeGroupMapperTest() {
        super(Storage.class, storage_domains.class, storage_domains.class);
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

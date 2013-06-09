package org.ovirt.engine.api.restapi.types;

import org.junit.Test;
import org.ovirt.engine.api.model.NfsVersion;
import org.ovirt.engine.api.model.StorageDomain;
import org.ovirt.engine.api.model.StorageDomainStatus;
import org.ovirt.engine.api.model.StorageDomainType;
import org.ovirt.engine.api.model.StorageType;
import org.ovirt.engine.api.restapi.model.StorageFormat;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;


public class StorageDomainMapperTest extends
        AbstractInvertibleMappingTest<StorageDomain, StorageDomainStatic, org.ovirt.engine.core.common.businessentities.StorageDomain> {

    public StorageDomainMapperTest() {
        super(StorageDomain.class, StorageDomainStatic.class, org.ovirt.engine.core.common.businessentities.StorageDomain.class);
    }

    @Override
    protected StorageDomain postPopulate(StorageDomain model) {
        model.setType(MappingTestHelper.shuffle(StorageDomainType.class).value());
        model.getStorage().setType(MappingTestHelper.shuffle(StorageType.class).value());
        model.setStorageFormat(MappingTestHelper.shuffle(StorageFormat.class).value());
        return model;
    }

    @Override
    protected org.ovirt.engine.core.common.businessentities.StorageDomain getInverse(StorageDomainStatic to) {
        org.ovirt.engine.core.common.businessentities.StorageDomain inverse = new org.ovirt.engine.core.common.businessentities.StorageDomain();
        inverse.setId(to.getId());
        inverse.setStorageName(to.getStorageName());
        inverse.setStorageDomainType(to.getStorageDomainType());
        inverse.setStorageType(to.getStorageType());
        inverse.setStorageFormat(to.getStorageFormat());
        inverse.setComment(to.getComment());
        return inverse;
    }

    @Override
    protected void verify(StorageDomain model, StorageDomain transform) {
        assertNotNull(transform);
        assertEquals(model.getName(), transform.getName());
        assertEquals(model.getId(), transform.getId());
        // REVIST No descriptions for storage domains
        // assertEquals(model.getDescription(), transform.getDescription());
        assertEquals(model.getComment(), transform.getComment());
        assertEquals(model.getType(), transform.getType());
        assertNotNull(transform.getStorage());
        assertEquals(model.getStorage().getType(), transform.getStorage().getType());
        assertEquals(model.getStorageFormat(), transform.getStorageFormat());
    }

    @Test
    public void testMemory() {
        org.ovirt.engine.core.common.businessentities.StorageDomain entity = new org.ovirt.engine.core.common.businessentities.StorageDomain();
        entity.setAvailableDiskSize(3);
        entity.setUsedDiskSize(4);
        entity.setCommittedDiskSize(5);
        StorageDomain model = StorageDomainMapper.map(entity, (StorageDomain) null);
        assertEquals(model.getAvailable(), Long.valueOf(3221225472L));
        assertEquals(model.getUsed(), Long.valueOf(4294967296L));
        assertEquals(model.getCommitted(), Long.valueOf(5368709120L));
    }

    @Test
    public void storageDomainMappings() {
        assertEquals(StorageDomainStatus.ACTIVE, StorageDomainMapper.map(org.ovirt.engine.core.common
                .businessentities.StorageDomainStatus.Active, null));
        assertEquals(StorageDomainStatus.INACTIVE, StorageDomainMapper.map(org.ovirt.engine.core.common
                .businessentities.StorageDomainStatus.InActive, null));
        assertEquals(StorageDomainStatus.LOCKED, StorageDomainMapper.map(org.ovirt.engine.core.common
                .businessentities.StorageDomainStatus.Locked, null));
        assertEquals(StorageDomainStatus.UNATTACHED, StorageDomainMapper.map(org.ovirt.engine.core.common
                .businessentities.StorageDomainStatus.Unattached, null));
        assertEquals(StorageDomainStatus.UNKNOWN, StorageDomainMapper.map(org.ovirt.engine.core.common
                .businessentities.StorageDomainStatus.Unknown, null));
        assertTrue(StorageDomainMapper.map(org.ovirt.engine.core.common
                .businessentities.StorageDomainStatus.Uninitialized, null) == null);
        assertEquals(StorageDomainStatus.MAINTENANCE, StorageDomainMapper.map(org.ovirt.engine.core.common
                .businessentities.StorageDomainStatus.Maintenance, null));

        assertEquals(org.ovirt.engine.core.common.businessentities.NfsVersion.V3,
                StorageDomainMapper.map(NfsVersion.V3, null));
        assertEquals(org.ovirt.engine.core.common.businessentities.NfsVersion.V4,
                StorageDomainMapper.map(NfsVersion.V4, null));
        assertEquals(org.ovirt.engine.core.common.businessentities.NfsVersion.AUTO,
                StorageDomainMapper.map(NfsVersion.AUTO, null));
        assertEquals(NfsVersion.V3.value(), StorageDomainMapper.map(org.ovirt.engine.core.common
                .businessentities.NfsVersion.V3, null));
        assertEquals(NfsVersion.V4.value(), StorageDomainMapper.map(org.ovirt.engine.core.common
                .businessentities.NfsVersion.V4, null));
        assertEquals(NfsVersion.AUTO.value(), StorageDomainMapper.map(org.ovirt.engine.core.common
                .businessentities.NfsVersion.AUTO, null));
    }
}

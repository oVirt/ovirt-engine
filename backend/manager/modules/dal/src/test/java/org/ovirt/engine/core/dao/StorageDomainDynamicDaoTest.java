package org.ovirt.engine.core.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.common.businessentities.ExternalStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainDynamic;
import org.ovirt.engine.core.compat.Guid;

public class StorageDomainDynamicDaoTest
        extends BaseGenericDaoTestCase<Guid, StorageDomainDynamic, StorageDomainDynamicDao> {

    private static final int USED_DISK_SIZE = 1000;

    @Override
    protected StorageDomainDynamic generateNewEntity() {
        StorageDomainDynamic newDynamicDomain = new StorageDomainDynamic();
        newDynamicDomain.setId(FixturesTool.STORAGE_DOMAIN_NFS2_2);
        newDynamicDomain.setAvailableDiskSize(USED_DISK_SIZE);
        return newDynamicDomain;
    }

    @Override
    protected void updateExistingEntity() {
        existingEntity.setUsedDiskSize(USED_DISK_SIZE);
    }

    @Override
    protected Guid getExistingEntityId() {
        return FixturesTool.STORAGE_DOMAIN_SCALE_SD5;
    }

    @Override
    protected Guid generateNonExistingId() {
        return Guid.newGuid();
    }

    @Override
    protected int getEntitiesTotalCount() {
        return 10;
    }

    @Test
    public void testUpdateStorageDomainExternalStatus() {
        existingEntity.setExternalStatus(ExternalStatus.Error);
        dao.updateExternalStatus(getExistingEntityId(), existingEntity.getExternalStatus());
        StorageDomainDynamic after = dao.get(existingEntity.getId());
        assertEquals(existingEntity.getExternalStatus(), after.getExternalStatus());
    }
}

package org.ovirt.engine.core.dao.qos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.common.businessentities.qos.StorageQos;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.BaseGenericDaoTestCase;
import org.ovirt.engine.core.dao.FixturesTool;

public class StorageQosDaoTest extends BaseGenericDaoTestCase<Guid, StorageQos, StorageQosDao> {
    @Override
    protected StorageQos generateNewEntity() {
        StorageQos storageQos = new StorageQos();
        storageQos.setId(Guid.newGuid());
        storageQos.setName("qos_d");
        storageQos.setDescription("bla bla");
        storageQos.setStoragePoolId(FixturesTool.STORAGE_POOL_MIXED_TYPES);
        storageQos.setMaxThroughput(200);
        storageQos.setMaxReadThroughput(200);
        storageQos.setMaxWriteThroughput(200);
        storageQos.setMaxIops(200);
        storageQos.setMaxReadIops(200);
        storageQos.setMaxWriteIops(200);
        return storageQos;
    }

    @Override
    protected void updateExistingEntity() {
        existingEntity.setName("newB");
        existingEntity.setDescription("If I owned a company, my employees would love me. They’d have huge pictures of me up the walls and in their home, like Lenin.");
        existingEntity.setMaxThroughput(30);
        existingEntity.setMaxReadThroughput(30);
        existingEntity.setMaxWriteThroughput(30);
        existingEntity.setMaxIops(30);
        existingEntity.setMaxReadIops(30);
        existingEntity.setMaxWriteIops(30);
    }

    @Override
    protected Guid getExistingEntityId() {
        return FixturesTool.QOS_ID_1;
    }

    @Override
    protected Guid generateNonExistingId() {
        return Guid.newGuid();
    }

    @Override
    protected int getEntitiesTotalCount() {
        return 3;
    }

    @Test
    public void getAllStorageQosForStoragePool() {
        List<StorageQos> allForStoragePoolId = dao.getAllForStoragePoolId(FixturesTool.STORAGE_POOL_MIXED_TYPES);
        assertNotNull(allForStoragePoolId);
        assertEquals(2, allForStoragePoolId.size());
    }

    @Test
    public void getQosByDiskProfileId() {
        StorageQos qos = dao.getQosByDiskProfileId(FixturesTool.DISK_PROFILE_1);
        assertNotNull(qos);
        assertEquals(FixturesTool.QOS_ID_1, qos.getId());
    }

    @Test
    public void getQosByDiskProfileIds() {
        Map<Guid, StorageQos> qosMap = dao.getQosByDiskProfileIds(Collections.singleton(FixturesTool.DISK_PROFILE_1));
        assertNotNull(qosMap);
        assertEquals(FixturesTool.QOS_ID_1, qosMap.get(FixturesTool.DISK_PROFILE_1).getId());
    }
}

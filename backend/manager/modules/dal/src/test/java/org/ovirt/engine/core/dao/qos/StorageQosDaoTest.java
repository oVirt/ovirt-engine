package org.ovirt.engine.core.dao.qos;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.qos.StorageQos;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.BaseDaoTestCase;
import org.ovirt.engine.core.dao.FixturesTool;

public class StorageQosDaoTest extends BaseDaoTestCase {

    private StorageQosDao dao;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        dao = getDbFacade().getStorageQosDao();
    }

    /**
     * Ensures that retrieving with an invalid ID returns null.
     */
    @Test
    public void testGetWithInvalidId() {
        assertNull(dao.get(Guid.newGuid()));
    }

    @Test
    public void getStorageQos() {
        StorageQos storageQos = new StorageQos();
        storageQos.setId(FixturesTool.QOS_ID_1);
        storageQos.setName("qos_a");
        storageQos.setDescription("You don't understand. There's relationship George, and then there's the George you know. Baudy George, Funny George");
        storageQos.setStoragePoolId(FixturesTool.STORAGE_POOL_MIXED_TYPES);
        storageQos.setMaxThroughput(1000);
        storageQos.setMaxReadThroughput(2000);
        storageQos.setMaxWriteThroughput(500);
        storageQos.setMaxIops(1000);
        storageQos.setMaxReadIops(2000);
        storageQos.setMaxWriteIops(500);

        StorageQos fetched = dao.get(FixturesTool.QOS_ID_1);
        assertNotNull(fetched);
        assertEquals(storageQos, fetched);
    }

    @Test
    public void updateStorageQos() {
        StorageQos storageQos = dao.get(FixturesTool.QOS_ID_2);
        assertNotNull(storageQos);
        storageQos.setName("newB");
        storageQos.setDescription("If I owned a company, my employees would love me. They’d have huge pictures of me up the walls and in their home, like Lenin.");
        storageQos.setMaxThroughput(30);
        storageQos.setMaxReadThroughput(30);
        storageQos.setMaxWriteThroughput(30);
        storageQos.setMaxIops(30);
        storageQos.setMaxReadIops(30);
        storageQos.setMaxWriteIops(30);
        assertFalse(storageQos.equals(dao.get(FixturesTool.QOS_ID_2)));
        dao.update(storageQos);
        StorageQos fetched = dao.get(FixturesTool.QOS_ID_2);
        assertNotNull(fetched);
        assertEquals(storageQos, fetched);
    }

    @Test
    public void removeStorageQos() {
        assertNotNull(dao.get(FixturesTool.QOS_ID_3));
        dao.remove(FixturesTool.QOS_ID_3);
        assertNull(dao.get(FixturesTool.QOS_ID_3));
    }

    @Test
    public void saveStorageQos() {
        StorageQos storageQos = new StorageQos();
        storageQos.setId(Guid.newGuid());
        assertNull(dao.get(storageQos.getId()));
        storageQos.setName("qos_d");
        storageQos.setDescription("bla bla");
        storageQos.setStoragePoolId(FixturesTool.STORAGE_POOL_MIXED_TYPES);
        storageQos.setMaxThroughput(200);
        storageQos.setMaxReadThroughput(200);
        storageQos.setMaxWriteThroughput(200);
        storageQos.setMaxIops(200);
        storageQos.setMaxReadIops(200);
        storageQos.setMaxWriteIops(200);
        dao.save(storageQos);
        StorageQos fetched = dao.get(storageQos.getId());
        assertNotNull(fetched);
        assertEquals(storageQos, fetched);
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

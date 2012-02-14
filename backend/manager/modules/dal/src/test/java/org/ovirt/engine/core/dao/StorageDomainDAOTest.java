package org.ovirt.engine.core.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.image_group_storage_domain_map;
import org.ovirt.engine.core.common.businessentities.storage_domain_static;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NGuid;
import org.ovirt.engine.core.utils.RandomUtils;

public class StorageDomainDAOTest extends BaseDAOTestCase {
    private static final String EXISTING_DOMAIN_ID = "72e3a666-89e1-4005-a7ca-f7548004a9ab";
    private static final Guid EXISTING_IMAGE_GROUP_ID = new Guid("c9a559d9-8666-40d1-9967-759502b19f0b");
    private static final Guid EXISTING_STORAGE_POOL_ID = new Guid("6d849ebf-755f-4552-ad09-9a090cda105d");
    private static final String EXISTING_CONNECTION = "10.35.64.25";

    private StorageDomainDAO dao;
    private storage_domains existingDomain;
    private storage_domain_static newStaticDomain;
    private image_group_storage_domain_map existingImageGroupStorageDomainMap;
    private image_group_storage_domain_map newImageGroupStorageDomainMap;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        dao = prepareDAO(dbFacade.getStorageDomainDAO());
        existingDomain = dao.get(new Guid(EXISTING_DOMAIN_ID));

        newStaticDomain = new storage_domain_static();
        newStaticDomain.setstorage("fDMzhE-wx3s-zo3q-Qcxd-T0li-yoYU-QvVePl");

        existingImageGroupStorageDomainMap =
                dao.getImageGroupStorageDomainMapForImageGroupAndStorageDomain(new image_group_storage_domain_map(EXISTING_IMAGE_GROUP_ID,
                        existingDomain.getId()));
        newImageGroupStorageDomainMap = new image_group_storage_domain_map(new Guid(), existingDomain.getId());
    }

    /**
     * Ensures that retrieving the id works.
     */
    @Test
    public void testGetMasterStorageDomainIdForPool() {
        Guid result = dao.getMasterStorageDomainIdForPool(EXISTING_STORAGE_POOL_ID);

        assertNotNull(result);
        assertEquals(new Guid(EXISTING_DOMAIN_ID), result);
    }

    /**
     * Ensures that nothing is returned when the id is invalid.
     */
    @Test
    public void testGetWithInvalidId() {
        storage_domains result = dao.get(Guid.NewGuid());

        assertNull(result);
    }

    /**
     * Ensures that retrieving a domain works.
     */
    @Test
    public void testGet() {
        storage_domains result = dao.get(existingDomain.getId());

        assertNotNull(result);
        assertEquals(existingDomain, result);
    }

    /**
     * Ensures that null is returned when the specified id does not exist.
     */
    @Test
    public void testGetForStoragePoolWithInvalidId() {
        storage_domains result = dao.getForStoragePool(Guid.NewGuid(), EXISTING_STORAGE_POOL_ID);

        assertNull(result);
    }

    /**
     * Ensures that null is returned if the pool doesn't exist.
     */
    @Test
    public void testGetForStoragePoolWithInvalidPool() {
        storage_domains result = dao.getForStoragePool(existingDomain.getId(),
                NGuid.NewGuid());

        assertNull(result);
    }

    /**
     * Ensures the call works as expected.
     */
    @Test
    public void testGetForStoragePool() {
        storage_domains result = dao.getForStoragePool(existingDomain.getId(), EXISTING_STORAGE_POOL_ID);

        assertNotNull(result);
        assertEquals(existingDomain, result);
    }

    /**
     * Ensures that all instances are returned.
     */
    @Test
    public void testGetAll() {
        List<storage_domains> result = dao.getAll();

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    /**
     * Ensures an empty collection is returned when the image group is invalid.
     */
    @Test
    public void testGetAllForImageGroupWithInvalidImageGroup() {
        List<storage_domains> result = dao.getAllForImageGroup(Guid.NewGuid());

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    /**
     * Ensures that the right domains are returned.
     */
    @Test
    public void testGetAllForImageGroup() {
        List<storage_domains> result = dao.getAllForImageGroup(new Guid(
                "c9a559d9-8666-40d1-9967-759502b19f0b"));

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    /**
     * Ensures a null collection is returned.
     */
    @Test
    public void testGetAllForStorageDomainWithInvalidDomain() {
        List<storage_domains> result = dao.getAllForStorageDomain(Guid
                .NewGuid());

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    /**
     * Ensures that the right set of domains are returned.
     */
    @Test
    public void testGetAllForStorageDomain() {
        List<storage_domains> result = dao
                .getAllForStorageDomain(existingDomain.getId());

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    /**
     * Ensures an empty list is returned for an invalid connection.
     */
    @Test
    public void testGetAllForConnectionWithInvalidConnection() {
        List<storage_domains> result = dao.getAllForConnection(RandomUtils.instance().nextString(10));

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    /**
     * Ensures that the right collection is returned for a given connection.
     */
    @Test
    public void testGetAllForConnection() {
        List<storage_domains> result = dao.getAllForConnection(EXISTING_CONNECTION);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        for (storage_domains domain : result) {
            assertEquals(EXISTING_STORAGE_POOL_ID, domain.getstorage_pool_id());
        }
    }

    /**
     * Ensures an empty list is returned for an invalid connection.
     */
    @Test
    public void testGetAllByStoragePoolAndConnectionWithInvalidConnection() {
        List<storage_domains> result =
                dao.getAllByStoragePoolAndConnection(EXISTING_STORAGE_POOL_ID, RandomUtils.instance().nextString(10));

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    /**
     * Ensures an empty list is returned for an invalid storage pool id.
     */
    @Test
    public void testGetAllByStoragePoolAndConnectionWithInvalidStoragePool() {
        List<storage_domains> result =
                dao.getAllByStoragePoolAndConnection(Guid.NewGuid(), EXISTING_CONNECTION);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    /**
     * Ensures an empty list is returned for an invalid storage pool id and connection.
     */
    @Test
    public void testGetAllByStoragePoolAndConnectionWithInvalidInput() {
        List<storage_domains> result =
                dao.getAllByStoragePoolAndConnection(Guid.NewGuid(), RandomUtils.instance().nextString(10));

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    /**
     * Ensures that the right collection is returned for a given connection.
     */
    @Test
    public void testGetAllByStoragePoolAndConnection() {
        List<storage_domains> result =
                dao.getAllByStoragePoolAndConnection(EXISTING_STORAGE_POOL_ID, EXISTING_CONNECTION);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        for (storage_domains domain : result) {
            assertEquals(EXISTING_STORAGE_POOL_ID, domain.getstorage_pool_id());
        }
    }

    /**
     * Ensures an empty list is returned for an invalid pool.
     */
    @Test
    public void testGetAllForStoragePoolWithInvalidPool() {
        List<storage_domains> result = dao.getAllForStoragePool(Guid.NewGuid());

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    /**
     * Ensures that the right collection is returned for a given storage pool.
     */
    @Test
    public void testGetAllForStoragePool() {
        List<storage_domains> result = dao.getAllForStoragePool(EXISTING_STORAGE_POOL_ID);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        for (storage_domains domain : result) {
            assertEquals(EXISTING_STORAGE_POOL_ID, domain.getstorage_pool_id());
        }
    }

    /**
     * Ensures all storages for template returned
     */
    @Test
    public void testGetAllStorageDomainsByImageGroup() {
        List<Guid> result = dao.getAllStorageDomainsByImageGroup(new Guid("1b26a52b-b60f-44cb-9f46-3ef333b04a34"));

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(result.get(0), existingDomain.getId());
    }

    @Test
    public void testGetImageGroupStorageDomainMap() {
        image_group_storage_domain_map result =
                dao.getImageGroupStorageDomainMapForImageGroupAndStorageDomain(existingImageGroupStorageDomainMap);

        assertNotNull(result);
        assertEquals(existingImageGroupStorageDomainMap, result);
    }

    @Test
    public void testAddImageGroupStorageDomainMap() {
        dao.addImageGroupStorageDomainMap(newImageGroupStorageDomainMap);

        image_group_storage_domain_map result =
                dao.getImageGroupStorageDomainMapForImageGroupAndStorageDomain(newImageGroupStorageDomainMap);

        assertNotNull(result);
        assertEquals(newImageGroupStorageDomainMap, result);
    }

    @Test
    public void testRemoveImageGroupStorageDomainMap() {
        dao.removeImageGroupStorageDomainMap(existingImageGroupStorageDomainMap);

        image_group_storage_domain_map result =
                dao.getImageGroupStorageDomainMapForImageGroupAndStorageDomain(existingImageGroupStorageDomainMap);

        assertNull(result);
    }

    @Test
    public void testGetAllImageGroupStorageDomainMapsForStorageDomain() {
        List<image_group_storage_domain_map> result =
                dao.getAllImageGroupStorageDomainMapsForStorageDomain(existingDomain.getId());

        assertNotNull(result);
        assertFalse(result.isEmpty());
        for (image_group_storage_domain_map mapping : result) {
            assertEquals(existingDomain.getId(), mapping.getstorage_domain_id());
        }
    }

    @Test
    public void testGetAllImageGroupStorageDomainMapsForImageGroup() {
        List<image_group_storage_domain_map> result =
                dao.getAllImageGroupStorageDomainMapsForImage(EXISTING_IMAGE_GROUP_ID);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        for (image_group_storage_domain_map mapping : result) {
            assertEquals(EXISTING_IMAGE_GROUP_ID, mapping.getimage_group_id());
        }
    }
}

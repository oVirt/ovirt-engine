package org.ovirt.engine.core.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.BaseDisk;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.RandomUtils;

public class StorageDomainDAOTest extends BaseDAOTestCase {
    private static final int NUMBER_OF_STORAGE_DOMAINS_FOR_PRIVELEGED_USER = 1;

    private static final Guid EXISTING_DOMAIN_ID = FixturesTool.STORAGE_DOAMIN_SCALE_SD5;
    private static final Guid EXISTING_STORAGE_POOL_ID = new Guid("72b9e200-f48b-4687-83f2-62828f249a47");
    private static final String EXISTING_CONNECTION = "10.35.64.25:/export/share";
    private static final Guid EXISTING_USER_ID = new Guid("9bf7c640-b620-456f-a550-0348f366544b");
    private static final long NUMBER_OF_IMAGES_ON_EXISTING_DOMAIN = 5;

    private StorageDomainDAO dao;
    private StorageDomain existingDomain;
    private StorageDomainStatic newStaticDomain;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        dao = dbFacade.getStorageDomainDao();
        existingDomain = dao.get(EXISTING_DOMAIN_ID);

        newStaticDomain = new StorageDomainStatic();
        newStaticDomain.setStorage("fDMzhE-wx3s-zo3q-Qcxd-T0li-yoYU-QvVePl");
    }

    /**
     * Ensures that retrieving the id works.
     */
    @Test
    public void testGetMasterStorageDomainIdForPool() {
        Guid result = dao.getMasterStorageDomainIdForPool(new Guid("6d849ebf-755f-4552-ad09-9a090cda105d"));

        assertNotNull(result);
        assertEquals(EXISTING_DOMAIN_ID, result);
    }

    @Test
    public void testGetstorage_domain_by_type_for_storagePoolId() {
        StorageDomain result = dao.getStorageDomain(new Guid("6d849ebf-755f-4552-ad09-9a090cda105d"),
                StorageDomainType.Master);

        assertNotNull(result);
        assertGetResult(result);
    }

    /**
     * Ensures that nothing is returned when the id is invalid.
     */
    @Test
    public void testGetWithInvalidId() {
        StorageDomain result = dao.get(Guid.newGuid());

        assertNull(result);
    }

    /**
     * Ensures that retrieving a domain works.
     */
    @Test
    public void testGet() {
        StorageDomain result = dao.get(existingDomain.getId());

        assertGetResult(result);
    }

    /**
     * Ensures that the right collection is returned for a given storage pool with filtering for a privileged user.
     */
    @Test
    public void testGetWithPermissionsPrivilegedUser() {
        StorageDomain result = dao.get(existingDomain.getId(), PRIVILEGED_USER_ID, true);

        assertGetResult(result);
    }

    /**
     * Ensures that the right collection is returned for a given storage pool with filtering disabled for an unprivileged user.
     */
    @Test
    public void testGetWithPermissionsDisabledUnprivilegedUser() {
        StorageDomain result = dao.get(existingDomain.getId(), UNPRIVILEGED_USER_ID, false);

        assertGetResult(result);
    }

    /**
     * Ensures that an empty collection is returned for a given storage pool for an unprivileged user.
     */
    @Test
    public void testGetWithPermissionsUnprivilegedUser() {
        StorageDomain result = dao.get(existingDomain.getId(), UNPRIVILEGED_USER_ID, true);

        assertNull(result);
    }

    /**
     * Ensures that null is returned when the specified id does not exist.
     */
    @Test
    public void testGetForStoragePoolWithInvalidId() {
        StorageDomain result = dao.getForStoragePool(Guid.newGuid(), EXISTING_STORAGE_POOL_ID);

        assertNull(result);
    }

    /**
     * Test getting storage for existing image id.
     */
    @Test
    public void testGetAllStorageDomainsByImageId() {
        List<StorageDomain> result = dao.getAllStorageDomainsByImageId(FixturesTool.TEMPLATE_IMAGE_ID);
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(FixturesTool.STORAGE_DOAMIN_SCALE_SD5, result.get(0).getId());
    }

    /**
     * Test getting storage for not existing image id.</BR> The expected result should be an empty list.
     */
    @Test
    public void testGetAllStorageDomainsByNotExistingImageId() {
        List<StorageDomain> result = dao.getAllStorageDomainsByImageId(Guid.newGuid());
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    /**
     * Asserts the result of {@link StorageDomainDAO#get(Guid)} returns the correct domain
     * @param result
     */
    private void assertGetResult(StorageDomain result) {
        assertNotNull(result);
        assertEquals(existingDomain, result);
    }

    /**
     * Ensures that null is returned if the pool doesn't exist.
     */
    @Test
    public void testGetForStoragePoolWithInvalidPool() {
        StorageDomain result = dao.getForStoragePool(existingDomain.getId(),
                Guid.newGuid());

        assertNull(result);
    }

    /**
     * Ensures the call works as expected.
     */
    @Test
    public void testGetForStoragePool() {
        StorageDomain result = dao.getForStoragePool(existingDomain.getId(), new Guid("6d849ebf-755f-4552-ad09-9a090cda105d"));

        assertGetResult(result);
    }

    /**
     * Ensures that all instances are returned.
     */
    @Test
    public void testGetAll() {
        List<StorageDomain> result = dao.getAll();

        assertCorrectGetAllResult(result);
    }

    /**
     * Ensures that retrieving storage domains works as expected for a privileged user.
     */
    @Test
    public void testGetAllWithPermissionsPrivilegedUser() {
        List<StorageDomain> result = dao.getAll(PRIVILEGED_USER_ID, true);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(NUMBER_OF_STORAGE_DOMAINS_FOR_PRIVELEGED_USER, result.size());
        assertEquals(result.iterator().next(), existingDomain);
    }

    /**
     * Ensures that retrieving storage domains works as expected with filtering disabled for an unprivileged user.
     */
    @Test
    public void testGetAllWithPermissionsDisabledUnprivilegedUser() {
        List<StorageDomain> result = dao.getAll(UNPRIVILEGED_USER_ID, false);

        assertCorrectGetAllResult(result);
    }

    /**
     * Ensures that no storage domains retrieved for an unprivileged user with filtering enabled.
     */
    @Test
    public void testGetAllWithPermissionsUnprivilegedUser() {
        List<StorageDomain> result = dao.getAll(UNPRIVILEGED_USER_ID, true);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    /**
     * Ensures a null collection is returned.
     */
    @Test
    public void testGetAllForStorageDomainWithInvalidDomain() {
        List<StorageDomain> result = dao.getAllForStorageDomain(Guid
                .newGuid());

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    /**
     * Ensures that the right set of domains are returned.
     */
    @Test
    public void testGetAllForStorageDomain() {
        List<StorageDomain> result = dao
                .getAllForStorageDomain(existingDomain.getId());

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    /**
     * Ensures an empty list is returned for an invalid connection.
     */
    @Test
    public void testGetAllForConnectionWithInvalidConnection() {
        List<StorageDomain> result = dao.getAllForConnection(RandomUtils.instance().nextString(10));

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    /**
     * Ensures that the right collection is returned for a given connection.
     */
    @Test
    public void testGetAllForConnection() {
        List<StorageDomain> result = dao.getAllForConnection(EXISTING_CONNECTION);

        assertGetAllForStoragePoolResult(result, EXISTING_STORAGE_POOL_ID);
    }

    /**
     * Ensures an empty list is returned for an invalid connection.
     */
    @Test
    public void testGetAllByStoragePoolAndConnectionWithInvalidConnection() {
        List<StorageDomain> result =
                dao.getAllByStoragePoolAndConnection(EXISTING_STORAGE_POOL_ID, RandomUtils.instance().nextString(10));

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    /**
     * Ensures an empty list is returned for an invalid storage pool id.
     */
    @Test
    public void testGetAllByStoragePoolAndConnectionWithInvalidStoragePool() {
        List<StorageDomain> result =
                dao.getAllByStoragePoolAndConnection(Guid.newGuid(), EXISTING_CONNECTION);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    /**
     * Ensures an empty list is returned for an invalid storage pool id and connection.
     */
    @Test
    public void testGetAllByStoragePoolAndConnectionWithInvalidInput() {
        List<StorageDomain> result =
                dao.getAllByStoragePoolAndConnection(Guid.newGuid(), RandomUtils.instance().nextString(10));

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    /**
     * Ensures that the right collection is returned for a given connection.
     */
    @Test
    public void testGetAllByStoragePoolAndConnection() {
        List<StorageDomain> result =
                dao.getAllByStoragePoolAndConnection(EXISTING_STORAGE_POOL_ID, EXISTING_CONNECTION);

        assertGetAllForStoragePoolResult(result, EXISTING_STORAGE_POOL_ID);
    }

    /**
     * Ensures an empty list is returned for an invalid pool.
     */
    @Test
    public void testGetAllForStoragePoolWithInvalidPool() {
        List<StorageDomain> result = dao.getAllForStoragePool(Guid.newGuid());

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    /**
     * Ensures that the right collection is returned for a given storage pool.
     */
    @Test
    public void testGetAllForStoragePool() {
        List<StorageDomain> result = dao.getAllForStoragePool(EXISTING_STORAGE_POOL_ID);

        assertGetAllForStoragePoolResult(result, EXISTING_STORAGE_POOL_ID);
    }

    /**
     * Ensures that the right collection is returned for a given storage pool with filtering for a privileged user.
     */
    @Test
    public void testGetAllForStoragePoolWithPermissionsPrivilegedUser() {
        Guid storagePoolId = new Guid("6d849ebf-755f-4552-ad09-9a090cda105d");
        List<StorageDomain> result = dao.getAllForStoragePool(storagePoolId, PRIVILEGED_USER_ID, true);

        assertGetAllForStoragePoolResult(result, storagePoolId);
    }

    /**
     * Ensures that the right collection is returned for a given storage pool with filtering disabled for an unprivileged user.
     */
    @Test
    public void testGetAllForStoragePoolWithPermissionsDisabledUnprivilegedUser() {
        List<StorageDomain> result = dao.getAllForStoragePool(EXISTING_STORAGE_POOL_ID, UNPRIVILEGED_USER_ID, false);

        assertGetAllForStoragePoolResult(result, EXISTING_STORAGE_POOL_ID);
    }

    /**
     * Ensures that an empty collection is returned for a given storage pool for an unprivileged user.
     */
    @Test
    public void testGetAllForStoragePoolWithPermissionsUnprivilegedUser() {
        List<StorageDomain> result = dao.getAllForStoragePool(EXISTING_STORAGE_POOL_ID, UNPRIVILEGED_USER_ID, true);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    /**
     * Asserts the result returned from {@link StorageDomainDAO#getAllForStoragePool(Guid)} is correct
     * @param result The result to check
     */
    private static void assertGetAllForStoragePoolResult(List<StorageDomain> result, Guid expectedStoragePoolId) {
        assertNotNull(result);
        assertFalse(result.isEmpty());
        for (StorageDomain domain : result) {
            assertEquals(expectedStoragePoolId, domain.getStoragePoolId());
        }
    }

    @Test
    public void testGetPermittedStorageDomains() {
        List<StorageDomain> result =
                dao.getPermittedStorageDomainsByStoragePool(EXISTING_USER_ID,
                        ActionGroup.CONFIGURE_VM_STORAGE,
                        new Guid("6d849ebf-755f-4552-ad09-9a090cda105d"));
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(result.get(0).getId(), existingDomain.getId());
    }

    @Test
    public void testGetNonePermittedStorageDomains() {
        List<StorageDomain> result =
                dao.getPermittedStorageDomainsByStoragePool(EXISTING_USER_ID,
                        ActionGroup.CONSUME_QUOTA,
                        new Guid("6d849ebf-755f-4552-ad09-9a090cda105d"));
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    /**
     * Asserts that the existing Storage Domain exists and has VMs and VM Templates, the after remove asserts
     * that the existing domain is removed along with the VM and VM Templates
     */
    @Test
    public void testRemove() {
        List<VM> vms = getDbFacade().getVmDao().getAllForStorageDomain(EXISTING_DOMAIN_ID);
        List<VmTemplate> templates = getDbFacade().getVmTemplateDao().getAllForStorageDomain(EXISTING_DOMAIN_ID);
        BaseDisk diskImage = getDbFacade().getBaseDiskDao().get(FixturesTool.DISK_ID);

        assertNotNull(diskImage);
        assertFalse(vms.isEmpty());
        assertFalse(templates.isEmpty());

        assertNotNull(dao.get(EXISTING_DOMAIN_ID));

        dao.remove(existingDomain.getId());

        assertNull(dao.get(EXISTING_DOMAIN_ID));

        for (VM vm : vms) {
            assertNull(getDbFacade().getVmDao().get(vm.getId()));
        }

        for (VmTemplate template : templates) {
            assertNull(getDbFacade().getVmTemplateDao().get(template.getId()));
        }
        assertNull(getDbFacade().getBaseDiskDao().get(FixturesTool.DISK_ID));
    }

    @Test
    public void testGetNumberOfImagesInExistingDomain() {
        long numOfImages = dao.getNumberOfImagesInStorageDomain(EXISTING_DOMAIN_ID);
        assertEquals("Number of images on storage domain different than expected", NUMBER_OF_IMAGES_ON_EXISTING_DOMAIN, numOfImages);
    }

    @Test
    public void testGetNumberOfImagesInNonExistingDomain() {
        long numOfImages = dao.getNumberOfImagesInStorageDomain(Guid.newGuid());
        assertEquals("Number of images on a non existing domain should be 0", 0, numOfImages);
    }

    /**
     * Asserts that the existing Storage Domain exists and has VMs and VM Templates, the after remove asserts
     * that the existing domain is removed along with the VM and VM Templates
     */
    @Test
    public void testRemoveEntitesFromStorageDomain() {
        List<VM> vms = getDbFacade().getVmDao().getAllForStorageDomain(EXISTING_DOMAIN_ID);
        List<VmTemplate> templates = getDbFacade().getVmTemplateDao().getAllForStorageDomain(EXISTING_DOMAIN_ID);
        BaseDisk diskImage = getDbFacade().getBaseDiskDao().get(FixturesTool.DISK_ID);

        assertNotNull(diskImage);
        assertFalse(vms.isEmpty());
        assertFalse(templates.isEmpty());

        assertNotNull(dao.get(EXISTING_DOMAIN_ID));

        dao.removeEntitesFromStorageDomain(existingDomain.getId());

        for (VM vm : vms) {
            assertNull(getDbFacade().getVmDao().get(vm.getId()));
        }

        for (VmTemplate template : templates) {
            assertNull(getDbFacade().getVmTemplateDao().get(template.getId()));
        }
        assertNull(getDbFacade().getBaseDiskDao().get(FixturesTool.DISK_ID));
    }

    @Test
    public void testAllByConnectionId() {
        List<StorageDomain> domains = dao.getAllByConnectionId(new Guid("0cc146e8-e5ed-482c-8814-270bc48c297f"));
        assertEquals("Unexpected number of storage domains by connection id", domains.size(), 1);
        assertEquals("Wrong storage domain id for search by connection id",
                domains.get(0).getId(),
                new Guid("c2211b56-8869-41cd-84e1-78d7cb96f31d"));
    }

    @Test
    public void testContainsUnregisteredEntities() {
        StorageDomain storageDomain = dao.get(FixturesTool.STORAGE_DOAMIN_NFS2_1);
        assertTrue(storageDomain.isContainsUnregisteredEntities());
    }

    @Test
    public void testNotContainsUnregisteredEntities() {
        StorageDomain storageDomain = dao.get(EXISTING_DOMAIN_ID);
        assertFalse(storageDomain.isContainsUnregisteredEntities());
    }

    /**
     * Asserts the result from {@link StorageDomainDAO#getAll()} is correct without filtering
     *
     * @param result A list of storage domains to assert
     */
    private static void assertCorrectGetAllResult(List<StorageDomain> result) {
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }
}

package org.ovirt.engine.core.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NGuid;
import org.ovirt.engine.core.utils.RandomUtils;

public class StorageDomainDAOTest extends BaseDAOTestCase {
    private static final int NUMBER_OF_STORAGE_DOMAINS_FOR_PRIVELEGED_USER = 1;

    private static final Guid EXISTING_DOMAIN_ID = FixturesTool.STORAGE_DOAMIN_SCALE_SD5;
    private static final Guid EXISTING_STORAGE_POOL_ID = new Guid("6d849ebf-755f-4552-ad09-9a090cda105d");
    private static final String EXISTING_CONNECTION = "10.35.64.25";
    private static final Guid EXISTING_USER_ID = new Guid("9bf7c640-b620-456f-a550-0348f366544b");

    private StorageDomainDAO dao;
    private StorageDomain existingDomain;
    private StorageDomainStatic newStaticDomain;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        dao = dbFacade.getStorageDomainDao();
        existingDomain = dao.get(EXISTING_DOMAIN_ID);

        newStaticDomain = new StorageDomainStatic();
        newStaticDomain.setstorage("fDMzhE-wx3s-zo3q-Qcxd-T0li-yoYU-QvVePl");
    }

    /**
     * Ensures that retrieving the id works.
     */
    @Test
    public void testGetMasterStorageDomainIdForPool() {
        Guid result = dao.getMasterStorageDomainIdForPool(EXISTING_STORAGE_POOL_ID);

        assertNotNull(result);
        assertEquals(EXISTING_DOMAIN_ID, result);
    }

    /**
     * Ensures that nothing is returned when the id is invalid.
     */
    @Test
    public void testGetWithInvalidId() {
        StorageDomain result = dao.get(Guid.NewGuid());

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
        StorageDomain result = dao.getForStoragePool(Guid.NewGuid(), EXISTING_STORAGE_POOL_ID);

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
        List<StorageDomain> result = dao.getAllStorageDomainsByImageId(Guid.NewGuid());
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
                NGuid.NewGuid());

        assertNull(result);
    }

    /**
     * Ensures the call works as expected.
     */
    @Test
    public void testGetForStoragePool() {
        StorageDomain result = dao.getForStoragePool(existingDomain.getId(), EXISTING_STORAGE_POOL_ID);

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
                .NewGuid());

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

        assertGetAllForStoragePoolResult(result);
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
                dao.getAllByStoragePoolAndConnection(Guid.NewGuid(), EXISTING_CONNECTION);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    /**
     * Ensures an empty list is returned for an invalid storage pool id and connection.
     */
    @Test
    public void testGetAllByStoragePoolAndConnectionWithInvalidInput() {
        List<StorageDomain> result =
                dao.getAllByStoragePoolAndConnection(Guid.NewGuid(), RandomUtils.instance().nextString(10));

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

        assertGetAllForStoragePoolResult(result);
    }

    /**
     * Ensures an empty list is returned for an invalid pool.
     */
    @Test
    public void testGetAllForStoragePoolWithInvalidPool() {
        List<StorageDomain> result = dao.getAllForStoragePool(Guid.NewGuid());

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    /**
     * Ensures that the right collection is returned for a given storage pool.
     */
    @Test
    public void testGetAllForStoragePool() {
        List<StorageDomain> result = dao.getAllForStoragePool(EXISTING_STORAGE_POOL_ID);

        assertGetAllForStoragePoolResult(result);
    }

    /**
     * Ensures that the right collection is returned for a given storage pool with filtering for a privileged user.
     */
    @Test
    public void testGetAllForStoragePoolWithPermissionsPrivilegedUser() {
        List<StorageDomain> result = dao.getAllForStoragePool(EXISTING_STORAGE_POOL_ID, PRIVILEGED_USER_ID, true);

        assertGetAllForStoragePoolResult(result);
    }

    /**
     * Ensures that the right collection is returned for a given storage pool with filtering disabled for an unprivileged user.
     */
    @Test
    public void testGetAllForStoragePoolWithPermissionsDisabledUnprivilegedUser() {
        List<StorageDomain> result = dao.getAllForStoragePool(EXISTING_STORAGE_POOL_ID, UNPRIVILEGED_USER_ID, false);

        assertGetAllForStoragePoolResult(result);
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
    private static void assertGetAllForStoragePoolResult(List<StorageDomain> result) {
        assertNotNull(result);
        assertFalse(result.isEmpty());
        for (StorageDomain domain : result) {
            assertEquals(EXISTING_STORAGE_POOL_ID, domain.getstorage_pool_id());
        }
    }

    @Test
    public void testGetPermittedStorageDomains() {
        List<StorageDomain> result =
                dao.getPermittedStorageDomainsByStoragePool(EXISTING_USER_ID,
                        ActionGroup.CONFIGURE_VM_STORAGE,
                        EXISTING_STORAGE_POOL_ID);
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(result.get(0).getId(), existingDomain.getId());
    }

    @Test
    public void testGetNonePermittedStorageDomains() {
        List<StorageDomain> result =
                dao.getPermittedStorageDomainsByStoragePool(EXISTING_USER_ID,
                        ActionGroup.CONSUME_QUOTA,
                        EXISTING_STORAGE_POOL_ID);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testGetPermittedStorageDomainsById() {
        StorageDomain result = dao.getPermittedStorageDomainsById(EXISTING_USER_ID,
                        ActionGroup.CONFIGURE_VM_STORAGE,
                        existingDomain.getId());
        assertNotNull(result);
        assertEquals(result.getId(), existingDomain.getId());
    }

    /**
     * Asserts that the existing Storage Domain exists and has VMs and VM Templates, the after remove asserts
     * that the existing domain is removed along with the VM and VM Templates
     */
    @Test
    public void testRemove() {
        List<VM> vms = getDbFacade().getVmDao().getAllForStorageDomain(EXISTING_DOMAIN_ID);
        List<VmTemplate> templates = getDbFacade().getVmTemplateDao().getAllForStorageDomain(EXISTING_DOMAIN_ID);

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

    }

    @Test
    public void testAllByConnectionId() {
        List<StorageDomain> domains = dao.getAllByConnectionId(FixturesTool.EXISTING_STORAGE_CONNECTION_ID);
        assertEquals("Unexpected number of storage domains by connection id", domains.size(), 1);
        assertEquals("Wrong storage domain id for search by connection id",
                domains.get(0).getId(),
                FixturesTool.EXISTING_DOMAIN_ID_FOR_CONNECTION_ID);
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

package org.ovirt.engine.core.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import javax.inject.Inject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmPool;
import org.ovirt.engine.core.common.businessentities.VmPoolMap;
import org.ovirt.engine.core.compat.Guid;

public class VmPoolDaoTest extends BaseDaoTestCase<VmPoolDao> {
    private static final Guid USER_ID = new Guid("9bf7c640-b620-456f-a550-0348f366544b");
    private static final Guid DELETABLE_VM_POOL_ID = new Guid("103cfd1d-18b1-4790-8a0c-1e52621b0078");
    private static final Guid EXISTING_VM_POOL_ID = new Guid("103cfd1d-18b1-4790-8a0c-1e52621b0076");
    private static final Guid FREE_VM_ID = FixturesTool.VM_RHEL5_POOL_51;
    private static final Guid EXISTING_VM_ID = FixturesTool.VM_RHEL5_POOL_57;
    private static final int VM_POOL_COUNT = 3;
    private VmPool existingVmPool;
    private VmPool deletableVmPool;
    private VmPool newVmPool;
    private VmPoolMap newVmPoolMap;
    @Inject
    private VmDao vmDao;

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setUp();

        existingVmPool = dao.get(EXISTING_VM_POOL_ID);
        deletableVmPool = dao.get(DELETABLE_VM_POOL_ID);

        newVmPool = new VmPool();
        newVmPool.setName("New VM Pool");
        newVmPool.setVmPoolDescription("This is a new VM pool.");
        newVmPool.setClusterId(FixturesTool.CLUSTER);

        newVmPoolMap = new VmPoolMap(FREE_VM_ID, EXISTING_VM_POOL_ID);
    }

    @Test
    public void testRemoveVmFromPool() {
        assertNotNull(vmDao.get(EXISTING_VM_ID).getVmPoolId());

        dao.removeVmFromVmPool(EXISTING_VM_ID);

        assertNull(vmDao.get(EXISTING_VM_ID).getVmPoolId());
    }

    /**
     * Ensures that null is returned when the id is invalid.
     */
    @Test
    public void testGetVmPoolWithInvalidId() {
        VmPool result = dao.get(Guid.newGuid());

        assertNull(result);
    }

    /**
     * Ensures that getting a VM pool works as expected.
     */
    @Test
    public void testGetVmPool() {
        VmPool result = dao.get(existingVmPool.getVmPoolId());

        assertGetResult(result);
    }

    @Test
    public void testGetFilteredWithPermissions() {
        VmPool result = dao.get(existingVmPool.getVmPoolId(), PRIVILEGED_USER_ID, true);

        assertGetResult(result);
    }

    @Test
    public void testGetFilteredWithPermissionsNoPermissions() {
        VmPool result = dao.get(existingVmPool.getVmPoolId(), UNPRIVILEGED_USER_ID, true);

        assertNull(result);
    }

    @Test
    public void testGetFilteredWithPermissionsNoPermissionsAndNoFilter() {
        VmPool result = dao.get(existingVmPool.getVmPoolId(), UNPRIVILEGED_USER_ID, false);

        assertGetResult(result);
    }

    private void assertGetResult(VmPool result) {
        assertNotNull(result);
        assertEquals(existingVmPool, result);
    }

    /**
     * Ensures that getting a VM pool by an invalid name returns null.
     */
    @Test
    public void testGetByNameWithInvalidName() {
        VmPool result = dao.getByName("farkle");

        assertNull(result);
    }

    /**
     * Ensures that getting a VM pool by name works as expected.
     */
    @Test
    public void testGetByName() {
        VmPool result = dao.getByName(existingVmPool.getName());

        assertNotNull(result);
        assertEquals(existingVmPool, result);
    }

    /**
     * Ensures the right number of pools are returned.
     */
    @Test
    public void testGetAllVmPools() {
        List<VmPool> result = dao.getAll();

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(VM_POOL_COUNT, result.size());
    }

    /**
     * Ensures an empty collection is returned.
     */
    @Test
    public void testGetAllVmPoolsForUserWithNoVmPools() {
        List<VmPool> result = dao.getAllForUser(Guid.newGuid());

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    /**
     * Ensures a collection of pools are returned.
     */
    @Test
    public void testGetAllVmPoolsForUser() {
        List<VmPool> result = dao.getAllForUser(USER_ID);

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    /**
     * Ensures that saving a VM pool works as expected.
     */
    @Test
    public void testSaveVmPool() {
        dao.save(newVmPool);

        VmPool result = dao.getByName(newVmPool.getName());

        assertNotNull(result);
        assertEquals(newVmPool, result);
    }

    /**
     * Ensures that updating a VM pool works as expected.
     */
    @Test
    public void testUpdateVmPool() {
        existingVmPool.setVmPoolDescription("This is an updated VM pool.");

        dao.update(existingVmPool);

        VmPool result = dao.get(existingVmPool.getVmPoolId());

        assertEquals(existingVmPool, result);
    }

    /**
     * Ensures removing a VM pool works as expected.
     */
    @Test
    public void testRemoveVmPool() {
        dao.remove(deletableVmPool.getVmPoolId());

        VmPool result = dao.get(deletableVmPool.getVmPoolId());

        assertNull(result);
    }

    @Test
    public void testAddVmToPool() {
        assertNull(vmDao.get(FREE_VM_ID).getVmPoolId());

        dao.addVmToPool(newVmPoolMap);

        assertNotNull(vmDao.get(FREE_VM_ID).getVmPoolId());
    }

    @Test
    public void testGetVmMapsInVmPoolByVmPoolIdAndStatus() {
        List<VmPoolMap> result = dao.getVmMapsInVmPoolByVmPoolIdAndStatus(
                existingVmPool.getVmPoolId(), VMStatus.MigratingFrom);

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    /**
     * Ensures that a VM from a vm pool is returned for a privileged user with filtering enabled.
     */
    @Test
    public void getVmDataFromPoolByPoolGuidWithPermissionsForPriviligedUser() {
        VM result = dao.getVmDataFromPoolByPoolGuid(EXISTING_VM_POOL_ID, PRIVILEGED_USER_ID, true);
        assertCorrectGetVmDataResult(result);
    }

    /**
     * Ensures a VM from a vm pool by is returned for a non privileged user with filtering disabled.
     */
    @Test
    public void getVmDataFromPoolByPoolGuidWithoutPermissionsForNonPriviligedUser() {
        VM result = dao.getVmDataFromPoolByPoolGuid(EXISTING_VM_POOL_ID, UNPRIVILEGED_USER_ID, false);
        assertCorrectGetVmDataResult(result);
    }

    /**
     * Ensures that no VM is returned for a non privileged user with filtering enabled.
     */
    @Test
    public void getVmDataFromPoolByPoolGuidWithPermissionsForNonPriviligedUser() {
        VM result = dao.getVmDataFromPoolByPoolGuid(EXISTING_VM_POOL_ID, UNPRIVILEGED_USER_ID, true);
        assertNull(result);
    }

    private void assertCorrectGetVmDataResult(VM result) {
        assertNotNull(result);
        assertEquals(EXISTING_VM_POOL_ID, result.getVmPoolId());
    }
}

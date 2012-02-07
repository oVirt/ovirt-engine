package org.ovirt.engine.core.dao;

import static org.junit.Assert.*;

import java.util.Date;
import java.util.List;

import org.junit.Test;

import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.time_lease_vm_pool_map;
import org.ovirt.engine.core.common.businessentities.vm_pool_map;
import org.ovirt.engine.core.common.businessentities.vm_pools;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NGuid;

public class VmPoolDAOTest extends BaseDAOTestCase {
    private static final Guid USER_ID = new Guid("9bf7c640-b620-456f-a550-0348f366544b");
    private static final Guid VDS_GROUP_ID = new Guid("b399944a-81ab-4ec5-8266-e19ba7c3c9d1");
    private static final Guid DELETABLE_VM_POOL_ID = new Guid("103cfd1d-18b1-4790-8a0c-1e52621b0078");
    private static final Guid EXISTING_VM_POOL_ID = new Guid("103cfd1d-18b1-4790-8a0c-1e52621b0076");
    private static final Guid FREE_VM_ID = new Guid("77296e00-0cad-4e5a-9299-008a7b6f4356");
    private static final Guid EXISTING_VM_ID = new Guid("77296e00-0cad-4e5a-9299-008a7b6f4355");
    private static final int VM_POOL_COUNT = 3;
    private static final Guid EXISTING_LEASE_ID = new Guid("87c27aec-0ae8-4071-ba64-4c8fba70b2a4");
    private static final Guid FREE_VM_POOL_ID = new Guid("103cfd1d-18b1-4790-8a0c-1e52621b0077");
    private VmPoolDAO dao;
    private vm_pools existingVmPool;
    private vm_pools deletableVmPool;
    private vm_pools newVmPool;
    private vm_pool_map newVmPoolMap;
    private vm_pool_map existingVmPoolMap;
    private time_lease_vm_pool_map existingTimeLeaseVmPoolMap;
    private time_lease_vm_pool_map newTimeLeaseVmPoolMap;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        dao = prepareDAO(dbFacade.getVmPoolDAO());

        existingVmPool = dao.get(EXISTING_VM_POOL_ID);
        deletableVmPool = dao.get(DELETABLE_VM_POOL_ID);

        newVmPool = new vm_pools();
        newVmPool.setvm_pool_name("New VM Pool");
        newVmPool.setvm_pool_description("This is a new VM pool.");
        newVmPool.setvds_group_id(VDS_GROUP_ID);

        existingVmPoolMap = dao.getVmPoolMapByVmGuid(new Guid("77296e00-0cad-4e5a-9299-008a7b6f4355"));
        newVmPoolMap =
                new vm_pool_map(FREE_VM_ID, existingVmPool.getvm_pool_id());

        existingTimeLeaseVmPoolMap = dao.getTimeLeasedVmPoolMapByIdForVmPool(EXISTING_LEASE_ID, EXISTING_VM_POOL_ID);
        newTimeLeaseVmPoolMap = new time_lease_vm_pool_map(new Date(), Guid.NewGuid(), new Date(), 1, FREE_VM_POOL_ID);
    }

    @Test
    public void testRemoveVmFromPool() {
        int before = dao.getVmPoolsMapByVmPoolId(existingVmPoolMap.getvm_pool_id()).size();

        dao.removeVmFromVmPool(EXISTING_VM_ID);

        int after = dao.getVmPoolsMapByVmPoolId(existingVmPoolMap.getvm_pool_id()).size();

        assertEquals(before - 1, after);

        vm_pool_map result = dao.getVmPoolMapByVmGuid(EXISTING_VM_ID);

        assertNull(result);
    }

    /**
     * Ensures that null is returned when the id is invalid.
     */
    @Test
    public void testGetVmPoolWithInvalidId() {
        vm_pools result = dao.get(NGuid.NewGuid());

        assertNull(result);
    }

    /**
     * Ensures that getting a VM pool works as expected.
     */
    @Test
    public void testGetVmPool() {
        vm_pools result = dao.get(existingVmPool.getvm_pool_id());

        assertNotNull(result);
        assertEquals(existingVmPool, result);
    }

    /**
     * Ensures that getting a VM pool by an invalid name returns null.
     */
    @Test
    public void testGetByNameWithInvalidName() {
        vm_pools result = dao.getByName("farkle");

        assertNull(result);
    }

    /**
     * Ensures that getting a VM pool by name works as expected.
     */
    @Test
    public void testGetByName() {
        vm_pools result = dao.getByName(existingVmPool.getvm_pool_name());

        assertNotNull(result);
        assertEquals(existingVmPool, result);
    }

    /**
     * Ensures the right number of pools are returned.
     */
    @Test
    public void testGetAllVmPools() {
        List<vm_pools> result = dao.getAll();

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(VM_POOL_COUNT, result.size());
    }

    /**
     * Ensures an empty collection is returned.
     */
    @Test
    public void testGetAllVmPoolsForUserWithNoVmPools() {
        List<vm_pools> result = dao.getAllForUser(Guid.NewGuid());

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    /**
     * Ensures a collection of pools are returned.
     */
    @Test
    public void testGetAllVmPoolsForUser() {
        List<vm_pools> result = dao.getAllForUser(USER_ID);

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    /**
     * Ensures an empty collection is returned.
     */
    @Test
    public void testGetAllVmPoolsForAdGroupWithNoVmPools() {
        // TODO the underlying stored procedure depends on a table that was removed
        //
        // List<vm_pools> result = dao.getAllVmPoolsForAdGroup(Guid.NewGuid());
        //
        // assertNotNull(result);
        // assertTrue(result.isEmpty());
    }

    /**
     * Ensures that saving a VM pool works as expected.
     */
    @Test
    public void testSaveVmPool() {
        dao.save(newVmPool);

        vm_pools result = dao.getByName(newVmPool.getvm_pool_name());

        assertNotNull(result);
        assertEquals(newVmPool, result);
    }

    /**
     * Ensures that updating a VM pool works as expected.
     */
    @Test
    public void testUpdateVmPool() {
        existingVmPool.setvm_pool_description("This is an updated VM pool.");

        dao.update(existingVmPool);

        vm_pools result = dao.get(existingVmPool.getvm_pool_id());

        assertEquals(existingVmPool, result);
    }

    /**
     * Ensures removing a VM pool works as expected.
     */
    @Test
    public void testRemoveVmPool() {
        dao.remove(deletableVmPool.getvm_pool_id());

        vm_pools result = dao.get(deletableVmPool.getvm_pool_id());

        assertNull(result);
    }

    @Test
    public void testGetVmPoolMap() {
        vm_pool_map result = dao.getVmPoolMapByVmGuid(EXISTING_VM_ID);

        assertNotNull(result);
        assertEquals(existingVmPoolMap, result);
    }

    @Test
    public void testAddVmToPool() {
        int before = dao.getVmPoolsMapByVmPoolId(newVmPoolMap.getvm_pool_id()).size();

        dao.addVmToPool(newVmPoolMap);

        int after = dao.getVmPoolsMapByVmPoolId(newVmPoolMap.getvm_pool_id()).size();

        assertEquals(before + 1, after);

        vm_pool_map result = dao.getVmPoolMapByVmGuid(newVmPoolMap.getvm_guid());

        assertNotNull(result);
        assertEquals(newVmPoolMap, result);
    }

    @Test
    public void testGetTimeLeaseVmPoolMap() {
        time_lease_vm_pool_map result =
                dao.getTimeLeasedVmPoolMapByIdForVmPool(existingTimeLeaseVmPoolMap.getid(),
                        existingTimeLeaseVmPoolMap.getvm_pool_id());

        assertNotNull(result);
        assertEquals(existingTimeLeaseVmPoolMap, result);
    }

    @Test
    public void testAddTimeLeaseVmPoolMap() {
        dao.addTimeLeasedVmPoolMap(newTimeLeaseVmPoolMap);

        time_lease_vm_pool_map result =
                dao.getTimeLeasedVmPoolMapByIdForVmPool(newTimeLeaseVmPoolMap.getid(),
                        newTimeLeaseVmPoolMap.getvm_pool_id());

        assertNotNull(result);
        assertEquals(newTimeLeaseVmPoolMap, result);
    }

    @Test
    public void testUpdateTimeLeaseVmPoolMap() {
        existingTimeLeaseVmPoolMap.settype(100 - existingTimeLeaseVmPoolMap.gettype());

        dao.updateTimeLeasedVmPoolMap(existingTimeLeaseVmPoolMap);

        time_lease_vm_pool_map result =
                dao.getTimeLeasedVmPoolMapByIdForVmPool(existingTimeLeaseVmPoolMap.getid(),
                        existingTimeLeaseVmPoolMap.getvm_pool_id());

        assertEquals(existingTimeLeaseVmPoolMap, result);
    }

    @Test
    public void testRemoveTimeLeaseVmPoolMap() {
        dao.removeTimeLeasedVmPoolMap(existingTimeLeaseVmPoolMap.getid(), existingTimeLeaseVmPoolMap.getvm_pool_id());

        time_lease_vm_pool_map result =
                dao.getTimeLeasedVmPoolMapByIdForVmPool(existingTimeLeaseVmPoolMap.getid(),
                        existingTimeLeaseVmPoolMap.getvm_pool_id());

        assertNull(result);
    }

    @Test
    public void testGetAllTimeLeaseVmPoolMaps() {
        List<time_lease_vm_pool_map> result = dao.getAllTimeLeasedVmPoolMaps();

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    public void testGetVmMapsInVmPoolByVmPoolIdAndStatus() {
        List<vm_pool_map> result = dao.getVmMapsInVmPoolByVmPoolIdAndStatus(
                existingVmPool.getvm_pool_id(), VMStatus.MigratingFrom);

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }
}

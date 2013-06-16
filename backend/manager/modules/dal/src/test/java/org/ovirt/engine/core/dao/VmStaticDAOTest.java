/**
 *
 */
package org.ovirt.engine.core.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.apache.commons.lang.NotImplementedException;
import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.OriginType;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NGuid;

/**
 * @author yzaslavs
 *
 */
public class VmStaticDAOTest extends BaseDAOTestCase {
    private static final Guid EXISTING_VM_ID = new Guid("77296e00-0cad-4e5a-9299-008a7b6f4355");
    private static final Guid VDS_STATIC_ID = new Guid("afce7a39-8e8c-4819-ba9c-796d316592e6");
    private static final Guid VDS_GROUP_ID = new Guid("b399944a-81ab-4ec5-8266-e19ba7c3c9d1");

    private VmStaticDAO dao;
    private VmStatic existingVmStatic;
    private VmStatic newVmStatic;
    private VmTemplate vmtemplate;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        dao = dbFacade.getVmStaticDao();
        existingVmStatic = dao.get(EXISTING_VM_ID);
        vmtemplate = dbFacade.getVmTemplateDao().get(
                new Guid("1b85420c-b84c-4f29-997e-0eb674b40b79"));
        newVmStatic = new VmStatic();
        newVmStatic.setId(Guid.NewGuid());
        newVmStatic.setName("New Virtual Machine");
        newVmStatic.setVdsGroupId(VDS_GROUP_ID);
        newVmStatic.setVmtGuid(vmtemplate.getId());
        newVmStatic.setOrigin(OriginType.OVIRT);
        newVmStatic.setQuotaId(Guid.NewGuid());
    }

    /**
     * Ensures that get requires a valid id.
     */
    @Test
    public void testGetWithInvalidId() {
        VmStatic result = dao.get(Guid.NewGuid());

        assertNull(result);
    }

    /**
     * Ensures that get works as expected.
     */
    @Test
    public void testGet() {
        VmStatic result = dao.get(existingVmStatic.getId());

        assertNotNull(result);
        assertEquals(result, existingVmStatic);
    }

    /**
     * Ensures that all VMs are returned.
     */
    @Test
    public void testGetAllStaticByName() {
        List<VmStatic> result = dao.getAllByName("rhel5-pool-50");

        assertNotNull(result);
        assertFalse(result.isEmpty());
        for (VmStatic vm : result) {
            assertEquals("rhel5-pool-50", vm.getName());
        }
    }

    /**
     * Ensures that all VMs are returned from storage pool
     */
    @Test
    public void testGetAllStaticByStoragePool() {
        NGuid spID = dbFacade.getVdsGroupDao().get(newVmStatic.getVdsGroupId()).getStoragePoolId();

        assertNotNull(spID.getValue());

        List<VmStatic> result = dao.getAllByStoragePoolId(spID.getValue());

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    /**
     * Ensures that all static vm details for the specified group and network are returned.
     */
    @Test
    public void testGetAllByGroupAndNetwork() {
        List<VmStatic> result = dao.getAllByGroupAndNetworkName(
                VDS_GROUP_ID, "engine");

        assertNotNull(result);
        assertFalse(result.isEmpty());
        for (VmStatic vm : result) {
            assertEquals(VDS_GROUP_ID, vm.getVdsGroupId());
        }
    }

    /**
     * Ensures that all static VMs for the specified VDS group are returned.
     */
    @Test
    public void testGetAllByVdsGroup() {
        List<VmStatic> result = dao.getAllByVdsGroup(VDS_GROUP_ID);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        for (VmStatic vm : result) {
            assertEquals(VDS_GROUP_ID, vm.getVdsGroupId());
        }
    }

    /**
     * Ensures that the right set of VMs are returned.
     */
    @Test
    public void testGetAllWithFailbackByVds() {
        List<VmStatic> result = dao.getAllWithFailbackByVds(VDS_STATIC_ID);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        for (VmStatic vm : result) {
            assertEquals(VDS_GROUP_ID, vm.getVdsGroupId());
        }
    }

    @Test(expected = NotImplementedException.class)
    public void testGetAll() {
        dao.getAll();
    }


    @Test
    public void testSave() {
        dao.save(newVmStatic);
        VmStatic result = dao.get(newVmStatic.getId());
        assertNotNull(result);
        assertEquals(newVmStatic, result);
    }

    @Test
    public void testUpdate() {
        existingVmStatic.setDescription("updated");
        dao.update(existingVmStatic);
        VmStatic result = dao.get(EXISTING_VM_ID);
        assertNotNull(result);
        assertEquals(existingVmStatic, result);
    }

    @Test
    public void testRemove() {
        for (Snapshot s : dbFacade.getSnapshotDao().getAll()) {
            dbFacade.getSnapshotDao().remove(s.getId());
        }

        dao.remove(EXISTING_VM_ID);
        VmStatic result = dao.get(EXISTING_VM_ID);
        assertNull(result);
    }

    @Test
    public void testGetAllNamesPinnedToHostReturnsNothingForRandomHost() throws Exception {
        assertTrue(dao.getAllNamesPinnedToHost(Guid.NewGuid()).isEmpty());
    }


    @Test
    public void testGetDbGeneration() {
        Long version = dao.getDbGeneration(FixturesTool.VM_RHEL5_POOL_50);
        assertNotNull("db generation shouldn't be null", version);
        assertEquals("db generation should be 1 by default for vm", 1, version.longValue());
    }

    @Test
    public void testIncrementDbGenerationForAllInStoragePool() {
        dao.incrementDbGenerationForAllInStoragePool(FixturesTool.STORAGE_POOL_RHEL6_ISCSI_OTHER);

        Long version = dao.getDbGeneration(FixturesTool.VM_RHEL5_POOL_50);
        assertEquals("db geneeration wasn't incremented to all vms in pool", 2, version.longValue());
        version = dao.getDbGeneration(FixturesTool.VM_RHEL5_POOL_51);
        assertEquals("db generation wasn't incremented to all vms in pool", 2, version.longValue());
        version = dao.getDbGeneration(FixturesTool.VM_RHEL5_POOL_60);
        assertEquals("db generation was incremented for vm in different pool", 1, version.longValue());
    }

    @Test
    public void testIncrementDbGeneration() {
        dao.incrementDbGeneration(FixturesTool.VM_RHEL5_POOL_50);
        Long version = dao.getDbGeneration(FixturesTool.VM_RHEL5_POOL_50);
        assertEquals("db generation wasn't incremented as expected", 2, version.longValue());
    }

    @Test
    public void testGetAllNamesPinnedToHostReturnsNothingForHostButNotPinned() throws Exception {
        assertTrue(dao.getAllNamesPinnedToHost(new Guid("afce7a39-8e8c-4819-ba9c-796d316592e7")).isEmpty());
    }

    @Test
    public void testGetAllNamesPinnedToHostReturnsVmNameForHostPinned() throws Exception {
        List<String> namesPinnedToHost = dao.getAllNamesPinnedToHost(VDS_STATIC_ID);

        assertFalse(namesPinnedToHost.isEmpty());
        assertTrue(namesPinnedToHost.contains(existingVmStatic.getName()));
    }
}

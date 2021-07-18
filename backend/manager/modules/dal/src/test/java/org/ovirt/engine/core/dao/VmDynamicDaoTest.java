package org.ovirt.engine.core.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.BusinessEntity;
import org.ovirt.engine.core.common.businessentities.GuestAgentStatus;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDynamic;
import org.ovirt.engine.core.compat.Guid;

public class VmDynamicDaoTest extends BaseGenericDaoTestCase<Guid, VmDynamic, VmDynamicDao> {
    private static final int DYNAMIC_RUNNING_COUNT = 4;

    /**
     * Test the {@link VmDynamicDao#save(BusinessEntity)} method by adding a vm_dynamic record to a template.
     * While this doesn't make any sense from a business logic perspective, it's perfectly legal from a database
     * perspective, and helps avoid dependencies on the {@link VmDynamicDao#remove(Guid)} method.
     */
    @Override
    protected VmDynamic generateNewEntity() {
        VmDynamic newEntity = new VmDynamic();
        newEntity.setId(FixturesTool.VM_TEMPLATE_RHEL5);
        newEntity.setStatus(VMStatus.NotResponding);
        return newEntity;
    }

    @Override
    protected void updateExistingEntity() {
        existingEntity.setVmHost("farkle.redhat.com");
        existingEntity.setRunOnce(!existingEntity.isRunOnce());
    }

    @Override
    protected Guid getExistingEntityId() {
        return FixturesTool.VM_RHEL5_POOL_57;
    }

    @Override
    protected Guid generateNonExistingId() {
        return Guid.newGuid();
    }

    @Override
    protected int getEntitiesTotalCount() {
        return 11;
    }

    /**
     * Gets all dynamic details for VMs running on a specific VDS.
     */
    @Test
    public void testGetAllForRunningForVds() {
        List<VmDynamic> result = dao.getAllRunningForVds(FixturesTool.VDS_RHEL6_NFS_SPM);
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(DYNAMIC_RUNNING_COUNT, result.size());
        result.forEach(vm -> assertEquals(FixturesTool.VDS_RHEL6_NFS_SPM, vm.getRunOnVds()));
    }

    @Test
    public void testIsAnyVmRunOnVds() {
        assertTrue(dao.isAnyVmRunOnVds(FixturesTool.VDS_RHEL6_NFS_SPM));
        assertFalse(dao.isAnyVmRunOnVds(FixturesTool.GLUSTER_BRICK_SERVER1));
    }

    /**
     * Ensures updating the dynamic status aspect of the VM works.
     */
    @Test
    public void testUpdateStatus() {
        existingEntity.setStatus(VMStatus.Down);
        dao.updateStatus(existingEntity.getId(), existingEntity.getStatus());
        VmDynamic after = dao.get(existingEntity.getId());
        assertEquals(existingEntity, after);
    }

    @Test
    public void testUpdateAll() {
        VmDynamic existingVm2 = dao.get(FixturesTool.VM_RHEL5_POOL_51);
        existingEntity.setStatus(VMStatus.Down);
        existingVm2.setIp("111");
        existingVm2.setFqdn("localhost.localdomain");

        dao.updateAll(Arrays.asList(existingEntity, existingVm2));

        assertEquals(existingEntity, dao.get(existingEntity.getId()));
        assertEquals(existingVm2, dao.get(existingVm2.getId()));
    }

    /**
     * Make sure that saving a new console user id and console user name to a virtual machine
     * without a previous console user succeeds and returns {@code true}.
     */
    @Test
    public void testUpdateConsoleUserWithOptimisticLockingSuccess() {
        VmDynamic vmWithoutConsoleUser = dao.get(FixturesTool.VM_RHEL5_POOL_51);
        vmWithoutConsoleUser.setConsoleUserId(new Guid("9bf7c640-b620-456f-a550-0348f366544b"));

        boolean result = dao.updateConsoleUserWithOptimisticLocking(vmWithoutConsoleUser);

        assertTrue(result);
    }

    /**
     * Make sure that saving a new console user id and console user name to a virtual machine
     * that already as a previous console user fails and returns {@code false}.
     */
    @Test
    public void testUpdateConsoleUserWithOptimisticLockingFailure() {
        VmDynamic vmWithoutConsoleUser = dao.get(FixturesTool.VM_RHEL5_POOL_57);
        vmWithoutConsoleUser.setConsoleUserId(new Guid("9bf7c640-b620-456f-a550-0348f366544b"));

        boolean result = dao.updateConsoleUserWithOptimisticLocking(vmWithoutConsoleUser);

        assertFalse(result);
    }

    @Test
    public void testClearMigratingToVds() {
        VmDynamic vmDynamic = dao.get(FixturesTool.VM_RHEL5_POOL_51);
        assertNotNull(vmDynamic.getMigratingToVds(), "migrating_to_vds field should not be null before we clear it");

        dao.clearMigratingToVds(FixturesTool.VM_RHEL5_POOL_51);

        vmDynamic = dao.get(FixturesTool.VM_RHEL5_POOL_51);
        assertNull(vmDynamic.getMigratingToVds(), "migrating_to_vds field should be null after we clear it");
    }

    @Test
    public void testGuestAgentStatus() {
        Guid vmId = FixturesTool.VM_RHEL5_POOL_51;
        VmDynamic vmDynamic = dao.get(vmId);
        vmDynamic.setOvirtGuestAgentStatus(GuestAgentStatus.UpdateNeeded);
        dao.update(vmDynamic);
        vmDynamic = dao.get(vmId);
        assertEquals(vmDynamic.getOvirtGuestAgentStatus().getValue(), GuestAgentStatus.UpdateNeeded.getValue());
    }

    @Test
    public void testUpdateToUnknown() {
        VmDynamic existingVm2 = dao.get(FixturesTool.VM_RHEL5_POOL_51);
        VmDynamic existingVm3 = dao.get(FixturesTool.VM_RHEL5_POOL_50);
        dao.updateVmsToUnknown(Arrays.asList(existingEntity.getId(), existingVm2.getId()));
        assertEquals(VMStatus.Unknown, dao.get(existingEntity.getId()).getStatus());
        assertEquals(VMStatus.Unknown, dao.get(existingVm2.getId()).getStatus());
        assertNotEquals(VMStatus.Unknown, dao.get(existingVm3.getId()).getStatus());
    }

    /**
     * Ensures that getting all VmDynamics with run_on_vds set for specific action group works as expected.
     */
    @Test
    public void testGetAllRunningForUserAndActionGroup() {
        List<VmDynamic> result = dao.getAllRunningForUserAndActionGroup(PRIVILEGED_USER_ID, ActionGroup.CONNECT_TO_SERIAL_CONSOLE);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(3, result.size());
    }

    /**
     * Ensures that getting all VmDynamics for unprivileged specific action group works as expected.
     */
    @Test
    public void testGetAllRunningForUnPrivilegedUserAndActionGroup() {
        List<VmDynamic> result = dao.getAllRunningForUserAndActionGroup(UNPRIVILEGED_USER_ID, ActionGroup.CONNECT_TO_SERIAL_CONSOLE);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

}

package org.ovirt.engine.core.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.compat.Guid;

public class DiskDaoTest extends BaseReadDaoTestCase<Guid, Disk, DiskDao> {

    private static final int TOTAL_DISK_IMAGES = 3;
    private static final Guid EXISTING_VM_TEMPLATE = new Guid("1b85420c-b84c-4f29-997e-0eb674b40b79");

    @Override
    protected Guid getExistingEntityId() {
        return new Guid("1b26a52b-b60f-44cb-9f46-3ef333b04a34");
    }

    @Override
    protected DiskDao prepareDao() {
        return dbFacade.getDiskDao();
    }

    @Override
    protected Guid generateNonExistingId() {
        return new Guid();
    }

    @Override
    protected int getEneitiesTotalCount() {
        return TOTAL_DISK_IMAGES + DiskLunMapDaoTest.TOTAL_DISK_LUN_MAPS;
    }

    @Test
    public void testGetAllForVm() {
        List<Disk> result = dao.getAllForVm(EXISTING_VM_TEMPLATE);

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    public void testGetAllForVMFilteredWithPermissions() {
        // test user 3 - has permissions
        List<Disk> disks = dao.getAllForVm(FixturesTool.VM_RHEL5_POOL_57, PRIVILEGED_USER_ID, true);
        assertFullGetAllForVMResult(disks);
    }

    @Test
    public void testGetAllForVMFilteredWithPermissionsNoPermissions() {
        // test user 2 - hasn't got permissions
        List<Disk> disks = dao.getAllForVm(FixturesTool.VM_RHEL5_POOL_57, UNPRIVILEGED_USER_ID, true);
        assertTrue("VM should have no disks viewable to the user", disks.isEmpty());
    }

    @Test
    public void testGetAllForVMFilteredWithPermissionsNoPermissionsAndNoFilter() {
        // test user 2 - hasn't got permissions, but no filtering was requested
        List<Disk> disks = dao.getAllForVm(FixturesTool.VM_RHEL5_POOL_57, UNPRIVILEGED_USER_ID, false);
        assertFullGetAllForVMResult(disks);
    }

    @Test
    public void testGetAllForVM() {
        List<Disk> disks = dao.getAllForVm(FixturesTool.VM_RHEL5_POOL_57);
        assertFullGetAllForVMResult(disks);
    }

    @Test
    public void testGetAllAttachableDisksByPoolIdNull() {
        List<Disk> result =
                dao.getAllAttachableDisksByPoolId(null, null, false);

        assertTrue(result.isEmpty());
    }

    @Test
    public void testGetAllAttachableDisksByPoolId() {
        List<Disk> result =
                dao.getAllAttachableDisksByPoolId(Guid.createGuidFromString("6d849ebf-755f-4552-ad09-9a090cda105d"),
                        null,
                        false);

        assertTrue(result.isEmpty());
    }

    /**
     * Asserts the result of {@link DiskImageDAO#getAllForVm(Guid)} contains the correct disks.
     * @param disks
     *            The result to check
     */
    private static void assertFullGetAllForVMResult(List<Disk> disks) {
        assertEquals("VM should have three disks", 3, disks.size());
    }

}

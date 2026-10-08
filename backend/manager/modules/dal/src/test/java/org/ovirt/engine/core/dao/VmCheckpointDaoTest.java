package org.ovirt.engine.core.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.compat.Guid;

public class VmCheckpointDaoTest extends BaseDaoTestCase<VmCheckpointDao> {

    @Test
    public void testGetAllForVmReturnsEmptyListForNonExistingVm() {
        var result = dao.getAllForVm(Guid.newGuid());
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testIsDiskIncludedInCheckpointReturnsFalseForNonExistingDisk() {
        var result = dao.isDiskIncludedInCheckpoint(Guid.newGuid());
        assertFalse(result);
    }

    @Test
    public void testIsDiskIncludedInCheckpointReturnsTrueForExistingDisk() {
        var result = dao.isDiskIncludedInCheckpoint(FixturesTool.DISK_ID_3);
        assertTrue(result);
    }

    @Test
    public void testGetAllDisksByCheckpointIdReturnsEmptyListForNonExistingCheckpoint() {
        var result = dao.getDisksByCheckpointId(Guid.newGuid());
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testGetAllDisksByCheckpointIdReturnsNonEmptyListForExistingCheckpoint() {
        var result = dao.getDisksByCheckpointId(FixturesTool.CHECKPOINT_ID_1);
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }

    @Test
    public void testRemoveDiskFromCheckpointClearsCheckpoint() {
        var before = dao.getAllForVm(FixturesTool.VM_VM2_SHARED_NONBOOTABLE_DISK);
        assertEquals(1, before.size());
        dao.removeAllCheckpointsByDiskId(FixturesTool.BOOTABLE_SHARED_DISK_ID);
        var result = dao.getAllForVm(FixturesTool.VM_VM2_SHARED_NONBOOTABLE_DISK);
        assertEquals(0, result.size());
    }

    @Test
    public void getAllForVmReturnsNonEmptyListForExistingVm() {
        var result = dao.getAllForVm(FixturesTool.VM_RHEL5_POOL_60);
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }
}

package org.ovirt.engine.core.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotStatus;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.RandomUtils;

/**
 * Unit tests to validate {@link BaseDiskDao}.
 */
public class SnapshotDaoTest extends BaseGenericDaoTestCase<Guid, Snapshot, SnapshotDao> {

    private static final Guid EXISTING_VM_ID = FixturesTool.VM_RHEL5_POOL_57;
    private static final Guid EXISTING_VM_ID2 = FixturesTool.VM_RHEL5_POOL_50;
    private static final Guid EXISTING_SNAPSHOT_ID = new Guid("a7bb24df-9fdf-4bd6-b7a9-f5ce52da0f89");
    private static final Guid EXISTING_SNAPSHOT_ID2 = new Guid("a7bb24df-9fdf-4bd6-b7a9-f5ce52da0f11");
    private static final int TOTAL_SNAPSHOTS = 2;
    private static final Guid EXISTING_MEMORY_DUMP_DISK_ID = new Guid("1b26a52b-b60f-44cb-9f46-3ef333b04a34");
    private static final Guid EXISTING_MEMORY_CONF_DISK_ID = new Guid("1b26a52b-b60f-44cb-9f46-3ef333b04a35");


    @Override
    protected Guid generateNonExistingId() {
        return Guid.newGuid();
    }

    @Override
    protected int getEntitiesTotalCount() {
        return TOTAL_SNAPSHOTS;
    }

    @Override
    protected Snapshot generateNewEntity() {
        return new Snapshot(Guid.newGuid(),
                RandomUtils.instance().nextEnum(SnapshotStatus.class),
                EXISTING_VM_ID,
                RandomUtils.instance().nextString(200000),
                RandomUtils.instance().nextEnum(SnapshotType.class),
                RandomUtils.instance().nextString(1000),
                new Date(),
                RandomUtils.instance().nextString(200000));
    }

    @Override
    protected void updateExistingEntity() {
        existingEntity.setDescription(RandomUtils.instance().nextString(1000));
    }

    @Override
    protected SnapshotDao prepareDao() {
        return dbFacade.getSnapshotDao();
    }

    @Override
    protected Guid getExistingEntityId() {
        return EXISTING_SNAPSHOT_ID;
    }

    @Test
    public void updateStatus() throws Exception {
        Snapshot snapshot = dao.get(getExistingEntityId());

        snapshot.setStatus(SnapshotStatus.LOCKED);
        dao.updateStatus(snapshot.getId(), snapshot.getStatus());

        assertEquals(snapshot, dao.get(snapshot.getId()));
    }

    @Test
    public void updateStatusForNonExistingSnapshot() throws Exception {
        Guid snapshotId = Guid.Empty;

        assertNull(dao.get(snapshotId));
        dao.updateStatus(snapshotId, SnapshotStatus.LOCKED);

        assertNull(dao.get(snapshotId));
    }

    @Test
    public void updateId() throws Exception {
        Snapshot snapshot = dao.get(getExistingEntityId());

        assertNotNull(snapshot);
        Guid oldId = snapshot.getId();
        snapshot.setId(Guid.Empty);

        dao.updateId(oldId, snapshot.getId());

        assertEquals(snapshot, dao.get(snapshot.getId()));
    }

    @Test
    public void updateIdForNonExistingSnapshot() throws Exception {
        Guid snapshotId = Guid.Empty;
        Guid newSnapshotId = Guid.Empty;

        assertNull(dao.get(snapshotId));
        dao.updateId(snapshotId, newSnapshotId);

        assertNull(dao.get(snapshotId));
        assertNull(dao.get(newSnapshotId));
    }

    @Test
    public void getZeroSnapshotsByMemory() {
        Snapshot snapshot = new Snapshot();
        snapshot.setMemoryDiskId(Guid.newGuid());
        snapshot.setMetadataDiskId(Guid.newGuid());
        assertEquals(0, dao.getNumOfSnapshotsByDisks(snapshot));
    }

    @Test
    public void getOneSnapshotsByMemoryDump() {
        Snapshot snapshot = new Snapshot();
        snapshot.setMemoryDiskId(EXISTING_MEMORY_DUMP_DISK_ID);
        snapshot.setMetadataDiskId(Guid.newGuid());
        assertEquals(1, dao.getNumOfSnapshotsByDisks(snapshot));
    }

    @Test
    public void getOneSnapshotsByMemoryConf() {
        Snapshot snapshot = new Snapshot();
        snapshot.setMemoryDiskId(Guid.newGuid());
        snapshot.setMetadataDiskId(EXISTING_MEMORY_CONF_DISK_ID);
        assertEquals(1, dao.getNumOfSnapshotsByDisks(snapshot));
    }


    @Test
    public void getSnaphsotByTypeReturnsIdForExistingByTypeAndStatus() throws Exception {
        assertNotNull(dao.get(EXISTING_VM_ID, SnapshotType.REGULAR));
    }

    @Test
    public void getSnaphsotByTypeReturnsIdForExistingByStatus() throws Exception {
        assertNotNull(dao.get(EXISTING_VM_ID, SnapshotStatus.OK));
    }

    @Test
    public void getSnaphsotByTypeReturnsIdForNotExistingByStatus() throws Exception {
        assertNull(dao.get(EXISTING_VM_ID, SnapshotStatus.IN_PREVIEW));
    }

    @Test
    public void getSnaphsotByTypeAndStatusForExistingEntity() throws Exception {
        assertEquals(existingEntity, dao.get(EXISTING_VM_ID, SnapshotType.REGULAR, SnapshotStatus.OK));
    }

    @Test
    public void getSnaphsotByTypeAndStatusForNonExistingEntity() throws Exception {
        assertNull(dao.get(EXISTING_VM_ID, SnapshotType.REGULAR, SnapshotStatus.LOCKED));
    }

    @Test
    public void getIdByTypeReturnsIdForExistingByTypeAndStatus() throws Exception {
        assertEquals(getExistingEntityId(), dao.getId(EXISTING_VM_ID, SnapshotType.REGULAR));
    }

    @Test
    public void getIdByTypeReturnsNullForNonExistingVm() throws Exception {
        assertNull(dao.getId(Guid.Empty, SnapshotType.REGULAR));
    }

    @Test
    public void getIdByTypeReturnsNullForNonExistingType() throws Exception {
        assertNull(dao.getId(EXISTING_VM_ID, SnapshotType.PREVIEW));
    }

    @Test
    public void getIdByTypeAndStatusReturnsIdForExistingByTypeAndStatus() throws Exception {
        assertEquals(getExistingEntityId(), dao.getId(EXISTING_VM_ID, SnapshotType.REGULAR, SnapshotStatus.OK));
    }

    @Test
    public void getIdByTypeAndStatusReturnsNullForNonExistingVm() throws Exception {
        assertNull(dao.getId(Guid.Empty, SnapshotType.REGULAR, SnapshotStatus.OK));
    }

    @Test
    public void getIdByTypeAndStatusReturnsNullForNonExistingType() throws Exception {
        assertNull(dao.getId(EXISTING_VM_ID, SnapshotType.PREVIEW, SnapshotStatus.OK));
    }

    @Test
    public void getIdByTypeAndStatusReturnsNullForNonExistingStatus() throws Exception {
        assertNull(dao.getId(EXISTING_VM_ID, SnapshotType.REGULAR, SnapshotStatus.IN_PREVIEW));
    }

    @Test
    public void getAllByVmWithConfiguration() {
        List<Snapshot> snapshots = dao.getAllWithConfiguration(FixturesTool.VM_RHEL5_POOL_50);
        assertEquals("VM should have a snapshot", 1, snapshots.size());
        for (Snapshot snapshot : snapshots) {
            assertEquals("Snapshot should have configuration", "test!", snapshot.getVmConfiguration());
            assertTrue("Snapshot should have configuration available", snapshot.isVmConfigurationAvailable());
        }
    }

    @Test
    public void getAllByVm() {
        List<Snapshot> snapshots = dao.getAll(FixturesTool.VM_RHEL5_POOL_57);
        assertFullGetAllByVmResult(snapshots);
    }

    @Test
    public void getAllByVmFilteredWithPermissions() {
        // test user 3 - has permissions
        List<Snapshot> snapshots = dao.getAll(FixturesTool.VM_RHEL5_POOL_57, PRIVILEGED_USER_ID, true);
        assertFullGetAllByVmResult(snapshots);
    }

    @Test
    public void getAllByVmFilteredWithPermissionsNoPermissions() {
        // test user 2 - hasn't got permissions
        List<Snapshot> snapshots = dao.getAll(FixturesTool.VM_RHEL5_POOL_57, UNPRIVILEGED_USER_ID, true);
        assertTrue("VM should have no snapshots viewable to the user", snapshots.isEmpty());
    }

    @Test
    public void getAllByVmFilteredWithPermissionsNoPermissionsAndNoFilter() {
        // test user 2 - hasn't got permissions, but no filtering was requested
        List<Snapshot> snapshots = dao.getAll(FixturesTool.VM_RHEL5_POOL_57, UNPRIVILEGED_USER_ID, false);
        assertFullGetAllByVmResult(snapshots);
    }

    @Test
    public void getAllByStorageDomain() {
        List<Snapshot> snapshots = dao.getAllByStorageDomain(FixturesTool.STORAGE_DOMAIN_SCALE_SD5);
        assertFalse("Snapshots list shouldn't be empty", snapshots.isEmpty());
    }

    @Test
    public void getFilteredWithPermissions() {
        Snapshot snapshot = dao.get(EXISTING_SNAPSHOT_ID, PRIVILEGED_USER_ID, true);
        assertNotNull(snapshot);
        assertEquals(existingEntity, snapshot);
    }

    @Test
    public void getFilteredWithPermissionsNoPermissions() {
        Snapshot snapshot = dao.get(EXISTING_SNAPSHOT_ID, UNPRIVILEGED_USER_ID, true);
        assertNull(snapshot);
    }

    @Test
    public void getFilteredWithPermissionsNoPermissionsAndNoFilter() {
        Snapshot snapshot = dao.get(EXISTING_SNAPSHOT_ID, UNPRIVILEGED_USER_ID, false);
        assertNotNull(snapshot);
        assertEquals(existingEntity, snapshot);
    }

    /**
     * Asserts the result of {@link SnapshotDao#getAll(Guid)} contains the correct snapshots.
     *
     * @param snapshots
     *            The result to check
     */
    private static void assertFullGetAllByVmResult(List<Snapshot> snapshots) {
        assertEquals("VM should have a snapshot", 1, snapshots.size());
        for (Snapshot snapshot : snapshots) {
            assertFalse("Snapshot shouldn't have configuration available", snapshot.isVmConfigurationAvailable());
            assertTrue("Snapshot should have no configuration", StringUtils.isEmpty(snapshot.getVmConfiguration()));
        }
    }

    @Test
    public void existsReturnsTrueForExistingByVmAndType() throws Exception {
        assertTrue(dao.exists(EXISTING_VM_ID, SnapshotType.REGULAR));
    }

    @Test
    public void existsWithTypeReturnsFalseForNonExistingVm() throws Exception {
        assertFalse(dao.exists(Guid.Empty, SnapshotType.REGULAR));
    }

    @Test
    public void existsWithTypeReturnsFalseForNonExistingStatus() throws Exception {
        assertFalse(dao.exists(EXISTING_VM_ID, SnapshotType.PREVIEW));
    }

    @Test
    public void existsReturnsTrueForExistingByVmAndStatus() throws Exception {
        assertTrue(dao.exists(EXISTING_VM_ID, SnapshotStatus.OK));
    }

    @Test
    public void existsWithStatusReturnsFalseForNonExistingVm() throws Exception {
        assertFalse(dao.exists(Guid.Empty, SnapshotStatus.OK));
    }

    @Test
    public void existsWithStatusReturnsFalseForNonExistingStatus() throws Exception {
        assertFalse(dao.exists(EXISTING_VM_ID, SnapshotStatus.LOCKED));
    }

    @Test
    public void existsReturnsTrueForExistingByVmAndSansphot() throws Exception {
        assertTrue(dao.exists(EXISTING_VM_ID, getExistingEntityId()));
    }

    @Test
    public void existsWithSnapshotReturnsFalseForNonExistingVm() throws Exception {
        assertFalse(dao.exists(Guid.Empty, getExistingEntityId()));
    }

    @Test
    public void existsWithSnapshotReturnsFalseForNonExistingSnapshot() throws Exception {
        assertFalse(dao.exists(EXISTING_VM_ID, Guid.Empty));
    }

    @Test
    public void removeMemoryFromActiveSnapshot() throws Exception {
        Snapshot snapshot = dao.get(EXISTING_SNAPSHOT_ID2);
        assertEquals(EXISTING_MEMORY_DUMP_DISK_ID, snapshot.getMemoryDiskId());
        assertEquals(EXISTING_MEMORY_CONF_DISK_ID, snapshot.getMetadataDiskId());

        dao.removeMemoryFromActiveSnapshot(EXISTING_VM_ID2);

        snapshot = dao.get(EXISTING_SNAPSHOT_ID2);
        assertNull(snapshot.getMemoryDiskId());
        assertNull(snapshot.getMetadataDiskId());
    }
}

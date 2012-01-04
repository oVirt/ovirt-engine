package org.ovirt.engine.core.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotStatus;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.RandomUtils;

/**
 * Unit tests to validate {@link DiskDao}.
 */
public class SnapshotDaoTest extends BaseGenericDaoTestCase<Guid, Snapshot, SnapshotDao> {

    private static final Guid EXISTING_VM_ID = new Guid("77296e00-0cad-4e5a-9299-008a7b6f4355");
    private static final Guid EXISTING_SNAPSHOT_ID = new Guid("a7bb24df-9fdf-4bd6-b7a9-f5ce52da0f89");
    private static final int TOTAL_SNAPSHOTS = 1;

    @Override
    protected Guid generateNonExistingId() {
        return Guid.NewGuid();
    }

    @Override
    protected int getEneitiesTotalCount() {
        return TOTAL_SNAPSHOTS;
    }

    @Override
    protected Snapshot generateNewEntity() {
        return new Snapshot(Guid.NewGuid(),
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
        return prepareDAO(dbFacade.getSnapshotDao());
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
        Guid snapshotId = new Guid();

        assertNull(dao.get(snapshotId));
        dao.updateStatus(snapshotId, SnapshotStatus.LOCKED);

        assertNull(dao.get(snapshotId));
    }

    @Test
    public void updateId() throws Exception {
        Snapshot snapshot = dao.get(getExistingEntityId());

        assertNotNull(snapshot);
        Guid oldId = snapshot.getId();
        snapshot.setId(new Guid());

        dao.updateId(oldId, snapshot.getId());

        assertEquals(snapshot, dao.get(snapshot.getId()));
    }

    @Test
    public void updateIdForNonExistingSnapshot() throws Exception {
        Guid snapshotId = new Guid();
        Guid newSnapshotId = new Guid();

        assertNull(dao.get(snapshotId));
        dao.updateId(snapshotId, newSnapshotId);

        assertNull(dao.get(snapshotId));
        assertNull(dao.get(newSnapshotId));
    }

    @Test
    public void getIdByTypeReturnsIdForExistingByTypeAndStatus() throws Exception {
        assertEquals(getExistingEntityId(), dao.getId(EXISTING_VM_ID, SnapshotType.REGULAR));
    }

    @Test
    public void getIdByTypeReturnsNullForNonExistingVm() throws Exception {
        assertEquals(null, dao.getId(new Guid(), SnapshotType.REGULAR));
    }

    @Test
    public void getIdByTypeReturnsNullForNonExistingType() throws Exception {
        assertEquals(null, dao.getId(EXISTING_VM_ID, SnapshotType.PREVIEW));
    }

    @Test
    public void getIdByTypeAndStatusReturnsIdForExistingByTypeAndStatus() throws Exception {
        assertEquals(getExistingEntityId(), dao.getId(EXISTING_VM_ID, SnapshotType.REGULAR, SnapshotStatus.OK));
    }

    @Test
    public void getIdByTypeAndStatusReturnsNullForNonExistingVm() throws Exception {
        assertEquals(null, dao.getId(new Guid(), SnapshotType.REGULAR, SnapshotStatus.OK));
    }

    @Test
    public void getIdByTypeAndStatusReturnsNullForNonExistingType() throws Exception {
        assertEquals(null, dao.getId(EXISTING_VM_ID, SnapshotType.PREVIEW, SnapshotStatus.OK));
    }

    @Test
    public void getIdByTypeAndStatusReturnsNullForNonExistingStatus() throws Exception {
        assertEquals(null, dao.getId(EXISTING_VM_ID, SnapshotType.REGULAR, SnapshotStatus.IN_PREVIEW));
    }

    @Test
    public void existsReturnsTrueForExistingByVmAndType() throws Exception {
        assertTrue(dao.exists(EXISTING_VM_ID, SnapshotType.REGULAR));
    }

    @Test
    public void existsWithTypeReturnsFalseForNonExistingVm() throws Exception {
        assertFalse(dao.exists(new Guid(), SnapshotType.REGULAR));
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
        assertFalse(dao.exists(new Guid(), SnapshotStatus.OK));
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
        assertFalse(dao.exists(new Guid(), getExistingEntityId()));
    }

    @Test
    public void existsWithSnapshotReturnsFalseForNonExistingSnapshot() throws Exception {
        assertFalse(dao.exists(EXISTING_VM_ID, new Guid()));
    }
}

package org.ovirt.engine.core.dao.gluster;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterSnapshotStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeSnapshotEntity;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.BaseDaoTestCase;

public class GlusterVolumeSnapshotDaoTest extends BaseDaoTestCase {
    private static final Guid VOLUME_ID = new Guid("0c3f45f6-3fe9-4b35-a30c-be0d1a835ea8");
    private static final Guid CLUSTER_ID = new Guid("ae956031-6be2-43d6-bb8f-5191c9253314");
    private static final Guid EXISTING_SNAPSHOT_ID = new Guid("0c3f45f6-3fe9-4b35-a30c-be0d1a835ea6");
    private static final Guid EXISTING_SNAPSHOT_ID_1 = new Guid("0c3f45f6-3fe9-4b35-a30c-be0d1a835ea7");
    private static final String EXISTING_SNAPSHOT_NAME_1 = "test-vol-distribute-1-snap2";
    private static final String NEW_SNAPSHOT_NAME = "test-vol-distribute-1-snap3";
    private GlusterVolumeSnapshotDao dao;
    private GlusterVolumeDao volumeDao;
    private GlusterVolumeSnapshotEntity existingSnapshot;
    private GlusterVolumeSnapshotEntity existingSnapshot1;
    private GlusterVolumeSnapshotEntity newSnapshot;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        dao = dbFacade.getGlusterVolumeSnapshotDao();
        volumeDao = dbFacade.getGlusterVolumeDao();
        existingSnapshot = dao.getById(EXISTING_SNAPSHOT_ID);
        existingSnapshot1 = dao.getById(EXISTING_SNAPSHOT_ID_1);
    }

    @Test
    public void testSaveAndGetById() {
        GlusterVolumeSnapshotEntity snapshot = dao.getByName(VOLUME_ID, NEW_SNAPSHOT_NAME);
        assertNull(snapshot);

        newSnapshot = insertTestSnapshot();
        snapshot = dao.getById(newSnapshot.getId());

        assertNotNull(snapshot);
        assertEquals(newSnapshot, snapshot);
    }

    @Test
    public void testGetByName() {
        newSnapshot = insertTestSnapshot();
        GlusterVolumeSnapshotEntity snapshot = dao.getByName(VOLUME_ID, NEW_SNAPSHOT_NAME);

        assertNotNull(snapshot);
        assertEquals(newSnapshot, snapshot);
    }

    @Test
    public void testGetByVolumeId() {
        List<GlusterVolumeSnapshotEntity> snapshots = dao.getAllByVolumeId(VOLUME_ID);

        assertTrue(snapshots != null);
        assertTrue(snapshots.size() == 2);
        assertTrue(snapshots.contains(existingSnapshot));
    }

    @Test
    public void testGetByClusterId() {
        List<GlusterVolumeSnapshotEntity> snapshots = dao.getAllByClusterId(CLUSTER_ID);

        assertNotNull(snapshots);
        assertTrue(snapshots.size() == 2);
        assertTrue(snapshots.contains(existingSnapshot));
    }

    @Test
    public void testGetAllWithQuery() {
        List<GlusterVolumeSnapshotEntity> snapshots =
                dao.getAllWithQuery("select * from gluster_volume_snapshots_view");

        assertTrue(snapshots != null);
        assertTrue(snapshots.size() == 2);
    }

    @Test
    public void testRemove() {
        GlusterVolumeEntity volume = volumeDao.getById(VOLUME_ID);
        assertEquals(volume.getSnapshotsCount().intValue(), 2);

        dao.remove(EXISTING_SNAPSHOT_ID);
        List<GlusterVolumeSnapshotEntity> snapshots = dao.getAllByVolumeId(VOLUME_ID);

        assertTrue(snapshots.size() == 1);
        assertFalse(snapshots.contains(existingSnapshot));

        GlusterVolumeEntity volume1 = volumeDao.getById(VOLUME_ID);
        assertEquals(volume1.getSnapshotsCount().intValue(), 1);
    }

    @Test
    public void testRemoveMultiple() {
        List<Guid> idsToRemove = new ArrayList<>();
        idsToRemove.add(EXISTING_SNAPSHOT_ID);
        idsToRemove.add(EXISTING_SNAPSHOT_ID_1);

        GlusterVolumeEntity volume = volumeDao.getById(VOLUME_ID);
        assertEquals(volume.getSnapshotsCount().intValue(), 2);

        dao.removeAll(idsToRemove);
        List<GlusterVolumeSnapshotEntity> snapshots = dao.getAllByVolumeId(VOLUME_ID);
        assertTrue(snapshots.isEmpty());

        GlusterVolumeEntity volume1 = volumeDao.getById(VOLUME_ID);
        assertEquals(volume1.getSnapshotsCount().intValue(), 0);
    }

    @Test
    public void testRemoveByName() {
        GlusterVolumeEntity volume = volumeDao.getById(VOLUME_ID);
        assertEquals(volume.getSnapshotsCount().intValue(), 2);

        dao.removeByName(VOLUME_ID, EXISTING_SNAPSHOT_NAME_1);
        List<GlusterVolumeSnapshotEntity> snapshots = dao.getAllByVolumeId(VOLUME_ID);

        assertTrue(snapshots.size() == 1);
        assertTrue(snapshots.contains(existingSnapshot));
        assertFalse(snapshots.contains(existingSnapshot1));

        GlusterVolumeEntity volume1 = volumeDao.getById(VOLUME_ID);
        assertEquals(volume1.getSnapshotsCount().intValue(), 1);
    }

    @Test
    public void testRemoveAllByVolumeId() {
        GlusterVolumeEntity volume = volumeDao.getById(VOLUME_ID);
        assertEquals(volume.getSnapshotsCount().intValue(), 2);

        dao.removeAllByVolumeId(VOLUME_ID);
        List<GlusterVolumeSnapshotEntity> snapshots = dao.getAllByVolumeId(VOLUME_ID);
        assertTrue(snapshots.isEmpty());

        GlusterVolumeEntity volume1 = volumeDao.getById(VOLUME_ID);
        assertEquals(volume1.getSnapshotsCount().intValue(), 0);
    }

    @Test
    public void testUpdateSnapshotStatus() {
        dao.updateSnapshotStatus(existingSnapshot.getSnapshotId(), GlusterSnapshotStatus.DEACTIVATED);
        GlusterVolumeSnapshotEntity snapshot = dao.getById(existingSnapshot.getSnapshotId());

        assertNotNull(snapshot);

        assertFalse(snapshot.equals(existingSnapshot));
        existingSnapshot.setStatus(GlusterSnapshotStatus.DEACTIVATED);
        assertEquals(existingSnapshot, snapshot);
    }

    @Test
    public void testUpdateSnapshotStatusByName() {
        dao.updateSnapshotStatusByName(existingSnapshot.getVolumeId(),
                existingSnapshot.getSnapshotName(),
                GlusterSnapshotStatus.DEACTIVATED);
        GlusterVolumeSnapshotEntity snapshot = dao.getById(existingSnapshot.getSnapshotId());

        assertNotNull(snapshot);

        assertFalse(snapshot.equals(existingSnapshot));
        existingSnapshot.setStatus(GlusterSnapshotStatus.DEACTIVATED);
        assertEquals(existingSnapshot, snapshot);
    }

    @Test
    public void testUpdateAllInBatch() {
        existingSnapshot = dao.getById(EXISTING_SNAPSHOT_ID);
        existingSnapshot1 = dao.getById(EXISTING_SNAPSHOT_ID_1);

        existingSnapshot.setStatus(GlusterSnapshotStatus.DEACTIVATED);
        existingSnapshot1.setStatus(GlusterSnapshotStatus.DEACTIVATED);

        List<GlusterVolumeSnapshotEntity> snapshots = new ArrayList<>();
        snapshots.add(existingSnapshot);
        snapshots.add(existingSnapshot1);

        dao.updateAllInBatch(snapshots);

        GlusterVolumeSnapshotEntity tmpSnapshot = dao.getById(EXISTING_SNAPSHOT_ID);
        GlusterVolumeSnapshotEntity tmpSnapshot1 = dao.getById(EXISTING_SNAPSHOT_ID_1);

        assertEquals(tmpSnapshot.getStatus(), GlusterSnapshotStatus.DEACTIVATED);
        assertEquals(tmpSnapshot1.getStatus(), GlusterSnapshotStatus.DEACTIVATED);
    }

    private GlusterVolumeSnapshotEntity insertTestSnapshot() {
        Guid snapshotId = Guid.newGuid();

        GlusterVolumeSnapshotEntity snapshot = new GlusterVolumeSnapshotEntity();
        snapshot.setSnapshotId(snapshotId);
        snapshot.setClusterId(CLUSTER_ID);
        snapshot.setSnapshotName(NEW_SNAPSHOT_NAME);
        snapshot.setVolumeId(VOLUME_ID);
        snapshot.setDescription("test-description");
        snapshot.setStatus(GlusterSnapshotStatus.ACTIVATED);
        snapshot.setCreatedAt(new Date());

        dao.save(snapshot);
        return snapshot;
    }
}

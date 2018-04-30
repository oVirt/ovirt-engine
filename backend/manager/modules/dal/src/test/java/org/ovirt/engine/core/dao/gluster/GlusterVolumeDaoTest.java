package org.ovirt.engine.core.dao.gluster;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.common.asynctasks.gluster.GlusterTaskType;
import org.ovirt.engine.core.common.businessentities.gluster.AccessProtocol;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeSizeInfo;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeType;
import org.ovirt.engine.core.common.businessentities.gluster.TransportType;
import org.ovirt.engine.core.common.job.JobExecutionStatus;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.BaseDaoTestCase;
import org.ovirt.engine.core.dao.FixturesTool;

/**
 * Tests for Gluster Volume Dao
 */
public class GlusterVolumeDaoTest extends BaseDaoTestCase<GlusterVolumeDao> {
    private static final Guid CLUSTER_ID = new Guid("ae956031-6be2-43d6-bb8f-5191c9253314");
    private static final Guid EXISTING_VOL_DIST_ID = new Guid("0c3f45f6-3fe9-4b35-a30c-be0d1a835ea8");
    private static final Guid EXISTING_VOL_REPL_ID = new Guid("b2cb2f73-fab3-4a42-93f0-d5e4c069a43e");
    private static final Guid REBALANCING_VOLUME_TASKID = new Guid("44f714ed-2818-4350-b94a-8c3927e53f7c");
    private final SimpleDateFormat EXPECTED_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final String EXISTING_VOL_REPL_NAME = "test-vol-replicate-1";
    private static final String NEW_VOL_NAME = "test-new-vol-1";
    private static final String OPTION_KEY_NFS_DISABLE = "nfs.disable";
    private static final String OPTION_VALUE_OFF = "off";
    private GlusterVolumeEntity existingDistVol;
    private GlusterVolumeEntity existingReplVol;
    private GlusterVolumeEntity newVolume;

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setUp();
        existingDistVol = dao.getById(EXISTING_VOL_DIST_ID);
        existingReplVol = dao.getById(EXISTING_VOL_REPL_ID);
    }

    @Test
    public void testSaveAndGetById() {
        GlusterVolumeEntity volume = dao.getByName(CLUSTER_ID, NEW_VOL_NAME);
        assertNull(volume);

        newVolume = insertTestVolume();
        volume = dao.getById(newVolume.getId());

        assertNotNull(volume);
        assertEquals(newVolume, volume);
    }

    @Test
    public void testGetByName() {
        newVolume = insertTestVolume();
        GlusterVolumeEntity volume = dao.getByName(CLUSTER_ID, NEW_VOL_NAME);

        assertNotNull(volume);
        assertEquals(newVolume, volume);
    }

    @Test
    public void testGetByClusterId() {
        List<GlusterVolumeEntity> volumes = dao.getByClusterId(CLUSTER_ID);

        assertNotNull(volumes);
        assertEquals(2, volumes.size());
        assertTrue(volumes.contains(existingDistVol));
        assertTrue(volumes.contains(existingReplVol));
    }

    @Test
    public void testGetCapacityInfo() throws ParseException {
        GlusterVolumeEntity volume = dao.getById(EXISTING_VOL_DIST_ID);
        assertNotNull(volume.getAdvancedDetails(), "volume capacity info is not available");
        assertEquals(100000L, volume.getAdvancedDetails().getCapacityInfo().getTotalSize().longValue());
        assertEquals(60000L, volume.getAdvancedDetails().getCapacityInfo().getUsedSize().longValue());
        assertEquals(40000L, volume.getAdvancedDetails().getCapacityInfo().getFreeSize().longValue());
        assertEquals(EXPECTED_DATE_FORMAT.parse(volume.getAdvancedDetails()
                .getUpdatedAt().toString()), EXPECTED_DATE_FORMAT.parse("2014-01-21 18:12:33"));
    }

    @Test
    public void testGetAllWithQuery() {
        List<GlusterVolumeEntity> volumes =
                dao.getAllWithQuery("select * from gluster_volumes_view where vol_type = '"
                        + GlusterVolumeType.DISTRIBUTED_REPLICATE.name() + "'");

        assertNotNull(volumes);
        assertEquals(1, volumes.size());
        assertEquals(existingReplVol, volumes.get(0));
    }

    @Test
    public void testRemove() {
        dao.remove(EXISTING_VOL_DIST_ID);
        List<GlusterVolumeEntity> volumes = dao.getByClusterId(CLUSTER_ID);

        assertEquals(1, volumes.size());
        assertFalse(volumes.contains(existingDistVol));
        assertTrue(volumes.contains(existingReplVol));
    }

    @Test
    public void testRemoveMultiple() {
        List<Guid> idsToRemove = new ArrayList<>();
        idsToRemove.add(EXISTING_VOL_DIST_ID);
        idsToRemove.add(EXISTING_VOL_REPL_ID);

        dao.removeAll(idsToRemove);
        List<GlusterVolumeEntity> volumes = dao.getByClusterId(CLUSTER_ID);

        assertTrue(volumes.isEmpty());
    }

    @Test
    public void testRemoveByName() {
        dao.removeByName(CLUSTER_ID, EXISTING_VOL_REPL_NAME);
        List<GlusterVolumeEntity> volumes = dao.getByClusterId(CLUSTER_ID);

        assertEquals(1, volumes.size());
        assertTrue(volumes.contains(existingDistVol));
        assertFalse(volumes.contains(existingReplVol));
    }

    @Test
    public void testRemoveByClusterId() {
        dao.removeByClusterId(CLUSTER_ID);
        List<GlusterVolumeEntity> volumes = dao.getByClusterId(CLUSTER_ID);
        assertTrue(volumes.isEmpty());
    }

    @Test
    public void testUpdateVolumeStatus() {
        assertTrue(existingDistVol.isOnline());

        dao.updateVolumeStatus(existingDistVol.getId(), GlusterStatus.DOWN);
        GlusterVolumeEntity volume = dao.getById(existingDistVol.getId());

        assertNotNull(volume);
        assertFalse(volume.isOnline());

        assertNotEquals(volume, existingDistVol);
        existingDistVol.setStatus(GlusterStatus.DOWN);
        assertEquals(existingDistVol, volume);
    }

    @Test
    public void testUpdateVolumeCapacityInfo() {
        GlusterVolumeSizeInfo capacityInfo = new GlusterVolumeSizeInfo();
        capacityInfo.setVolumeId(existingDistVol.getId());
        capacityInfo.setTotalSize(Long.valueOf("500000"));
        capacityInfo.setFreeSize(Long.valueOf("300000"));
        capacityInfo.setUsedSize(Long.valueOf("200000"));

        dao.updateVolumeCapacityInfo(capacityInfo);

        GlusterVolumeEntity volume = dao.getById(existingDistVol.getId());

        assertNotNull(volume);
        assertNotEquals(volume, existingDistVol);
        assertNotNull(volume.getAdvancedDetails().getCapacityInfo(), "volume capacity info is not available");
        assertEquals(500000, (long) volume.getAdvancedDetails().getCapacityInfo().getTotalSize());
        assertEquals(200000, (long) volume.getAdvancedDetails().getCapacityInfo().getUsedSize());
        assertEquals(300000, (long) volume.getAdvancedDetails().getCapacityInfo().getFreeSize());
        assertNotNull(volume.getAdvancedDetails().getUpdatedAt());

    }

    @Test
    public void testUpdateVolumeStatusByName() {
        assertTrue(existingDistVol.isOnline());

        dao.updateVolumeStatusByName(existingDistVol.getClusterId(),
                existingDistVol.getName(),
                GlusterStatus.DOWN);
        GlusterVolumeEntity volume = dao.getById(existingDistVol.getId());

        assertNotNull(volume);
        assertFalse(volume.isOnline());

        assertNotEquals(volume, existingDistVol);
        existingDistVol.setStatus(GlusterStatus.DOWN);
        assertEquals(existingDistVol, volume);
    }

    @Test
    public void testUpdateAsyncTaskId() {
        assertNotNull(existingDistVol.getAsyncTask());
        assertNull(existingDistVol.getAsyncTask().getTaskId());
        dao.updateVolumeTask(existingDistVol.getId(), REBALANCING_VOLUME_TASKID);
        GlusterVolumeEntity volume = dao.getAllWithQuery("select * from gluster_volumes_view where id = '"
                        + existingDistVol.getId() + "'").get(0);

        assertNotNull(volume, "Volume : "+ existingDistVol.getId() +" doesn't exists");
        assertEquals(REBALANCING_VOLUME_TASKID, volume.getAsyncTask().getTaskId(), "Task ID is not getting updated");
        assertEquals(JobExecutionStatus.STARTED, volume.getAsyncTask().getStatus(), "Invalid Task status");
        assertEquals(GlusterTaskType.REBALANCE, volume.getAsyncTask().getType(), "Invalid Task type");
    }

    @Test
    public void testReplicateCount() {
        GlusterVolumeEntity volume = dao.getById(EXISTING_VOL_REPL_ID);
        int replicaCount = volume.getReplicaCount();

        assertTrue(replicaCount != 0);
        assertEquals(2, replicaCount);

        dao.updateReplicaCount(EXISTING_VOL_REPL_ID, 3);

        GlusterVolumeEntity volumeAfter = dao.getById(EXISTING_VOL_REPL_ID);
        assertNotNull(volumeAfter);

        replicaCount = volumeAfter.getReplicaCount();
        assertTrue(replicaCount != 0);
        assertEquals(3, replicaCount);
    }

    @Test
    public void testStripeCount() {
        GlusterVolumeEntity volume = dao.getById(EXISTING_VOL_REPL_ID);
        int replicaCount = volume.getReplicaCount();

        assertTrue(replicaCount != 0);
        assertEquals(2, replicaCount);

        dao.updateReplicaCount(EXISTING_VOL_REPL_ID, 4);

        GlusterVolumeEntity volumeAfter = dao.getById(EXISTING_VOL_REPL_ID);
        assertNotNull(volumeAfter);

        replicaCount = volumeAfter.getReplicaCount();
        assertTrue(replicaCount != 0);
        assertEquals(4, replicaCount);
    }

    @Test
    public void testAddAccessProtocol() {
        Set<AccessProtocol> protocols = existingDistVol.getAccessProtocols();
        assertEquals(1, protocols.size());
        assertFalse(protocols.contains(AccessProtocol.NFS));

        dao.addAccessProtocol(EXISTING_VOL_DIST_ID, AccessProtocol.NFS);

        GlusterVolumeEntity volumeAfter = dao.getById(EXISTING_VOL_DIST_ID);
        assertNotNull(volumeAfter);

        protocols = volumeAfter.getAccessProtocols();
        assertEquals(2, protocols.size());
        assertTrue(protocols.contains(AccessProtocol.NFS));

        assertNotEquals(volumeAfter, existingDistVol);
        existingDistVol.addAccessProtocol(AccessProtocol.NFS);
        assertEquals(volumeAfter, existingDistVol);
    }

    @Test
    public void testRemoveAccessProtocol() {
        Set<AccessProtocol> protocols = existingReplVol.getAccessProtocols();
        assertEquals(2, protocols.size());
        assertTrue(protocols.contains(AccessProtocol.NFS));

        dao.removeAccessProtocol(EXISTING_VOL_REPL_ID, AccessProtocol.NFS);

        GlusterVolumeEntity volumeAfter = dao.getById(EXISTING_VOL_REPL_ID);
        assertNotNull(volumeAfter);

        protocols = volumeAfter.getAccessProtocols();
        assertEquals(1, protocols.size());
        assertFalse(protocols.contains(AccessProtocol.NFS));

        assertNotEquals(volumeAfter, existingReplVol);
        existingReplVol.removeAccessProtocol(AccessProtocol.NFS);
        assertEquals(volumeAfter, existingReplVol);
    }

    @Test
    public void testAddTransportType() {
        Set<TransportType> transportTypes = existingDistVol.getTransportTypes();
        assertEquals(1, transportTypes.size());
        assertFalse(transportTypes.contains(TransportType.RDMA));

        dao.addTransportType(EXISTING_VOL_DIST_ID, TransportType.RDMA);

        GlusterVolumeEntity volumeAfter = dao.getById(EXISTING_VOL_DIST_ID);
        assertNotNull(volumeAfter);

        transportTypes = volumeAfter.getTransportTypes();
        assertEquals(2, transportTypes.size());
        assertTrue(transportTypes.contains(TransportType.RDMA));

        assertNotEquals(volumeAfter, existingDistVol);
        existingDistVol.addTransportType(TransportType.RDMA);
        assertEquals(volumeAfter, existingDistVol);
    }

    @Test
    public void testAddVolumeCapacityInfo() {
        GlusterVolumeEntity volumeBefore = dao.getById(EXISTING_VOL_REPL_ID);
        assertNotNull(volumeBefore);
        assertNull(volumeBefore.getAdvancedDetails().getCapacityInfo());

        GlusterVolumeSizeInfo capacityInfo = new GlusterVolumeSizeInfo();
        capacityInfo.setVolumeId(EXISTING_VOL_REPL_ID);
        capacityInfo.setTotalSize(Long.valueOf("250000"));
        capacityInfo.setUsedSize(Long.valueOf("175000"));
        capacityInfo.setFreeSize(Long.valueOf("75000"));

        dao.addVolumeCapacityInfo(capacityInfo);
        GlusterVolumeEntity volumeAfter = dao.getById(EXISTING_VOL_REPL_ID);
        assertNotNull(volumeAfter);
        assertNotNull(volumeAfter.getAdvancedDetails().getCapacityInfo());
        assertEquals(250000, (long) volumeAfter.getAdvancedDetails().getCapacityInfo().getTotalSize());
        assertEquals(175000, (long) volumeAfter.getAdvancedDetails().getCapacityInfo().getUsedSize());
        assertEquals(75000, (long) volumeAfter.getAdvancedDetails().getCapacityInfo().getFreeSize());
        assertNotNull(volumeAfter.getAdvancedDetails().getUpdatedAt());
    }

    @Test
    public void testAddTransportTypes() {
        Set<TransportType> transportTypes = existingDistVol.getTransportTypes();
        assertEquals(1, transportTypes.size());
        dao.removeTransportType(EXISTING_VOL_DIST_ID, TransportType.TCP);
        transportTypes = dao.getById(EXISTING_VOL_DIST_ID).getTransportTypes();
        assertEquals(0, transportTypes.size());

        List<TransportType> types = new ArrayList<>();
        types.add(TransportType.TCP);
        types.add(TransportType.RDMA);
        dao.addTransportTypes(EXISTING_VOL_DIST_ID, types);

        GlusterVolumeEntity volumeAfter = dao.getById(EXISTING_VOL_DIST_ID);
        assertNotNull(volumeAfter);

        transportTypes = volumeAfter.getTransportTypes();
        assertEquals(2, transportTypes.size());
        assertTrue(transportTypes.contains(TransportType.TCP));
        assertTrue(transportTypes.contains(TransportType.RDMA));

        assertNotEquals(volumeAfter, existingDistVol);
        existingDistVol.addTransportType(TransportType.TCP);
        existingDistVol.addTransportType(TransportType.RDMA);
        assertEquals(volumeAfter, existingDistVol);
    }

    @Test
    public void testRemoveTransportType() {
        Set<TransportType> transportTypes = existingReplVol.getTransportTypes();
        assertEquals(2, transportTypes.size());
        assertTrue(transportTypes.contains(TransportType.RDMA));

        dao.removeTransportType(EXISTING_VOL_REPL_ID, TransportType.RDMA);

        GlusterVolumeEntity volumeAfter = dao.getById(EXISTING_VOL_REPL_ID);
        assertNotNull(volumeAfter);

        transportTypes = volumeAfter.getTransportTypes();
        assertEquals(1, transportTypes.size());
        assertFalse(transportTypes.contains(TransportType.RDMA));

        assertNotEquals(volumeAfter, existingReplVol);
        existingReplVol.removeTransportType(TransportType.RDMA);
        assertEquals(volumeAfter, existingReplVol);
    }

    @Test
    public void testGetVolumesByOption() {
        List<GlusterVolumeEntity> volumes = dao.getVolumesByOption(CLUSTER_ID, GlusterStatus.UP, OPTION_KEY_NFS_DISABLE, OPTION_VALUE_OFF);

        assertNotNull(volumes);
        assertTrue(volumes.contains(existingReplVol));
        assertTrue(volumes.get(0).isNfsEnabled());
    }

    @Test
    public void testGetVolumesByStatusTypesAndOption() {
        List<GlusterVolumeEntity> volumes =
                dao.getVolumesByStatusTypesAndOption(CLUSTER_ID,
                        GlusterStatus.UP,
                        Collections.singletonList(GlusterVolumeType.DISTRIBUTED_REPLICATE),
                        OPTION_KEY_NFS_DISABLE,
                        OPTION_VALUE_OFF);

        assertNotNull(volumes);
        assertTrue(volumes.contains(existingReplVol));
        for (GlusterVolumeEntity volume : volumes) {
            assertTrue(volume.isNfsEnabled() && volume.getVolumeType() == GlusterVolumeType.DISTRIBUTED_REPLICATE);
        }
    }

    @Test
    public void testGetVolumesByStatusAndTypes() {
        List<GlusterVolumeEntity> volumes =
                dao.getVolumesByStatusAndTypes(CLUSTER_ID,
                        GlusterStatus.UP,
                        Collections.singletonList(GlusterVolumeType.DISTRIBUTE));

        assertNotNull(volumes);
        assertTrue(volumes.contains(existingDistVol));
        for (GlusterVolumeEntity volume : volumes) {
            assertEquals(GlusterVolumeType.DISTRIBUTE, volume.getVolumeType());
        }
    }

    @Test
    public void testRemoveTransportTypes() {
        Set<TransportType> transportTypes = existingReplVol.getTransportTypes();
        assertEquals(2, transportTypes.size());
        assertTrue(transportTypes.contains(TransportType.TCP));
        assertTrue(transportTypes.contains(TransportType.RDMA));

        List<TransportType> types = new ArrayList<>();
        types.add(TransportType.TCP);
        types.add(TransportType.RDMA);

        dao.removeTransportTypes(EXISTING_VOL_REPL_ID, types);

        GlusterVolumeEntity volumeAfter = dao.getById(EXISTING_VOL_REPL_ID);
        assertNotNull(volumeAfter);

        transportTypes = volumeAfter.getTransportTypes();
        assertEquals(0, transportTypes.size());
        assertFalse(transportTypes.contains(TransportType.TCP));
        assertFalse(transportTypes.contains(TransportType.RDMA));

        assertNotEquals(volumeAfter, existingReplVol);
        existingReplVol.removeTransportType(TransportType.TCP);
        existingReplVol.removeTransportType(TransportType.RDMA);
        assertEquals(volumeAfter, existingReplVol);
    }

    @Test
    public void testGetVolumesSupportedAsStorageDomain() {
        List<GlusterVolumeEntity> vols = dao.getVolumesSupportedAsStorageDomain();
        assertEquals(2, vols.size());
    }

    private GlusterVolumeEntity insertTestVolume() {
        Guid volumeId = Guid.newGuid();

        GlusterVolumeEntity volume = new GlusterVolumeEntity();
        volume.setName(NEW_VOL_NAME);
        volume.setClusterId(CLUSTER_ID);
        volume.setId(volumeId);
        volume.setVolumeType(GlusterVolumeType.DISTRIBUTE);
        volume.addTransportType(TransportType.TCP);
        volume.setReplicaCount(0);
        volume.setStripeCount(0);
        volume.setStatus(GlusterStatus.UP);
        volume.setOption("auth.allow", "*");
        volume.addAccessProtocol(AccessProtocol.GLUSTER);
        volume.addAccessProtocol(AccessProtocol.NFS);

        GlusterBrickEntity brick = new GlusterBrickEntity();
        brick.setVolumeId(volumeId);
        brick.setServerId(FixturesTool.VDS_RHEL6_NFS_SPM);
        brick.setServerName("some host name");
        brick.setBrickDirectory("/export/testVol1");
        brick.setStatus(GlusterStatus.UP);
        brick.setBrickOrder(0);
        volume.addBrick(brick);
        volume.setSnapshotsCount(0);
        volume.setSnapMaxLimit(0);

        dao.save(volume);
        return volume;
    }
}

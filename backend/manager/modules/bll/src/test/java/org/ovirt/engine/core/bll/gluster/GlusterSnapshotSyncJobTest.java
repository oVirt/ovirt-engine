package org.ovirt.engine.core.bll.gluster;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.bll.utils.GlusterAuditLogUtil;
import org.ovirt.engine.core.bll.utils.GlusterUtil;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterSnapshotConfigInfo;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterSnapshotStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeSnapshotConfig;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeSnapshotEntity;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.gluster.GlusterVolumeSnapshotVDSParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dao.ClusterDao;
import org.ovirt.engine.core.dao.gluster.GlusterVolumeDao;
import org.ovirt.engine.core.dao.gluster.GlusterVolumeSnapshotConfigDao;
import org.ovirt.engine.core.dao.gluster.GlusterVolumeSnapshotDao;
import org.ovirt.engine.core.utils.lock.EngineLock;

@MockitoSettings(strictness = Strictness.LENIENT)
public class GlusterSnapshotSyncJobTest {
    private static final Guid CLUSTER_ID_1 = Guid.newGuid();
    private static final Guid VOLUME_ID_1 = Guid.newGuid();
    private static final String VOLUME_NAME_1 = "VOL1";
    private static final Guid VOLUME_ID_2 = Guid.newGuid();
    private static final String VOLUME_NAME_2 = "VOL2";
    private static final Guid[] existingSnapshotIds = { Guid.newGuid(), Guid.newGuid() };
    private static final String[] existingSnapshotNames = { "snap-1", "snap-2" };
    private static final Date existingSnapsCreateDate = new Date();
    private static final Guid newSnapshotId = Guid.newGuid();
    private static final String newSnapshotName = "new-snap";
    private static final String PARAM_SNAP_MAX_LIMIT = "snap-max-hard-limit";

    @Mock
    private GlusterVolumeDao volumeDao;

    @Mock
    private GlusterVolumeSnapshotDao snapshotDao;

    @Mock
    private GlusterVolumeSnapshotConfigDao snapshotConfigDao;

    @Mock
    private ClusterDao clusterDao;

    @InjectMocks
    @Spy
    private GlusterSnapshotSyncJob syncJob;

    @Mock
    private GlusterAuditLogUtil logUtil;

    @Mock
    private GlusterUtil glusterUtil;

    @Mock
    private EngineLock engineLock;

    @BeforeEach
    public void init() {
        doReturn(getClusters()).when(clusterDao).getAll();
        doReturn(getValidCluster()).when(clusterDao).get(any());
        doReturn(getVolumes()).when(volumeDao).getByClusterId(CLUSTER_ID_1);
        doReturn(getServer()).when(glusterUtil).getRandomUpServer(any());
        doReturn(engineLock).when(syncJob).acquireVolumeSnapshotLock(any());
    }

    @Test
    public void testSyncSnapshotsList() {
        doReturn(getSnapshotVDSReturnVal()).when(syncJob)
                .runVdsCommand(eq(VDSCommandType.GetGlusterVolumeSnapshotInfo),
                        argThat(snapshotInfoParam()));
        when(volumeDao.getById(any())).thenReturn(getVolume(CLUSTER_ID_1, VOLUME_ID_1, VOLUME_NAME_1));
        syncJob.refreshSnapshotList();
        verify(snapshotDao, times(1)).saveAll(any());
        verify(snapshotDao, times(1)).removeAll(any());
        verify(snapshotDao, times(1)).updateAllInBatch(any());
    }

    @Test
    public void testSyncSnapshotConfigs() {
        doReturn(getClusterSnapMaxLimit()).when(snapshotConfigDao)
                .getConfigByClusterIdAndName(CLUSTER_ID_1, PARAM_SNAP_MAX_LIMIT);
        doReturn(getVolume(CLUSTER_ID_1, VOLUME_ID_1, VOLUME_NAME_1)).when(volumeDao).getByName(CLUSTER_ID_1,
                VOLUME_NAME_1);
        doReturn(getVolume(CLUSTER_ID_1, VOLUME_ID_2, VOLUME_NAME_2)).when(volumeDao).getByName(CLUSTER_ID_1,
                VOLUME_NAME_2);
        doReturn(getVolumeSnapMaxLimit()).when(snapshotConfigDao)
                .getConfigByVolumeIdAndName(CLUSTER_ID_1, VOLUME_ID_1, PARAM_SNAP_MAX_LIMIT);
        doReturn(getSnapshotConfigVDSReturnValue()).when(syncJob)
                .runVdsCommand(eq(VDSCommandType.GetGlusterVolumeSnapshotConfigInfo),
                        argThat(snapshotInfoParam()));
        syncJob.refreshSnapshotConfig();
        verify(snapshotConfigDao, times(3)).save(any());
        verify(snapshotConfigDao, times(1)).updateConfigByClusterIdAndName(any(), any(), any());
        verify(snapshotConfigDao, times(1)).updateConfigByVolumeIdAndName(
                any(), any(), any(), any());
    }

    private GlusterVolumeSnapshotConfig getClusterSnapMaxLimit() {
        GlusterVolumeSnapshotConfig param = new GlusterVolumeSnapshotConfig();
        param.setClusterId(CLUSTER_ID_1);
        param.setVolumeId(null);
        param.setParamName(PARAM_SNAP_MAX_LIMIT);
        param.setParamValue("256");
        return param;
    }

    private GlusterVolumeSnapshotConfig getVolumeSnapMaxLimit() {
        GlusterVolumeSnapshotConfig param = new GlusterVolumeSnapshotConfig();
        param.setClusterId(CLUSTER_ID_1);
        param.setVolumeId(VOLUME_ID_1);
        param.setParamName(PARAM_SNAP_MAX_LIMIT);
        param.setParamValue("20");
        return param;
    }

    private ArgumentMatcher<GlusterVolumeSnapshotVDSParameters> snapshotInfoParam() {
        return argument -> argument.getClusterId().equals(CLUSTER_ID_1);
    }

    private VDSReturnValue getSnapshotConfigVDSReturnValue() {
        VDSReturnValue vdsRetValue = new VDSReturnValue();
        vdsRetValue.setSucceeded(true);
        vdsRetValue.setReturnValue(getSnapshotConfigInfo());
        return vdsRetValue;
    }

    private GlusterSnapshotConfigInfo getSnapshotConfigInfo() {
        GlusterSnapshotConfigInfo config = new GlusterSnapshotConfigInfo();
        Map<String, String> clusterConfigs = new HashMap<>();
        clusterConfigs.put("snap-max-hard-limit", "200");
        clusterConfigs.put("snap-max-soft-limit", "90%");
        clusterConfigs.put("auto-delete", "enable");
        config.setClusterConfigOptions(clusterConfigs);

        Map<String, Map<String, String>> volumeConfigs = new HashMap<>();

        Map<String, String> volConf1 = new HashMap<>();
        volConf1.put("snap-max-hard-limit", "30");
        volumeConfigs.put(VOLUME_NAME_1, volConf1);

        Map<String, String> volConf2 = new HashMap<>();
        volConf2.put("snap-max-hard-limit", "50");
        volumeConfigs.put(VOLUME_NAME_2, volConf2);

        config.setVolumeConfigOptions(volumeConfigs);

        return config;
    }

    private VDSReturnValue getSnapshotVDSReturnVal() {
        VDSReturnValue vdsRetValue = new VDSReturnValue();
        vdsRetValue.setSucceeded(true);
        vdsRetValue.setReturnValue(getSnapshotDetails());
        return vdsRetValue;
    }

    private List<GlusterVolumeSnapshotEntity> getSnapshotDetails() {
        List<GlusterVolumeSnapshotEntity> snapshots = new ArrayList<>();

        GlusterVolumeSnapshotEntity snap1 = new GlusterVolumeSnapshotEntity();
        snap1.setClusterId(CLUSTER_ID_1);
        snap1.setCreatedAt(existingSnapsCreateDate);
        snap1.setDescription("");
        snap1.setId(existingSnapshotIds[0]);
        snap1.setSnapshotName(existingSnapshotNames[0]);
        snap1.setStatus(GlusterSnapshotStatus.DEACTIVATED);
        snap1.setVolumeId(VOLUME_ID_1);
        snapshots.add(snap1);

        GlusterVolumeSnapshotEntity snap2 = new GlusterVolumeSnapshotEntity();
        snap2.setClusterId(CLUSTER_ID_1);
        snap2.setCreatedAt(new Date());
        snap2.setDescription("");
        snap2.setId(newSnapshotId);
        snap2.setSnapshotName(newSnapshotName);
        snap2.setStatus(GlusterSnapshotStatus.ACTIVATED);
        snap2.setVolumeId(VOLUME_ID_1);
        snapshots.add(snap2);

        return snapshots;
    }

    private Cluster getValidCluster() {
        Cluster cluster = new Cluster();
        cluster.setId(CLUSTER_ID_1);
        cluster.setName("cluster");
        cluster.setGlusterService(true);
        cluster.setVirtService(false);
        cluster.setCompatibilityVersion(Version.getLast());

        return cluster;
    }

    private List<Cluster> getClusters() {
        List<Cluster> list = new ArrayList<>();

        Cluster cluster = new Cluster();
        cluster.setId(CLUSTER_ID_1);
        cluster.setName("cluster");
        cluster.setGlusterService(true);
        cluster.setVirtService(false);
        cluster.setCompatibilityVersion(Version.getLast());
        list.add(cluster);

        return list;
    }

    private VDS getServer() {
        VDS vds = new VDS();
        vds.setId(Guid.newGuid());
        return vds;
    }

    private List<GlusterVolumeEntity> getVolumes() {
        List<GlusterVolumeEntity> volList = new ArrayList<>();
        volList.add(getVolume(CLUSTER_ID_1, VOLUME_ID_1, VOLUME_NAME_1));
        return volList;
    }

    private GlusterVolumeEntity getVolume(Guid clusterId, Guid volumeId, String volumeName) {
        GlusterVolumeEntity volume = new GlusterVolumeEntity();
        volume.setName(volumeName);
        volume.setClusterId(clusterId);
        volume.setId(volumeId);
        volume.setReplicaCount(2);

        GlusterBrickEntity brick = new GlusterBrickEntity();
        brick.setVolumeId(volume.getId());
        brick.setBrickDirectory("/export/testvol1");
        brick.setStatus(GlusterStatus.UP);
        brick.setBrickOrder(0);
        volume.addBrick(brick);

        GlusterBrickEntity brick2 = new GlusterBrickEntity();
        brick2.setVolumeId(volume.getId());
        brick2.setBrickDirectory("/export/testvol1");
        brick2.setStatus(GlusterStatus.UP);
        brick2.setBrickOrder(1);
        volume.addBrick(brick2);

        return volume;
    }
}

package org.ovirt.engine.core.bll.gluster;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.ovirt.engine.core.utils.MockConfigRule.mockConfig;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.bll.utils.ClusterUtils;
import org.ovirt.engine.core.bll.utils.GlusterUtil;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterSnapshotConfigInfo;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterSnapshotStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeSnapshotConfig;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeSnapshotEntity;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.gluster.GlusterVolumeSnapshotVDSParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.gluster.GlusterAuditLogUtil;
import org.ovirt.engine.core.dao.VdsGroupDAO;
import org.ovirt.engine.core.dao.gluster.GlusterVolumeDao;
import org.ovirt.engine.core.dao.gluster.GlusterVolumeSnapshotConfigDao;
import org.ovirt.engine.core.dao.gluster.GlusterVolumeSnapshotDao;
import org.ovirt.engine.core.utils.MockConfigRule;
import org.ovirt.engine.core.utils.lock.EngineLock;

@RunWith(MockitoJUnitRunner.class)
public class GlusterSnapshotSyncJobTest {
    private static final Guid CLUSTER_ID_1 = Guid.newGuid();
    private static final Guid CLUSTER_ID_2 = Guid.newGuid();
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
    private static final String PARAM_SNAP_MAX_SOFT_LIMIT = "snap-max-soft-limit";
    private static final String PARAM_AUTO_DELETE = "auto-delete";

    @Mock
    private GlusterVolumeDao volumeDao;

    @Mock
    private ClusterUtils clusterUtils;

    @Mock
    private GlusterVolumeSnapshotDao snapshotDao;

    @Mock
    private GlusterVolumeSnapshotConfigDao snapshotConfigDao;

    @Mock
    private VdsGroupDAO clusterDao;

    private GlusterSnapshotSyncJob syncJob;

    @Mock
    private GlusterAuditLogUtil logUtil;

    @Mock
    private GlusterUtil glusterUtil;

    @Mock
    private EngineLock engineLock;

    @ClassRule
    public static MockConfigRule mcr = new MockConfigRule(
            mockConfig(ConfigValues.GlusterVolumeSnapshotSupported, Version.v3_5.toString(), true),
            mockConfig(ConfigValues.GlusterVolumeSnapshotSupported, Version.v3_4.toString(), false),
            mockConfig(ConfigValues.DefaultMinThreadPoolSize, 10),
            mockConfig(ConfigValues.DefaultMaxThreadPoolSize, 20),
            mockConfig(ConfigValues.DefaultMaxThreadWaitQueueSize, 10)
            );

    @Before
    public void init() {
        syncJob = Mockito.spy(GlusterSnapshotSyncJob.getInstance());
        MockitoAnnotations.initMocks(this);
        syncJob.setLogUtil(logUtil);

        doReturn(clusterDao).when(syncJob).getClusterDao();
        doReturn(volumeDao).when(syncJob).getGlusterVolumeDao();
        doReturn(snapshotDao).when(syncJob).getGlusterVolumeSnapshotDao();
        doReturn(snapshotConfigDao).when(syncJob).getGlusterVolumeSnapshotConfigDao();
        doReturn(clusterUtils).when(syncJob).getClusterUtils();

        doReturn(getClusters()).when(clusterDao).getAll();
        doReturn(getVolumes()).when(volumeDao).getByClusterId(argThat(validClusterId()));
        doReturn(getVolume(CLUSTER_ID_1, VOLUME_ID_1, VOLUME_NAME_1)).when(volumeDao)
                .getByName(argThat(validClusterId()), argThat(validVolumeName()));
        doReturn(getServer()).when(clusterUtils).getRandomUpServer(any(Guid.class));

        doReturn(engineLock).when(syncJob).acquireVolumeSnapshotLock(any(Guid.class));
    }

    @Test
    public void testSyncSnapshotsList() {
        doReturn(getExistingSnapshots()).when(snapshotDao).getAllByVolumeId(argThat(validVolumeId()));
        doReturn(getSnapshotVDSReturnVal(true)).when(syncJob)
                .runVdsCommand(eq(VDSCommandType.GetGlusterVolumeSnapshotInfo),
                        argThat(snapshotInfoParam()));
        syncJob.refreshSnapshotList();
        Mockito.verify(snapshotDao, times(1)).saveAll(any(List.class));
        Mockito.verify(snapshotDao, times(1)).removeAll(any(List.class));
        Mockito.verify(snapshotDao, times(1)).updateAllInBatch(any(List.class));
    }

    @Test
    public void testSyncSnapshotConfigs() {
        doReturn(getClusterSnapMaxLimit()).when(snapshotConfigDao)
                .getConfigByClusterIdAndName(CLUSTER_ID_1, PARAM_SNAP_MAX_LIMIT);
        doReturn(null).when(snapshotConfigDao).getConfigByClusterIdAndName(CLUSTER_ID_1,
                PARAM_SNAP_MAX_SOFT_LIMIT);
        doReturn(null).when(snapshotConfigDao).getConfigByClusterIdAndName(CLUSTER_ID_1,
                PARAM_AUTO_DELETE);
        doReturn(getVolume(CLUSTER_ID_1, VOLUME_ID_1, VOLUME_NAME_1)).when(volumeDao).getByName(CLUSTER_ID_1,
                VOLUME_NAME_1);
        doReturn(getVolume(CLUSTER_ID_1, VOLUME_ID_2, VOLUME_NAME_2)).when(volumeDao).getByName(CLUSTER_ID_1,
                VOLUME_NAME_2);
        doReturn(getVolumeSnapMaxLimit()).when(snapshotConfigDao)
                .getConfigByVolumeIdAndName(CLUSTER_ID_1, VOLUME_ID_1, PARAM_SNAP_MAX_LIMIT);
        doReturn(null).when(snapshotConfigDao).getConfigByVolumeIdAndName(CLUSTER_ID_1,
                VOLUME_ID_2,
                PARAM_SNAP_MAX_LIMIT);
        doReturn(getSnapshotConfigVDSReturnValue()).when(syncJob)
                .runVdsCommand(eq(VDSCommandType.GetGlusterVolumeSnapshotConfigInfo),
                        argThat(snapshotInfoParam()));
        syncJob.refreshSnapshotConfig();
        Mockito.verify(snapshotConfigDao, times(3)).save(any(GlusterVolumeSnapshotConfig.class));
        Mockito.verify(snapshotConfigDao, times(1)).updateConfigByClusterIdAndName(any(Guid.class),
                any(String.class),
                any(String.class));
        Mockito.verify(snapshotConfigDao, times(1)).updateConfigByVolumeIdAndName(any(Guid.class),
                any(Guid.class),
                any(String.class),
                any(String.class));
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
        return new ArgumentMatcher<GlusterVolumeSnapshotVDSParameters>() {

            @Override
            public boolean matches(Object argument) {
                if (!(argument instanceof GlusterVolumeSnapshotVDSParameters)) {
                    return false;
                }
                return ((GlusterVolumeSnapshotVDSParameters) argument).getClusterId().equals(CLUSTER_ID_1);
            }
        };
    }

    private ArgumentMatcher<Guid> validClusterId() {
        return new ArgumentMatcher<Guid>() {
            @Override
            public boolean matches(Object argument) {
                if (!(argument instanceof Guid)) {
                    return false;
                }
                return ((Guid) argument).equals(CLUSTER_ID_1);
            }
        };
    }

    private ArgumentMatcher<String> validVolumeName() {
        return new ArgumentMatcher<String>() {
            @Override
            public boolean matches(Object argument) {
                if (!(argument instanceof String)) {
                    return false;
                }
                return ((String) argument).equals(VOLUME_NAME_1);
            }
        };
    }

    private ArgumentMatcher<Guid> validVolumeId() {
        return new ArgumentMatcher<Guid>() {
            @Override
            public boolean matches(Object argument) {
                if (!(argument instanceof Guid)) {
                    return false;
                }
                return ((Guid) argument).equals(VOLUME_ID_1);
            }
        };
    }

    private List<GlusterVolumeSnapshotEntity> getExistingSnapshots() {
        List<GlusterVolumeSnapshotEntity> snapsList = new ArrayList<GlusterVolumeSnapshotEntity>();

        GlusterVolumeSnapshotEntity snap1 = new GlusterVolumeSnapshotEntity();
        snap1.setId(existingSnapshotIds[0]);
        snap1.setClusterId(CLUSTER_ID_1);
        snap1.setCreatedAt(existingSnapsCreateDate);
        snap1.setDescription("");
        snap1.setSnapshotName(existingSnapshotNames[0]);
        snap1.setStatus(GlusterSnapshotStatus.STARTED);
        snap1.setVolumeId(VOLUME_ID_1);
        snapsList.add(snap1);

        GlusterVolumeSnapshotEntity snap2 = new GlusterVolumeSnapshotEntity();
        snap2.setId(existingSnapshotIds[1]);
        snap2.setClusterId(CLUSTER_ID_1);
        snap2.setCreatedAt(existingSnapsCreateDate);
        snap2.setDescription("");
        snap2.setSnapshotName(existingSnapshotNames[1]);
        snap2.setStatus(GlusterSnapshotStatus.STARTED);
        snap2.setVolumeId(VOLUME_ID_1);
        snapsList.add(snap2);

        return snapsList;
    }

    private Object getSnapshotConfigVDSReturnValue() {
        VDSReturnValue vdsRetValue = new VDSReturnValue();
        vdsRetValue.setSucceeded(true);
        vdsRetValue.setReturnValue(getSnapshotConfigInfo());
        return vdsRetValue;
    }

    private GlusterSnapshotConfigInfo getSnapshotConfigInfo() {
        GlusterSnapshotConfigInfo config = new GlusterSnapshotConfigInfo();
        Map<String, String> clusterConfigs = new HashMap<String, String>();
        clusterConfigs.put("snap-max-hard-limit", "200");
        clusterConfigs.put("snap-max-soft-limit", "90%");
        clusterConfigs.put("auto-delete", "enable");
        config.setClusterConfigOptions(clusterConfigs);

        Map<String, Map<String, String>> volumeConfigs = new HashMap<String, Map<String, String>>();

        Map<String, String> volConf1 = new HashMap<String, String>();
        volConf1.put("snap-max-hard-limit", "30");
        volumeConfigs.put(VOLUME_NAME_1, volConf1);

        Map<String, String> volConf2 = new HashMap<String, String>();
        volConf2.put("snap-max-hard-limit", "50");
        volumeConfigs.put(VOLUME_NAME_2, volConf2);

        config.setVolumeConfigOptions(volumeConfigs);

        return config;
    }

    private Object getSnapshotVDSReturnVal(boolean ret) {
        VDSReturnValue vdsRetValue = new VDSReturnValue();
        vdsRetValue.setSucceeded(ret);
        if (ret) {
            vdsRetValue.setReturnValue(getSnapshotDetails());
        } else {
            vdsRetValue.setReturnValue(null);
        }
        return vdsRetValue;
    }

    private List<GlusterVolumeSnapshotEntity> getSnapshotDetails() {
        List<GlusterVolumeSnapshotEntity> snapshots = new ArrayList<GlusterVolumeSnapshotEntity>();

        GlusterVolumeSnapshotEntity snap1 = new GlusterVolumeSnapshotEntity();
        snap1.setClusterId(CLUSTER_ID_1);
        snap1.setCreatedAt(existingSnapsCreateDate);
        snap1.setDescription("");
        snap1.setId(existingSnapshotIds[0]);
        snap1.setSnapshotName(existingSnapshotNames[0]);
        snap1.setStatus(GlusterSnapshotStatus.STOPPED);
        snap1.setVolumeId(VOLUME_ID_1);
        snapshots.add(snap1);

        GlusterVolumeSnapshotEntity snap2 = new GlusterVolumeSnapshotEntity();
        snap2.setClusterId(CLUSTER_ID_1);
        snap2.setCreatedAt(new Date());
        snap2.setDescription("");
        snap2.setId(newSnapshotId);
        snap2.setSnapshotName(newSnapshotName);
        snap2.setStatus(GlusterSnapshotStatus.STARTED);
        snap2.setVolumeId(VOLUME_ID_1);
        snapshots.add(snap2);

        return snapshots;
    }

    private List<VDSGroup> getClusters() {
        List<VDSGroup> list = new ArrayList<>();

        VDSGroup cluster = new VDSGroup();
        cluster.setId(CLUSTER_ID_1);
        cluster.setName("cluster");
        cluster.setGlusterService(true);
        cluster.setVirtService(false);
        cluster.setCompatibilityVersion(Version.v3_5);
        list.add(cluster);

        VDSGroup cluster1 = new VDSGroup();
        cluster1.setId(CLUSTER_ID_2);
        cluster1.setName("cluster1");
        cluster1.setGlusterService(true);
        cluster1.setVirtService(false);
        cluster1.setCompatibilityVersion(Version.v3_4);
        list.add(cluster1);

        return list;
    }

    private VDS getServer() {
        VDS vds = new VDS();
        vds.setId(Guid.newGuid());
        return vds;
    }

    private List<GlusterVolumeEntity> getVolumes() {
        List<GlusterVolumeEntity> volList = new ArrayList<GlusterVolumeEntity>();
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

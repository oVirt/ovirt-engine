package org.ovirt.engine.core.bll.gluster;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.ovirt.engine.core.common.utils.MockConfigRule.mockConfig;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.bll.utils.GlusterUtil;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.gluster.GeoRepSessionStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterGeoRepSession;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterGeoRepSessionConfiguration;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterGeoRepSessionDetails;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.utils.MockConfigRule;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.gluster.GlusterVolumeGeoRepSessionVDSParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.gluster.GlusterAuditLogUtil;
import org.ovirt.engine.core.dao.ClusterDao;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.dao.gluster.GlusterGeoRepDao;
import org.ovirt.engine.core.dao.gluster.GlusterVolumeDao;
import org.ovirt.engine.core.utils.lock.EngineLock;

@RunWith(MockitoJUnitRunner.class)
public class GlusterGeoRepSyncJobTest {
    private static final Guid CLUSTER_GUID = new Guid("CC111111-1111-1111-1111-111111111111");

    @Mock
    private GlusterGeoRepDao geoRepDao;

    @Mock
    private GlusterUtil glusterUtil;

    @Mock
    private ClusterDao clusterDao;

    @Mock
    private VdsDao vdsDao;

    @Mock
    private GlusterVolumeDao volumeDao;

    private GlusterGeoRepSyncJob syncJob;

    @Mock
    private GlusterAuditLogUtil logUtil;

    @ClassRule
    public static MockConfigRule mcr = new MockConfigRule(
            mockConfig(ConfigValues.DefaultMinThreadPoolSize, 10),
            mockConfig(ConfigValues.DefaultMaxThreadPoolSize, 20),
            mockConfig(ConfigValues.DefaultMaxThreadWaitQueueSize, 10)
            );

    @Before
    public void init() {
        syncJob = Mockito.spy(GlusterGeoRepSyncJob.getInstance());
        MockitoAnnotations.initMocks(this);
        syncJob.setLogUtil(logUtil);
        doReturn(clusterDao).when(syncJob).getClusterDao();
        doReturn(vdsDao).when(syncJob).getVdsDao();
        doReturn(geoRepDao).when(syncJob).getGeoRepDao();
        doReturn(volumeDao).when(syncJob).getVolumeDao();
        doReturn(getClusters()).when(clusterDao).getAll();
        doReturn(getVolume()).when(volumeDao).getByName(any(Guid.class), any(String.class));
        doReturn(getVolume()).when(volumeDao).getById(any(Guid.class));
        doReturn(getServer()).when(glusterUtil).getRandomUpServer(any(Guid.class));
        doReturn(glusterUtil).when(syncJob).getGlusterUtil();
        doReturn(getMockLock()).when(syncJob).acquireGeoRepSessionLock(any(Guid.class));
        doReturn(getSessions(2, true)).when(geoRepDao).getGeoRepSessionsInCluster(CLUSTER_GUID);
    }

    @Test
    public void testDiscoverGeoRepData() {

        doReturn(getSessionsVDSReturnVal(true, 2)).when(syncJob)
                .runVdsCommand(eq(VDSCommandType.GetGlusterVolumeGeoRepSessionList),
                        any(GlusterVolumeGeoRepSessionVDSParameters.class));
        syncJob.discoverGeoRepData();
        Mockito.verify(geoRepDao, times(2)).save(any(GlusterGeoRepSession.class));
    }

    @Test
    public void testDiscoverGeoRepDataWithConfig() {

        doReturn(getSessionsVDSReturnVal(true, 2)).when(syncJob)
                .runVdsCommand(eq(VDSCommandType.GetGlusterVolumeGeoRepSessionList),
                        any(GlusterVolumeGeoRepSessionVDSParameters.class));
        doReturn(getSessionsConfigListVDSReturnVal(true)).when(syncJob)
                .runVdsCommand(eq(VDSCommandType.GetGlusterVolumeGeoRepConfigList),
                any(GlusterVolumeGeoRepSessionVDSParameters.class));
        syncJob.discoverGeoRepData();
        Mockito.verify(geoRepDao, times(2)).save(any(GlusterGeoRepSession.class));
        Mockito.verify(geoRepDao, times(2)).saveConfig(any(GlusterGeoRepSessionConfiguration.class));
    }

    @Test
    public void testDiscoverGeoRepDataWhenNoSessions() {

        doReturn(getSessionsVDSReturnVal(true, 0)).when(syncJob)
                .runVdsCommand(eq(VDSCommandType.GetGlusterVolumeGeoRepSessionList),
                        any(GlusterVolumeGeoRepSessionVDSParameters.class));
        syncJob.discoverGeoRepData();
        Mockito.verify(geoRepDao, times(0)).save(any(GlusterGeoRepSession.class));
    }

    @Test
    public void testRefreshStatus() {
        doReturn(getSessionDetailsVDSReturnVal(true)).when(syncJob)
                .runVdsCommand(eq(VDSCommandType.GetGlusterVolumeGeoRepSessionStatus),
                any(GlusterVolumeGeoRepSessionVDSParameters.class));
        syncJob.refreshGeoRepSessionStatus();
        Mockito.verify(geoRepDao, times(2)).saveOrUpdateDetailsInBatch(any(List.class));
    }

    @Test
    public void testRefreshStatusNoSessions() {
        doReturn(getSessionDetailsVDSReturnVal(false)).when(syncJob)
                .runVdsCommand(eq(VDSCommandType.GetGlusterVolumeGeoRepSessionStatus),
                        any(GlusterVolumeGeoRepSessionVDSParameters.class));
        syncJob.refreshGeoRepSessionStatus();
        Mockito.verify(geoRepDao, times(0)).saveOrUpdateDetailsInBatch(any(List.class));
    }

    private EngineLock getMockLock() {
        return new EngineLock() {

            @Override
            public void close() {

            }

        };
    }

    private Object getSessionsVDSReturnVal(boolean ret, int count) {
        VDSReturnValue vdsRetValue = new VDSReturnValue();
        vdsRetValue.setSucceeded(ret);
        if (ret) {
            vdsRetValue.setReturnValue(getSessions(count, false));
        } else {
            vdsRetValue.setReturnValue(null);
        }
        return vdsRetValue;
    }

    private Object getSessionDetailsVDSReturnVal(boolean ret) {
        VDSReturnValue vdsRetValue = new VDSReturnValue();
        vdsRetValue.setSucceeded(ret);
        if (ret) {
            vdsRetValue.setReturnValue(getSessionDetailsList());
        } else {
            vdsRetValue.setReturnValue(null);
        }
        return vdsRetValue;
    }

    private Object getSessionsConfigListVDSReturnVal(boolean ret) {
        VDSReturnValue vdsRetValue = new VDSReturnValue();
        vdsRetValue.setSucceeded(ret);
        if (ret) {
            vdsRetValue.setReturnValue(getSessionConfigList());
        } else {
            vdsRetValue.setReturnValue(null);
        }
        return vdsRetValue;
    }

    private List<GlusterGeoRepSessionConfiguration> getSessionConfigList() {
        List<GlusterGeoRepSessionConfiguration> configList = new ArrayList<>();
        GlusterGeoRepSessionConfiguration config = new GlusterGeoRepSessionConfiguration();
        config.setKey("georep-crawl");
        config.setValue("hybrid");
        configList.add(config);
        return configList;
    }

    private List<GlusterGeoRepSession> getSessions(int count, boolean populateVoId) {
        List<GlusterGeoRepSession> sessions = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            sessions.add(getSession(populateVoId));
        }
        return sessions;
    }

    private GlusterGeoRepSession getSession(boolean populateVoId) {
        GlusterGeoRepSession session = new GlusterGeoRepSession();
        session.setMasterVolumeName("VOL1");
        if (populateVoId) {
            session.setMasterVolumeId(Guid.newGuid());
        }
        session.setId(Guid.newGuid());
        session.setSessionKey(session.getId() + session.getMasterVolumeName());
        session.setStatus(GeoRepSessionStatus.ACTIVE);
        session.setSessionDetails(getSessionDetailsList());
        return session;
    }

    private ArrayList<GlusterGeoRepSessionDetails> getSessionDetailsList() {
        ArrayList<GlusterGeoRepSessionDetails> list = new ArrayList<>();
        GlusterGeoRepSessionDetails details = new GlusterGeoRepSessionDetails();
        details.setMasterBrickId(Guid.newGuid());
        list.add(details);
        return list;
    }

    private List<Cluster> getClusters() {
        List<Cluster> list = new ArrayList<>();
        list.add(createCluster());
        return list;
    }

    private static Cluster createCluster() {
        Cluster cluster = new Cluster();
        cluster.setId(CLUSTER_GUID);
        cluster.setName("cluster");
        cluster.setGlusterService(true);
        cluster.setVirtService(false);
        cluster.setCompatibilityVersion(Version.v4_0);
        return cluster;
    }

    private VDS getServer() {
        VDS vds = new VDS();
        vds.setId(Guid.newGuid());
        return vds;
    }

    private GlusterVolumeEntity getVolume() {
        GlusterVolumeEntity volume = new GlusterVolumeEntity();
        volume.setName("VOL1");
        volume.setClusterId(Guid.newGuid());
        volume.setId(Guid.newGuid());
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

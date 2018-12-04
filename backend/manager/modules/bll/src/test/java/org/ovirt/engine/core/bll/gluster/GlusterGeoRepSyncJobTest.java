package org.ovirt.engine.core.bll.gluster;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.bll.utils.GlusterAuditLogUtil;
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
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dao.ClusterDao;
import org.ovirt.engine.core.dao.StorageDomainDRDao;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.dao.gluster.GlusterGeoRepDao;
import org.ovirt.engine.core.dao.gluster.GlusterVolumeDao;
import org.ovirt.engine.core.utils.ExecutorServiceExtension;
import org.ovirt.engine.core.utils.lock.LockManager;

@ExtendWith({MockitoExtension.class, ExecutorServiceExtension.class})
@MockitoSettings(strictness = Strictness.LENIENT)
public class GlusterGeoRepSyncJobTest {
    private static final Guid CLUSTER_GUID = new Guid("CC111111-1111-1111-1111-111111111111");

    @Mock
    private LockManager lockManager;

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

    @Mock
    private StorageDomainDRDao storageDomainDRDao;

    @InjectMocks
    @Spy
    private GlusterGeoRepSyncJob syncJob;

    @Mock
    private GlusterAuditLogUtil logUtil;

    @BeforeEach
    public void init() {
        doReturn(getClusters()).when(clusterDao).getAll();
        doReturn(getVolume()).when(volumeDao).getByName(any(), any());
        doReturn(getVolume()).when(volumeDao).getById(any());
        doReturn(getServer()).when(glusterUtil).getRandomUpServer(any());
        doReturn(getSessions(2, true)).when(geoRepDao).getGeoRepSessionsInCluster(CLUSTER_GUID);
    }

    @Test
    public void testDiscoverGeoRepData() {

        doReturn(getSessionsVDSReturnVal(2)).when(syncJob)
                .runVdsCommand(eq(VDSCommandType.GetGlusterVolumeGeoRepSessionList), any());
        syncJob.discoverGeoRepData();
        verify(geoRepDao, times(2)).save(any());
    }

    @Test
    public void testDiscoverGeoRepDataWithConfig() {

        doReturn(getSessionsVDSReturnVal(2)).when(syncJob)
                .runVdsCommand(eq(VDSCommandType.GetGlusterVolumeGeoRepSessionList), any());
        doReturn(getSessionsConfigListVDSReturnVal()).when(syncJob)
                .runVdsCommand(eq(VDSCommandType.GetGlusterVolumeGeoRepConfigList), any());
        syncJob.discoverGeoRepData();
        verify(geoRepDao, times(2)).save(any());
        verify(geoRepDao, times(2)).saveConfig(any());
    }

    @Test
    public void testDiscoverGeoRepDataWhenNoSessions() {

        doReturn(getSessionsVDSReturnVal(0)).when(syncJob)
                .runVdsCommand(eq(VDSCommandType.GetGlusterVolumeGeoRepSessionList), any());
        syncJob.discoverGeoRepData();
        verify(geoRepDao, times(0)).save(any());
    }

    @Test
    public void testRefreshStatus() {
        doReturn(getSessionDetailsVDSReturnVal(true)).when(syncJob)
                .runVdsCommand(eq(VDSCommandType.GetGlusterVolumeGeoRepSessionStatus), any());
        syncJob.refreshGeoRepSessionStatus();
        verify(geoRepDao, times(2)).saveOrUpdateDetailsInBatch(any());
    }

    @Test
    public void testRefreshStatusNoSessions() {
        doReturn(getSessionDetailsVDSReturnVal(false)).when(syncJob)
                .runVdsCommand(eq(VDSCommandType.GetGlusterVolumeGeoRepSessionStatus), any());
        syncJob.refreshGeoRepSessionStatus();
        verify(geoRepDao, times(0)).saveOrUpdateDetailsInBatch(any());
    }

    private Object getSessionsVDSReturnVal(int count) {
        VDSReturnValue vdsRetValue = new VDSReturnValue();
        vdsRetValue.setSucceeded(true);
        vdsRetValue.setReturnValue(getSessions(count, false));
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

    private Object getSessionsConfigListVDSReturnVal() {
        VDSReturnValue vdsRetValue = new VDSReturnValue();
        vdsRetValue.setSucceeded(true);
        vdsRetValue.setReturnValue(getSessionConfigList());
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
        cluster.setCompatibilityVersion(Version.getLast());
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

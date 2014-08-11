package org.ovirt.engine.core.bll.gluster;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.ovirt.engine.core.utils.MockConfigRule.mockConfig;

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
import org.ovirt.engine.core.bll.utils.ClusterUtils;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.gluster.GeoRepSessionStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterGeoRepSession;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterGeoRepSessionDetails;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.gluster.GlusterVolumeGeoRepSessionVDSParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.gluster.GlusterAuditLogUtil;
import org.ovirt.engine.core.dao.VdsGroupDAO;
import org.ovirt.engine.core.dao.gluster.GlusterGeoRepDao;
import org.ovirt.engine.core.dao.gluster.GlusterVolumeDao;
import org.ovirt.engine.core.utils.MockConfigRule;

@RunWith(MockitoJUnitRunner.class)
public class GlusterGeoRepSyncJobTest {

    @Mock
    private GlusterGeoRepDao geoRepDao;

    @Mock
    private ClusterUtils clusterUtils;

    @Mock
    private VdsGroupDAO clusterDao;

    @Mock
    private GlusterVolumeDao volumeDao;

    private GlusterGeoRepSyncJob syncJob;

    @Mock
    private GlusterAuditLogUtil logUtil;

    @ClassRule
    public static MockConfigRule mcr = new MockConfigRule(
            mockConfig(ConfigValues.GlusterGeoReplicationEnabled, Version.v3_5.toString(), true),
            mockConfig(ConfigValues.GlusterGeoReplicationEnabled, Version.v3_4.toString(), false)
            );

    @Before
    public void init() {
        syncJob = Mockito.spy(GlusterGeoRepSyncJob.getInstance());
        MockitoAnnotations.initMocks(this);
        doReturn(clusterDao).when(syncJob).getClusterDao();
        doReturn(geoRepDao).when(syncJob).getGeoRepDao();
        doReturn(volumeDao).when(syncJob).getVolumeDao();
        doReturn(clusterUtils).when(syncJob).getClusterUtils();
        syncJob.setLogUtil(logUtil);
        doReturn(getClusters()).when(clusterDao).getAll();
        doReturn(getVolume()).when(volumeDao).getByName(any(Guid.class), any(String.class));
        doReturn(getServer()).when(clusterUtils).getRandomUpServer(any(Guid.class));
    }

    @Test
    public void testSync() {

        doReturn(getSessionsVDSReturnVal(true, 2)).when(syncJob)
                .runVdsCommand(eq(VDSCommandType.GetGlusterVolumeGeoRepStatus),
                        any(GlusterVolumeGeoRepSessionVDSParameters.class));
        syncJob.refreshGeoRepData();
        Mockito.verify(geoRepDao, times(2)).save(any(GlusterGeoRepSession.class));
    }

    @Test
    public void testSyncWhenNoSessions() {

        doReturn(getSessionsVDSReturnVal(true, 0)).when(syncJob)
                .runVdsCommand(eq(VDSCommandType.GetGlusterVolumeGeoRepStatus),
                        any(GlusterVolumeGeoRepSessionVDSParameters.class));
        syncJob.refreshGeoRepData();
        Mockito.verify(geoRepDao, times(0)).save(any(GlusterGeoRepSession.class));
    }

    private Object getSessionsVDSReturnVal(boolean ret, int count) {
        VDSReturnValue vdsRetValue = new VDSReturnValue();
        vdsRetValue.setSucceeded(ret);
        if (ret) {
            vdsRetValue.setReturnValue(getSessions(count));
        } else {
            vdsRetValue.setReturnValue(null);
        }
        return vdsRetValue;
    }

    private List<GlusterGeoRepSession> getSessions(int count) {
        List<GlusterGeoRepSession> sessions = new ArrayList<GlusterGeoRepSession>();
        for (int i = 0; i < count; i++) {
            sessions.add(getSession());
        }
        return sessions;
    }

    private GlusterGeoRepSession getSession() {
        GlusterGeoRepSession session = new GlusterGeoRepSession();
        session.setMasterVolumeName("VOL1");
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

    private List<VDSGroup> getClusters() {
        List<VDSGroup> list = new ArrayList<>();
        list.add(createCluster(Version.v3_4));
        list.add(createCluster(Version.v3_5));
        return list;
    }

    private VDSGroup createCluster(Version v) {
        VDSGroup cluster = new VDSGroup();
        cluster.setId(Guid.newGuid());
        cluster.setName("cluster");
        cluster.setGlusterService(true);
        cluster.setVirtService(false);
        cluster.setcompatibility_version(v);
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

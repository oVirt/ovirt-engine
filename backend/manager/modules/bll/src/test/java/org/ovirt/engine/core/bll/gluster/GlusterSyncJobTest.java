package org.ovirt.engine.core.bll.gluster;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.times;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import javax.transaction.TransactionManager;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.ArgumentMatcher;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.mockito.verification.VerificationMode;
import org.ovirt.engine.core.bll.utils.GlusterAuditLogUtil;
import org.ovirt.engine.core.bll.utils.GlusterUtil;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.gluster.AccessProtocol;
import org.ovirt.engine.core.common.businessentities.gluster.BrickDetails;
import org.ovirt.engine.core.common.businessentities.gluster.BrickProperties;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterServer;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterServerInfo;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeAdvancedDetails;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeOptionEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeSizeInfo;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeType;
import org.ovirt.engine.core.common.businessentities.gluster.PeerStatus;
import org.ovirt.engine.core.common.businessentities.gluster.TransportType;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.utils.gluster.GlusterCoreUtil;
import org.ovirt.engine.core.common.vdscommands.RemoveVdsVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.ClusterDao;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.dao.VdsDynamicDao;
import org.ovirt.engine.core.dao.VdsStaticDao;
import org.ovirt.engine.core.dao.VdsStatisticsDao;
import org.ovirt.engine.core.dao.gluster.GlusterBrickDao;
import org.ovirt.engine.core.dao.gluster.GlusterOptionDao;
import org.ovirt.engine.core.dao.gluster.GlusterServerDao;
import org.ovirt.engine.core.dao.gluster.GlusterVolumeDao;
import org.ovirt.engine.core.dao.network.NetworkDao;
import org.ovirt.engine.core.utils.InjectedMock;
import org.ovirt.engine.core.utils.InjectorExtension;
import org.ovirt.engine.core.utils.MockConfigDescriptor;
import org.ovirt.engine.core.utils.MockConfigExtension;

@ExtendWith({MockitoExtension.class, MockConfigExtension.class, InjectorExtension.class})
@MockitoSettings(strictness = Strictness.LENIENT)
public class GlusterSyncJobTest {

    private static final String REPL_VOL_NAME = "repl-vol";

    private static final String DIST_VOL_NAME = "dist-vol";

    @Mock
    private GlusterUtil glusterUtil;

    public static Stream<MockConfigDescriptor<?>> mockConfiguration() {
        return Stream.of(MockConfigDescriptor.of(ConfigValues.GlusterMetaVolumeName, "gluster_shared_storage"));
    }

    @Spy
    @InjectMocks
    private GlusterSyncJob glusterManager;

    @Mock
    private GlusterAuditLogUtil logUtil;

    private static final String OPTION_AUTH_ALLOW = "auth.allow";
    private static final String OPTION_AUTH_REJECT = "auth.reject";
    private static final String OPTION_NFS_DISABLE = "nfs.disable";
    private static final String AUTH_REJECT_IP = "192.168.1.999";
    private static final String OPTION_VALUE_ON = "on";
    private static final String OPTION_VALUE_OFF = "off";
    private static final Guid SERVER_ID_1 = new Guid("23f6d691-5dfb-472b-86dc-9e1d2d3c18f3");
    private static final Guid SERVER_ID_2 = new Guid("2001751e-549b-4e7a-aff6-32d36856c125");
    private static final Guid SERVER_ID_3 = new Guid("2001751e-549b-4e7a-aff6-32d36856c126");
    private static final Guid GLUSTER_SERVER_UUID_1 = new Guid("24f4d494-5dfb-472b-86dc-9e1d2d3c18f3");
    private static final String SERVER_NAME_1 = "srvr1";
    private static final String SERVER_NAME_2 = "srvr2";
    private static final String SERVER_NAME_3 = "srvr3";
    private static final String DIST_BRICK_D1 = "/export/test-vol-dist/dir1";
    private static final String DIST_BRICK_D2 = "/export/test-vol-dist/dir2";
    private static final String REPL_BRICK_R1D1 = "/export/test-vol-replicate-1/r1dir1";
    private static final String REPL_BRICK_R1D2 = "/export/test-vol-replicate-1/r1dir2";
    private static final String REPL_BRICK_R2D1 = "/export/test-vol-replicate-1/r2dir1";
    private static final String REPL_BRICK_R2D2 = "/export/test-vol-replicate-1/r2dir2";
    private static final String REPL_BRICK_R1D1_NEW = "/export/test-vol-replicate-1/r1dir1_new";
    private static final String REPL_BRICK_R2D1_NEW = "/export/test-vol-replicate-1/r2dir1_new";
    private static final Guid CLUSTER_ID = new Guid("ae956031-6be2-43d6-bb8f-5191c9253314");
    private static final Guid EXISTING_VOL_DIST_ID = new Guid("0c3f45f6-3fe9-4b35-a30c-be0d1a835ea8");
    private static final Guid EXISTING_VOL_REPL_ID = new Guid("b2cb2f73-fab3-4a42-93f0-d5e4c069a43e");
    private static final Guid NEW_VOL_ID = new Guid("98918f1c-a3d7-4abe-ab25-563bbf0d4fd3");
    private static final String NEW_VOL_NAME = "test-new-vol";

    @InjectedMock
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    public TransactionManager transactionManager;
    @Mock
    private GlusterVolumeDao volumeDao;
    @Mock
    private GlusterBrickDao brickDao;
    @Mock
    private GlusterOptionDao optionDao;
    @Mock
    private VdsDao vdsDao;
    @Mock
    private VdsStatisticsDao vdsStatisticsDao;
    @Mock
    private VdsStaticDao vdsStaticDao;
    @Mock
    private VdsDynamicDao vdsDynamicDao;
    @Mock
    private ClusterDao clusterDao;
    @Mock
    private GlusterServerDao glusterServerDao;
    @Mock
    private NetworkDao networkDao;

    private Cluster existingCluster;
    private VDS existingServer1;
    private VDS existingServer2;
    private final List<VDS> existingServers = new ArrayList<>();
    private GlusterVolumeEntity existingDistVol;
    private GlusterVolumeEntity existingReplVol;
    private GlusterVolumeEntity newVolume;
    private final List<GlusterVolumeEntity> existingVolumes = new ArrayList<>();
    private final List<Guid> removedBrickIds = new ArrayList<>();
    private final List<Guid> addedBrickIds = new ArrayList<>();
    private final List<GlusterBrickEntity> bricksWithChangedStatus = new ArrayList<>();

    private void createObjects() {
        existingServer1 = createServer(SERVER_ID_1, SERVER_NAME_1);
        existingServer2 = createServer(SERVER_ID_2, SERVER_NAME_2);
        existingServers.add(existingServer1);
        existingServers.add(existingServer2);
        existingServers.add(createServer(SERVER_ID_3, SERVER_NAME_3));

        existingDistVol = createDistVol(DIST_VOL_NAME, EXISTING_VOL_DIST_ID);
        existingReplVol = createReplVol();
    }

    private void createCluster() {
        existingCluster = new Cluster();
        existingCluster.setId(CLUSTER_ID);
        existingCluster.setName("cluster");
        existingCluster.setGlusterService(true);
        existingCluster.setVirtService(false);
        existingCluster.setGlusterCliBasedSchedulingOn(true);
        createObjects();
    }

    private VDS createServer(Guid serverId, String hostname) {
        VDS vds = new VDS();
        vds.setId(serverId);
        vds.setHostName(hostname);
        vds.setStatus(VDSStatus.Up);
        return vds;
    }

    private GlusterVolumeEntity createDistVol(String volName, Guid volId) {
        GlusterVolumeEntity vol = createVolume(volName, volId);
        vol.getAdvancedDetails().setCapacityInfo(getCapacityInfo(volId));
        vol.addBrick(createBrick(volId, existingServer1, DIST_BRICK_D1));
        vol.addBrick(createBrick(volId, existingServer1, DIST_BRICK_D2));
        existingVolumes.add(vol);
        return vol;
    }

    private GlusterVolumeSizeInfo getCapacityInfo(Guid volId) {
        GlusterVolumeSizeInfo capacityInfo = new GlusterVolumeSizeInfo();
        capacityInfo.setVolumeId(volId);
        capacityInfo.setTotalSize(90000L);
        capacityInfo.setUsedSize(90000L);
        capacityInfo.setFreeSize(10000L);
        return capacityInfo;
    }

    private GlusterVolumeEntity createReplVol() {
        GlusterVolumeEntity vol = createVolume(REPL_VOL_NAME, EXISTING_VOL_REPL_ID);
        vol.addBrick(createBrick(EXISTING_VOL_REPL_ID, existingServer1, REPL_BRICK_R1D1));
        vol.addBrick(createBrick(EXISTING_VOL_REPL_ID, existingServer2, REPL_BRICK_R1D2));
        vol.addBrick(createBrick(EXISTING_VOL_REPL_ID, existingServer1, REPL_BRICK_R2D1));
        vol.addBrick(createBrick(EXISTING_VOL_REPL_ID, existingServer2, REPL_BRICK_R2D2));
        vol.setOption(OPTION_AUTH_ALLOW, "*");
        vol.setOption(OPTION_NFS_DISABLE, OPTION_VALUE_OFF);
        existingVolumes.add(vol);
        return vol;
    }

    private GlusterVolumeEntity createVolume(String volName, Guid id) {
        GlusterVolumeEntity vol = new GlusterVolumeEntity();
        vol.setId(id);
        vol.setName(volName);
        vol.setClusterId(CLUSTER_ID);
        vol.setStatus(GlusterStatus.UP);
        return vol;
    }

    private GlusterBrickEntity createBrick(Guid existingVolDistId, VDS server, String brickDir) {
        GlusterBrickEntity brick = new GlusterBrickEntity();
        brick.setVolumeId(existingVolDistId);
        brick.setServerId(server.getId());
        brick.setServerName(server.getHostName());
        brick.setBrickDirectory(brickDir);
        brick.setStatus(GlusterStatus.UP);
        return brick;
    }

    private GlusterServer getGlusterServer() {
        return new GlusterServer(SERVER_ID_1, GLUSTER_SERVER_UUID_1);
    }

    @SuppressWarnings("unchecked")
    private void setupMocks() {
        mockDaos();

        doReturn(existingServer1).when(glusterUtil).getUpServer(any());
        doReturn(existingServer1).when(glusterUtil).getRandomUpServer(any());

        doReturn(getFetchedServersList()).when(glusterManager).fetchServers(any());
        doReturn(getFetchedVolumesList()).when(glusterManager).fetchVolumes(any());
        doReturn(getVolumeAdvancedDetails(existingDistVol)).when(glusterManager)
                .getVolumeAdvancedDetails(existingServer1, CLUSTER_ID, existingDistVol.getName());
        doReturn(getVolumeAdvancedDetails(existingReplVol)).when(glusterManager)
                .getVolumeAdvancedDetails(existingServer1, CLUSTER_ID, existingReplVol.getName());
        doReturn(new VDSReturnValue()).when(glusterManager).runVdsCommand(eq(VDSCommandType.RemoveVds),
                argThat(isRemovedServer()));

        doNothing().when(glusterManager).acquireLock(CLUSTER_ID);
        doNothing().when(glusterManager).releaseLock(CLUSTER_ID);
    }

    private ArgumentMatcher<RemoveVdsVDSCommandParameters> isRemovedServer() {
        return argument -> argument.getVdsId().equals(SERVER_ID_3);
    }

    private void verifyMocksForLightWeight() {
        InOrder inOrder =
                inOrder(clusterDao,
                        vdsDao,
                        glusterUtil,
                        glusterManager,
                        vdsStatisticsDao,
                        vdsDynamicDao,
                        vdsStaticDao,
                        volumeDao,
                        brickDao,
                        optionDao);

        // all clusters fetched from db
        inOrder.verify(clusterDao, times(1)).getAll();

        // get servers of the cluster from db
        inOrder.verify(vdsDao, times(1)).getAllForCluster(CLUSTER_ID);

        // get the UP server from cluster
        inOrder.verify(glusterUtil, times(1)).getUpServer(CLUSTER_ID);

        // acquire lock on the cluster
        inOrder.verify(glusterManager, times(1)).acquireLock(CLUSTER_ID);

        // servers are fetched from glusterfs
        inOrder.verify(glusterManager, times(1)).fetchServers(existingServer1);

        // detached server SERVER_ID_3 is deleted from DB
        inOrder.verify(vdsStatisticsDao, times(1)).remove(SERVER_ID_3);
        inOrder.verify(vdsDynamicDao, times(1)).remove(SERVER_ID_3);
        inOrder.verify(vdsStaticDao, times(1)).remove(SERVER_ID_3);

        // detached server SERVER_ID_3 is removed from resource manager
        inOrder.verify(glusterManager, times(1)).runVdsCommand(eq(VDSCommandType.RemoveVds),
                any());

        // release lock on the cluster
        inOrder.verify(glusterManager, times(1)).releaseLock(CLUSTER_ID);

        // acquire lock on the cluster for next operation (refresh volumes)
        inOrder.verify(glusterManager, times(1)).acquireLock(CLUSTER_ID);

        // volumes are fetched from glusterfs
        inOrder.verify(glusterManager, times(1)).fetchVolumes(any());

        // get volumes by cluster id to identify those that need to be removed
        inOrder.verify(volumeDao, times(1)).getByClusterId(CLUSTER_ID);
        // remove deleted volumes
        inOrder.verify(volumeDao, times(1)).removeAll(argThat(areRemovedVolumes()));

        // create new volume
        inOrder.verify(volumeDao, times(1)).save(newVolume);

        // remove detached bricks
        inOrder.verify(brickDao, times(1)).removeAll(argThat(containsRemovedBricks()));
        // add new bricks
        inOrder.verify(brickDao, times(2)).save(argThat(isAddedBrick()));

        // add new options
        inOrder.verify(optionDao, times(1)).saveAll(argThat(areAddedOptions()));

        // update modified options
        Map<String, GlusterVolumeOptionEntity> existingOptions = new HashMap<>();
        existingReplVol.getOption(OPTION_NFS_DISABLE).setValue(OPTION_VALUE_ON);
        existingOptions.put(OPTION_NFS_DISABLE, existingReplVol.getOption(OPTION_NFS_DISABLE));
        List<GlusterVolumeOptionEntity> list = new ArrayList<>(existingOptions.values());
        Collections.sort(list);
        inOrder.verify(optionDao, times(1)).updateAll("UpdateGlusterVolumeOption", list);

        // delete removed options
        inOrder.verify(optionDao, times(1)).removeAll(argThat(areRemovedOptions()));

        // release lock on the cluster
        inOrder.verify(glusterManager, times(1)).releaseLock(CLUSTER_ID);
    }

    private void mockDaos() {
        doReturn(Collections.singletonList(existingCluster)).when(clusterDao).getAll();
        doReturn(existingCluster).when(clusterDao).get(any());
        doReturn(existingServers).when(vdsDao).getAllForCluster(CLUSTER_ID);
        doReturn(existingReplVol).when(volumeDao).getById(EXISTING_VOL_REPL_ID);
        doReturn(existingVolumes).when(volumeDao).getByClusterId(CLUSTER_ID);
    }

    private ArgumentMatcher<Collection<Guid>> areRemovedVolumes() {
        return removedVolumeIds -> removedVolumeIds.size() == 1 && removedVolumeIds.contains(EXISTING_VOL_DIST_ID);
    }

    private ArgumentMatcher<Collection<Guid>> areRemovedOptions() {
        return optionsToRemove -> optionsToRemove.size() == 1 &&
                optionsToRemove.contains(existingReplVol.getOption(OPTION_AUTH_ALLOW).getId());
    }

    private ArgumentMatcher<Collection<GlusterVolumeOptionEntity>> areAddedOptions() {
        return optionsToAdd -> {
            // set the added option to volume
            GlusterVolumeOptionEntity option = optionsToAdd.iterator().next();
            existingReplVol.setOption(option);
            return optionsToAdd.size() == 1 && option.getKey().equals(OPTION_AUTH_REJECT);
        };
    }

    private ArgumentMatcher<Collection<Guid>> containsRemovedBricks() {
        return ids -> ids.size() == removedBrickIds.size() && removedBrickIds.containsAll(ids);
    }

    private ArgumentMatcher<GlusterBrickEntity> isAddedBrick() {
        return argument -> addedBrickIds.contains(argument.getId());
    }

    private GlusterVolumeAdvancedDetails getVolumeAdvancedDetails(GlusterVolumeEntity volume) {
        GlusterVolumeAdvancedDetails volDetails = new GlusterVolumeAdvancedDetails();
        GlusterVolumeSizeInfo capacityInfo = new GlusterVolumeSizeInfo();
        capacityInfo.setVolumeId(volume.getId());
        capacityInfo.setTotalSize(600000L);
        capacityInfo.setFreeSize(200000L);
        capacityInfo.setUsedSize(400000L);
        volDetails.setCapacityInfo(capacityInfo);

        List<BrickDetails> brickDetailsList = new ArrayList<>();
        for (GlusterBrickEntity brick : volume.getBricks()) {
            BrickDetails brickDetails = new BrickDetails();
            BrickProperties properties = new BrickProperties();
            properties.setBrickId(brick.getId());
            brickDetails.setBrickProperties(properties);
            properties.setStatus(brick.getStatus());
            if (volume == existingReplVol) {
                if (brick.getServerId().equals(SERVER_ID_1)
                        && (brick.getBrickDirectory().equals(REPL_BRICK_R1D1) || brick.getBrickDirectory()
                                .equals(REPL_BRICK_R2D1))) {
                    properties.setStatus(GlusterStatus.DOWN);
                    bricksWithChangedStatus.add(brick);
                }
            }
            brickDetailsList.add(brickDetails);
        }
        volDetails.setBrickDetails(brickDetailsList);

        return volDetails;
    }

    /**
     * Returns the list of volumes as if they were fetched from glusterfs. Changes from existing volumes are:<br>
     * - existingDistVol not fetched (means it was removed from gluster cli, and should be removed from db<br>
     * - option 'auth.allow' removed from the existingReplVol<br>
     * - new option 'auth.reject' added to existingReplVol<br>
     * - value of option 'nfs.disable' changed from 'off' ot 'on' in existingReplVol<br>
     * - new volume test-new-vol fetched from gluster (means it was added from gluster cli, and should be added to db<br>
     */
    private Map<Guid, GlusterVolumeEntity> getFetchedVolumesList() {
        Map<Guid, GlusterVolumeEntity> volumes = new HashMap<>();

        GlusterVolumeEntity fetchedReplVol = createReplVol();
        fetchedReplVol.removeOption(OPTION_AUTH_ALLOW); // option removed
        fetchedReplVol.setOption(OPTION_AUTH_REJECT, AUTH_REJECT_IP); // added
        fetchedReplVol.setOption(OPTION_NFS_DISABLE, OPTION_VALUE_ON); // changed

        // brick changes
        removedBrickIds.add(GlusterCoreUtil.findBrick(existingReplVol.getBricks(), SERVER_ID_1, REPL_BRICK_R1D1)
                .getId());
        removedBrickIds.add(GlusterCoreUtil.findBrick(existingReplVol.getBricks(), SERVER_ID_1, REPL_BRICK_R2D1)
                .getId());

        GlusterBrickEntity brickToReplace =
                GlusterCoreUtil.findBrick(fetchedReplVol.getBricks(), SERVER_ID_1, REPL_BRICK_R1D1);
        replaceBrick(brickToReplace,
                SERVER_ID_1,
                REPL_BRICK_R1D1_NEW);

        brickToReplace = GlusterCoreUtil.findBrick(fetchedReplVol.getBricks(), SERVER_ID_1, REPL_BRICK_R2D1);
        replaceBrick(brickToReplace,
                SERVER_ID_1,
                REPL_BRICK_R2D1_NEW);
        volumes.put(fetchedReplVol.getId(), fetchedReplVol);

        // add a new volume
        newVolume = getNewVolume();
        volumes.put(newVolume.getId(), newVolume);

        return volumes;
    }

    private void replaceBrick(GlusterBrickEntity brick, Guid newServerId, String newBrickDir) {
        brick.setId(Guid.newGuid());
        brick.setServerId(newServerId);
        brick.setBrickDirectory(newBrickDir);
        addedBrickIds.add(brick.getId());
    }

    private List<GlusterServerInfo> getFetchedServersList() {
        List<GlusterServerInfo> servers = new ArrayList<>();
        servers.add(new GlusterServerInfo(SERVER_ID_1, SERVER_NAME_1, PeerStatus.CONNECTED));
        servers.add(new GlusterServerInfo(SERVER_ID_2, SERVER_NAME_2, PeerStatus.CONNECTED));
        return servers;
    }

    @Test
    public void testRefreshLightWeight() {
        createCluster();
        setupMocks();
        doReturn(getGlusterServer()).when(glusterServerDao).getByServerId(any());

        glusterManager.refreshLightWeightData();
        verifyMocksForLightWeight();
    }

    @Test
    public void testRefreshHeavyWeight() {
        createCluster();
        setupMocks();
        glusterManager.refreshHeavyWeightData();
        verifyMocksForHeavyWeight();
    }

    private void verifyMocksForHeavyWeight() {
        InOrder inOrder = inOrder(clusterDao, glusterUtil, volumeDao, glusterManager, brickDao);

        // all clusters fetched from db
        inOrder.verify(clusterDao, times(1)).getAll();

        VerificationMode mode = times(1);

        // get the UP server from cluster
        inOrder.verify(glusterUtil, mode).getRandomUpServer(CLUSTER_ID);

        // get volumes of the cluster
        inOrder.verify(volumeDao, mode).getByClusterId(CLUSTER_ID);

        // acquire lock on the cluster
        inOrder.verify(glusterManager, mode).acquireLock(CLUSTER_ID);

        // acquire lock on the cluster for repl volume
        inOrder.verify(glusterManager, mode).acquireLock(CLUSTER_ID);

        // release lock on the cluster
        inOrder.verify(glusterManager, mode).releaseLock(CLUSTER_ID);

        // release lock on the cluster
        inOrder.verify(glusterManager, mode).releaseLock(CLUSTER_ID);
    }

    /**
     * Matches following properties: <br>
     * - is a list <br>
     * - has two elements (bricks) <br>
     * - both have status DOWN <br>
     * - these are the same whose status was changed <br>
     */
    private ArgumentMatcher<List<GlusterBrickEntity>> hasBricksWithChangedStatus() {
        return bricksToUpdate -> {
            if (bricksToUpdate.size() != 2) {
                return false;
            }

            for (GlusterBrickEntity brick : bricksToUpdate) {
                if (brick.isOnline()) {
                    return false;
                }
                if (!GlusterCoreUtil.containsBrick(bricksWithChangedStatus, brick)) {
                    return false;
                }
            }
            return true;
        };
    }

    private GlusterVolumeEntity getNewVolume() {
        GlusterVolumeEntity volume = new GlusterVolumeEntity();
        volume.setName(NEW_VOL_NAME);
        volume.setClusterId(CLUSTER_ID);
        volume.setId(NEW_VOL_ID);
        volume.setVolumeType(GlusterVolumeType.DISTRIBUTE);
        volume.addTransportType(TransportType.TCP);
        volume.setReplicaCount(0);
        volume.setStripeCount(0);
        volume.setStatus(GlusterStatus.UP);
        volume.setOption("auth.allow", "*");
        volume.addAccessProtocol(AccessProtocol.GLUSTER);
        volume.addAccessProtocol(AccessProtocol.NFS);

        GlusterBrickEntity brick = new GlusterBrickEntity();
        brick.setVolumeId(NEW_VOL_ID);
        brick.setServerId(existingServer1.getId());
        brick.setServerName(existingServer1.getHostName());
        brick.setBrickDirectory("/export/testvol1");
        brick.setStatus(GlusterStatus.UP);
        brick.setBrickOrder(0);
        volume.addBrick(brick);

        return volume;
    }
}

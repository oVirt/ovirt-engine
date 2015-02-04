package org.ovirt.engine.core.bll.gluster;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.bll.CanDoActionTestUtils;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.validator.gluster.GlusterVolumeValidator;
import org.ovirt.engine.core.common.action.gluster.GlusterVolumeBricksActionParameters;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.common.businessentities.gluster.AccessProtocol;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeType;
import org.ovirt.engine.core.common.businessentities.gluster.TransportType;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dao.VdsGroupDAO;
import org.ovirt.engine.core.dao.VdsStaticDAO;
import org.ovirt.engine.core.dao.gluster.GlusterBrickDao;
import org.ovirt.engine.core.dao.gluster.GlusterVolumeDao;
import org.ovirt.engine.core.dao.network.InterfaceDao;
import org.ovirt.engine.core.dao.network.NetworkDao;

@RunWith(MockitoJUnitRunner.class)
public class AddBricksToGlusterVolumeCommandTest {

    @Mock
    GlusterVolumeDao volumeDao;

    @Mock
    VdsStaticDAO vdsStaticDao;

    @Mock
    GlusterBrickDao brickDao;

    @Mock
    VdsGroupDAO vdsGroupDao;

    @Mock
    GlusterVolumeValidator validator;

    @Mock
    NetworkDao networkDao;

    @Mock
    InterfaceDao interfaceDao;

    private final String serverName = "myhost";

    private final Guid clusterId = new Guid("c0dd8ca3-95dd-44ad-a88a-440a6e3d8106");

    private final Guid serverId = new Guid("d7f10a21-bbf2-4ffd-aab6-4da0b3b2ccec");

    private final Guid glusterIfaceId = new Guid("bbbb0a21-bbf2-4ffd-aab6-4da0b3b2ccec");

    private final Guid volumeId1 = new Guid("8bc6f108-c0ef-43ab-ba20-ec41107220f5");

    private final Guid volumeId2 = new Guid("b2cb2f73-fab3-4a42-93f0-d5e4c069a43e");

    private static final String BRICK_DIRECTORY = "/tmp/s1";

    private static final String GLUSTER_NW = "gluster-net";

    private static final String SERVER_ADDRESS = "10.70.8.8";

    /**
     * The command under test.
     */
    private AddBricksToGlusterVolumeCommand cmd;

    private AddBricksToGlusterVolumeCommand createTestCommand(Guid volumeId,
            List<GlusterBrickEntity> bricks,
            int replicaCount,
            int stripeCount,
            boolean force) {
        return new AddBricksToGlusterVolumeCommand(new GlusterVolumeBricksActionParameters(volumeId,
                bricks,
                replicaCount,
                stripeCount,
                force));
    }

    private List<GlusterBrickEntity> getBricks(Guid volumeId, int max) {
        return getBricks(volumeId, max, false);
    }

    private List<GlusterBrickEntity> getBricks(Guid volumeId, int max, boolean withDuplicates) {
        List<GlusterBrickEntity> bricks = new ArrayList<GlusterBrickEntity>();
        GlusterBrickEntity brick = null;
        for (Integer i = 0; i < max; i++) {
            brick = new GlusterBrickEntity();
            brick.setVolumeId(volumeId);
            brick.setServerId(serverId);
            brick.setServerName(serverName);
            brick.setBrickDirectory("/tmp/s" + i.toString());
            brick.setStatus(GlusterStatus.UP);
            bricks.add(brick);
        }

        if (max > 0 && withDuplicates) {
            bricks.add(brick);
        }
        return bricks;
    }

    private GlusterBrickEntity getBrick(Guid volumeId) {
        GlusterBrickEntity brick = new GlusterBrickEntity();
        brick.setVolumeId(volumeId);
        brick.setServerId(serverId);
        brick.setServerName(serverName);
        brick.setBrickDirectory(BRICK_DIRECTORY);
        brick.setStatus(GlusterStatus.UP);
        return brick;
    }

    private VDS getVds(VDSStatus status) {
        VDS vds = new VDS();
        vds.setId(Guid.newGuid());
        vds.setVdsName("gfs1");
        vds.setVdsGroupId(clusterId);
        vds.setStatus(status);
        return vds;
    }

    private void prepareMocks(AddBricksToGlusterVolumeCommand command) {
        doReturn(volumeDao).when(command).getGlusterVolumeDao();
        doReturn(vdsStaticDao).when(command).getVdsStaticDAO();
        doReturn(brickDao).when(command).getGlusterBrickDao();
        doReturn(validator).when(command).createVolumeValidator();
        doReturn(networkDao).when(command).getNetworkDAO();
        doReturn(interfaceDao).when(command).getInterfaceDAO();

        doReturn(getVds(VDSStatus.Up)).when(command).getUpServer();
        doReturn(getSingleBrickVolume(volumeId1)).when(volumeDao).getById(volumeId1);
        doReturn(getMultiBrickVolume(volumeId2)).when(volumeDao).getById(volumeId2);
        doReturn(getBrick(volumeId1)).when(brickDao).getBrickByServerIdAndDirectory(serverId, BRICK_DIRECTORY);
        doReturn(null).when(volumeDao).getById(null);
        doReturn(getVdsStatic()).when(vdsStaticDao).get(serverId);
        doReturn(getVDsGroup()).when(command).getVdsGroup();
        doReturn(ValidationResult.VALID).when(validator).isForceCreateVolumeAllowed(Version.v3_1, false);
        doReturn(new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_GLUSTER_VOLUME_ADD_BRICK_FORCE_NOT_SUPPORTED)).when(validator)
                .isForceCreateVolumeAllowed(Version.v3_1, true);
    }

    private void prepareInterfaceMocks(AddBricksToGlusterVolumeCommand command) {
        doReturn(getNetworks()).when(networkDao).getAllForCluster(any(Guid.class));
        doReturn(getNetworkInterfaces()).when(interfaceDao).getAllInterfacesForVds(serverId);
    }

    private List<Network> getNetworks() {
        List<Network> networks = new ArrayList<>();
        Network nw = new Network();
        nw.setName(GLUSTER_NW);
        NetworkCluster nc = new NetworkCluster();
        nc.setGluster(true);
        nw.setCluster(nc);
        networks.add(nw);
        return networks;
    }

    private List<VdsNetworkInterface> getNetworkInterfaces() {
        List<VdsNetworkInterface> ifaces = new ArrayList<>();
        VdsNetworkInterface iface = new VdsNetworkInterface();
        iface.setNetworkName(GLUSTER_NW);
        iface.setId(Guid.newGuid());
        iface.setAddress(SERVER_ADDRESS);
        ifaces.add(iface);
        return ifaces;
    }

    private VDSGroup getVDsGroup() {
        VDSGroup vdsGroup = new VDSGroup();
        vdsGroup.setId(clusterId);
        vdsGroup.setVirtService(false);
        vdsGroup.setGlusterService(true);
        vdsGroup.setCompatibilityVersion(Version.v3_1);
        return vdsGroup;
    }

    private VdsStatic getVdsStatic() {
        VdsStatic vds = new VdsStatic();
        vds.setId(serverId);
        vds.setVdsGroupId(clusterId);
        vds.setHostName(serverName);
        return vds;
    }

    private GlusterVolumeEntity getSingleBrickVolume(Guid volumeId) {
        GlusterVolumeEntity volume = getGlusterVolume(volumeId);
        volume.setStatus(GlusterStatus.UP);
        volume.setBricks(getBricks(volumeId, 1));
        volume.setClusterId(clusterId);
        return volume;
    }

    private GlusterVolumeEntity getMultiBrickVolume(Guid volumeId) {
        GlusterVolumeEntity volume = getGlusterVolume(volumeId);
        volume.setStatus(GlusterStatus.UP);
        volume.setBricks(getBricks(volumeId, 2));
        volume.setClusterId(clusterId);
        return volume;
    }

    private GlusterVolumeEntity getGlusterVolume(Guid id) {
        GlusterVolumeEntity volumeEntity = new GlusterVolumeEntity();
        volumeEntity.setId(id);
        volumeEntity.setName("test-vol");
        volumeEntity.addAccessProtocol(AccessProtocol.GLUSTER);
        volumeEntity.addTransportType(TransportType.TCP);
        volumeEntity.setVolumeType(GlusterVolumeType.DISTRIBUTE);
        volumeEntity.setClusterId(clusterId);
        return volumeEntity;
    }

    @Test
    public void canDoActionSucceeds() {
        cmd = spy(createTestCommand(volumeId2, getBricks(volumeId2, 1), 2, 0, false));
        prepareMocks(cmd);
        assertTrue(cmd.canDoAction());
    }

    @Test
    public void canDoActionFails() {
        cmd = spy(createTestCommand(volumeId1, getBricks(volumeId1, 2), 0, 4, false));
        prepareMocks(cmd);
        assertFalse(cmd.canDoAction());
    }

    @Test
    public void canDoActionFailsWithForceNotSupported() {
        cmd = spy(createTestCommand(volumeId1, getBricks(volumeId1, 2), 0, 4, true));
        prepareMocks(cmd);
        CanDoActionTestUtils.runAndAssertCanDoActionFailure(cmd,
                VdcBllMessages.ACTION_TYPE_FAILED_GLUSTER_VOLUME_ADD_BRICK_FORCE_NOT_SUPPORTED);
    }

    @Test
    public void canDoActionFailsWithDuplicateBricks() {
        cmd = spy(createTestCommand(volumeId2, getBricks(volumeId2, 1, true), 2, 0, false));
        prepareMocks(cmd);
        assertFalse(cmd.canDoAction());
    }

    @Test
    public void canDoActionFailsDiffInterface() {
        cmd = spy(createTestCommand(volumeId1, getBricks(volumeId1, 2), 0, 4, false));
        prepareMocks(cmd);
        prepareInterfaceMocks(cmd);
        assertFalse(cmd.canDoAction());
    }

    @Test
    public void canDoActionFailsOnNull() {
        cmd = spy(createTestCommand(null, null, 0, 0, false));
        prepareMocks(cmd);
        assertFalse(cmd.canDoAction());
    }

}

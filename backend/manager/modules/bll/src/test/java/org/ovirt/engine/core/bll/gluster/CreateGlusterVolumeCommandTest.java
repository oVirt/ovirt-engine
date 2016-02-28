package org.ovirt.engine.core.bll.gluster;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.ovirt.engine.core.bll.BaseCommandTest;
import org.ovirt.engine.core.bll.ValidateTestUtils;
import org.ovirt.engine.core.common.action.gluster.CreateGlusterVolumeParameters;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeType;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.ClusterDao;
import org.ovirt.engine.core.dao.VdsStaticDao;
import org.ovirt.engine.core.dao.gluster.GlusterBrickDao;
import org.ovirt.engine.core.dao.gluster.GlusterVolumeDao;
import org.ovirt.engine.core.dao.network.InterfaceDao;
import org.ovirt.engine.core.dao.network.NetworkDao;

public class CreateGlusterVolumeCommandTest extends BaseCommandTest {

    @Mock
    GlusterVolumeDao volumeDao;

    @Mock
    GlusterBrickDao brickDao;

    @Mock
    VdsStaticDao vdsStaticDao;

    @Mock
    ClusterDao clusterDao;

    @Mock
    NetworkDao networkDao;

    @Mock
    InterfaceDao interfaceDao;

    private static final String serverName = "myhost";

    private final Guid clusterId = new Guid("c0dd8ca3-95dd-44ad-a88a-440a6e3d8106");

    private final Guid serverId = new Guid("d7f10a21-bbf2-4ffd-aab6-4da0b3b2ccec");

    private CreateGlusterVolumeCommand cmd;

    private CreateGlusterVolumeCommand createTestCommand(GlusterVolumeEntity volumeEntity) {
        CreateGlusterVolumeParameters parameters = new CreateGlusterVolumeParameters(volumeEntity);
        return new CreateGlusterVolumeCommand(parameters, null);
    }

    private VDS getVds(VDSStatus status) {
        VDS vds = new VDS();
        vds.setId(Guid.newGuid());
        vds.setVdsName("gfs1");
        vds.setClusterId(clusterId);
        vds.setStatus(status);
        return vds;
    }

    private VdsStatic getVdsStatic() {
        VdsStatic vds = new VdsStatic();
        vds.setClusterId(clusterId);
        vds.setHostName(serverName);
        return vds;
    }

    private Cluster getCluster(boolean glusterEnabled) {
        Cluster cluster = new Cluster();
        cluster.setId(clusterId);
        cluster.setVirtService(false);
        cluster.setGlusterService(glusterEnabled);
        return cluster;
    }

    private void prepareMocks(CreateGlusterVolumeCommand command) {
        doReturn(clusterDao).when(command).getClusterDao();
        doReturn(volumeDao).when(command).getGlusterVolumeDao();
        doReturn(vdsStaticDao).when(command).getVdsStaticDao();
        doReturn(brickDao).when(command).getGlusterBrickDao();
        doReturn(networkDao).when(command).getNetworkDao();
        doReturn(interfaceDao).when(command).getInterfaceDao();

        doReturn(getVds(VDSStatus.Up)).when(command).getUpServer();
        doReturn(getVdsStatic()).when(vdsStaticDao).get(serverId);
        doReturn(getCluster(true)).when(clusterDao).get(Mockito.any(Guid.class));
    }

    private GlusterVolumeEntity getVolume(int brickCount, boolean withDuplicateBricks) {
        GlusterVolumeEntity volumeEntity = new GlusterVolumeEntity();
        volumeEntity.setId(Guid.newGuid());
        volumeEntity.setClusterId(clusterId);
        volumeEntity.setName("vol1");
        volumeEntity.setVolumeType(GlusterVolumeType.DISTRIBUTE);
        volumeEntity.setBricks(getBricks(volumeEntity.getId(), brickCount, withDuplicateBricks));
        return volumeEntity;
    }

    private List<GlusterBrickEntity> getBricks(Guid volumeId, int max, boolean withDuplicates) {
        List<GlusterBrickEntity> bricks = new ArrayList<>();
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

    @Test
    public void validateSucceeds() {
        cmd = spy(createTestCommand(getVolume(2, false)));
        prepareMocks(cmd);
        assertTrue(cmd.validate());
    }

    @Test
    public void validateFailsWithClusterDoesNotSupportGluster() {
        cmd = spy(createTestCommand(getVolume(2, false)));
        prepareMocks(cmd);
        doReturn(getCluster(false)).when(clusterDao).get(Mockito.any(Guid.class));

        ValidateTestUtils.runAndAssertValidateFailure(cmd,
                EngineMessage.ACTION_TYPE_FAILED_CLUSTER_DOES_NOT_SUPPORT_GLUSTER);
    }

    @Test
    public void validateFailsWithDuplicateVolumeName() {
        cmd = spy(createTestCommand(getVolume(2, false)));
        prepareMocks(cmd);
        doReturn(getVolume(2, false)).when(volumeDao).getByName(clusterId, "vol1");

        ValidateTestUtils.runAndAssertValidateFailure(cmd,
                EngineMessage.ACTION_TYPE_FAILED_GLUSTER_VOLUME_NAME_ALREADY_EXISTS);
    }

    @Test
    public void validateFailsWithEmptyBricks() {
        cmd = spy(createTestCommand(getVolume(0, false)));
        prepareMocks(cmd);

        ValidateTestUtils.runAndAssertValidateFailure(cmd,
                EngineMessage.ACTION_TYPE_FAILED_BRICKS_REQUIRED);
    }

    @Test
    public void validateFailsWithDuplicateBricks() {
        cmd = spy(createTestCommand(getVolume(2, true)));
        prepareMocks(cmd);

        ValidateTestUtils.runAndAssertValidateFailure(cmd,
                EngineMessage.ACTION_TYPE_FAILED_DUPLICATE_BRICKS);
    }
}

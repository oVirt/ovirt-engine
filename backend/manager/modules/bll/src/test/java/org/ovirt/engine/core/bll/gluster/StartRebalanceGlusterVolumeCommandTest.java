package org.ovirt.engine.core.bll.gluster;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.ovirt.engine.core.utils.MockConfigRule.mockConfig;

import java.util.ArrayList;
import java.util.List;

import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.common.action.gluster.GlusterVolumeRebalanceParameters;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.gluster.AccessProtocol;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeType;
import org.ovirt.engine.core.common.businessentities.gluster.TransportType;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dao.gluster.GlusterVolumeDao;
import org.ovirt.engine.core.utils.MockConfigRule;

@RunWith(MockitoJUnitRunner.class)
public class StartRebalanceGlusterVolumeCommandTest {
    private static final Version SUPPORTED_VERSION = new Version(3, 4);
    private static final Version UNSUPPORTED_VERSION = new Version(3, 3);

    @Mock
    GlusterVolumeDao volumeDao;

    private final Guid volumeId1 = new Guid("8bc6f108-c0ef-43ab-ba20-ec41107220f5");
    private final Guid volumeId2 = new Guid("b2cb2f73-fab3-4a42-93f0-d5e4c069a43e");
    private final Guid volumeId3 = new Guid("000000000000-0000-0000-0000-00000003");
    private final Guid volumeId4 = new Guid("000000000000-0000-0000-0000-00000004");
    private final Guid CLUSTER_ID = new Guid("b399944a-81ab-4ec5-8266-e19ba7c3c9d1");

    @ClassRule
    public static MockConfigRule mcr = new MockConfigRule(
            mockConfig(ConfigValues.GlusterAsyncTasksSupport, UNSUPPORTED_VERSION.getValue(), false),
            mockConfig(ConfigValues.GlusterAsyncTasksSupport, SUPPORTED_VERSION.getValue(), true));

    @Mock
    protected VDSGroup vdsGroup;

    /**
     * The command under test.
     */
    private StartRebalanceGlusterVolumeCommand cmd;

    private void prepareMocks(StartRebalanceGlusterVolumeCommand command) {
        doReturn(volumeDao).when(command).getGlusterVolumeDao();
        doReturn(getVds(VDSStatus.Up)).when(command).getUpServer();
        doReturn(getDistributedVolume(volumeId1)).when(volumeDao).getById(volumeId1);
        doReturn(getDistributedVolume(volumeId2)).when(volumeDao).getById(volumeId2);
        doReturn(getReplicatedVolume(volumeId3, 2)).when(volumeDao).getById(volumeId3);
        doReturn(getReplicatedVolume(volumeId4, 4)).when(volumeDao).getById(volumeId4);
        doReturn(null).when(volumeDao).getById(null);
        doReturn(SUPPORTED_VERSION).when(vdsGroup).getcompatibility_version();
        doReturn(vdsGroup).when(command).getVdsGroup();
    }

    private VDS getVds(VDSStatus status) {
        VDS vds = new VDS();
        vds.setId(Guid.newGuid());
        vds.setVdsName("gfs1");
        vds.setVdsGroupId(CLUSTER_ID);
        vds.setStatus(status);
        return vds;
    }

    private GlusterVolumeEntity getDistributedVolume(Guid volumeId) {
        GlusterVolumeEntity volume = getVolume(volumeId);
        volume.setStatus((volumeId == volumeId1) ? GlusterStatus.UP : GlusterStatus.DOWN);
        volume.setBricks(getBricks(volumeId, 2));
        volume.setVolumeType(GlusterVolumeType.DISTRIBUTE);
        volume.setClusterId(CLUSTER_ID);
        return volume;
    }

    private GlusterVolumeEntity getReplicatedVolume(Guid volumeId, int brickCount) {
        GlusterVolumeEntity volume = getVolume(volumeId);
        volume.setStatus(GlusterStatus.UP);
        volume.setBricks(getBricks(volumeId, brickCount));
        volume.setVolumeType(GlusterVolumeType.REPLICATE);
        volume.setReplicaCount(brickCount);
        volume.setClusterId(CLUSTER_ID);
        return volume;
    }

    private GlusterVolumeEntity getVolume(Guid id) {
        GlusterVolumeEntity volumeEntity = new GlusterVolumeEntity();
        volumeEntity.setId(id);
        volumeEntity.setName("test-vol");
        volumeEntity.addAccessProtocol(AccessProtocol.GLUSTER);
        volumeEntity.addTransportType(TransportType.TCP);
        return volumeEntity;
    }

    private List<GlusterBrickEntity> getBricks(Guid volumeId, int n) {
        List<GlusterBrickEntity> bricks = new ArrayList<GlusterBrickEntity>();
        GlusterBrickEntity brick;
        for (Integer i = 0; i < n; i++) {
            brick = new GlusterBrickEntity();
            brick.setVolumeId(volumeId);
            brick.setBrickDirectory("/tmp/test-vol" + i.toString());
            brick.setStatus(GlusterStatus.UP);
            bricks.add(brick);
        }
        return bricks;
    }

    private StartRebalanceGlusterVolumeCommand createTestCommand(Guid volumeId) {
        return new StartRebalanceGlusterVolumeCommand(new GlusterVolumeRebalanceParameters(volumeId,
                false,
                false));
    }

    @Test
    public void canDoActionSucceedsOnUpVolume() {
        cmd = spy(createTestCommand(volumeId1));
        prepareMocks(cmd);
        assertTrue(cmd.canDoAction());
    }

    @Test
    public void canDoActionSucceedsOnDistributedVolume() {
        cmd = spy(createTestCommand(volumeId4));
        prepareMocks(cmd);
        assertFalse(cmd.canDoAction());
    }

    @Test
    public void canDoActionFailesOnDownVolume() {
        cmd = spy(createTestCommand(volumeId2));
        prepareMocks(cmd);
        assertFalse(cmd.canDoAction());
    }

    @Test
    public void canDoActionFailsOnNoDistribution() {
        cmd = spy(createTestCommand(volumeId3));
        prepareMocks(cmd);
        assertFalse(cmd.canDoAction());
    }

    @Test
    public void canDoActionFailsOnNull() {
        cmd = spy(createTestCommand(null));
        prepareMocks(cmd);
        assertFalse(cmd.canDoAction());
    }

    @Test
    public void canDoActionFailsOnCompat() {
        cmd = spy(createTestCommand(volumeId1));
        prepareMocks(cmd);
        doReturn(UNSUPPORTED_VERSION).when(vdsGroup).getcompatibility_version();
        assertFalse(cmd.canDoAction());
        assertTrue(cmd.getReturnValue()
                .getCanDoActionMessages()
                .contains(VdcBllMessages.GLUSTER_TASKS_NOT_SUPPORTED_FOR_CLUSTER_LEVEL.toString()));
    }

}

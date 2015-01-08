package org.ovirt.engine.core.bll.gluster;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.bll.CanDoActionTestUtils;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.validator.gluster.GlusterVolumeValidator;
import org.ovirt.engine.core.common.action.gluster.CreateGlusterVolumeParameters;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeType;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dao.VdsGroupDAO;
import org.ovirt.engine.core.dao.VdsStaticDAO;
import org.ovirt.engine.core.dao.gluster.GlusterBrickDao;
import org.ovirt.engine.core.dao.gluster.GlusterVolumeDao;

@RunWith(MockitoJUnitRunner.class)
public class CreateGlusterVolumeCommandTest {

    @Mock
    GlusterVolumeDao volumeDao;

    @Mock
    GlusterBrickDao brickDao;

    @Mock
    VdsStaticDAO vdsStaticDao;

    @Mock
    VdsGroupDAO vdsGroupDao;

    @Mock
    GlusterVolumeValidator validator;

    private String serverName = "myhost";

    private Guid clusterId = new Guid("c0dd8ca3-95dd-44ad-a88a-440a6e3d8106");

    private Guid serverId = new Guid("d7f10a21-bbf2-4ffd-aab6-4da0b3b2ccec");

    private CreateGlusterVolumeCommand cmd;

    private CreateGlusterVolumeCommand createTestCommand(GlusterVolumeEntity volumeEntity) {
        return new CreateGlusterVolumeCommand(new CreateGlusterVolumeParameters(volumeEntity));
    }

    private VDS getVds(VDSStatus status) {
        VDS vds = new VDS();
        vds.setId(Guid.newGuid());
        vds.setVdsName("gfs1");
        vds.setVdsGroupId(clusterId);
        vds.setStatus(status);
        return vds;
    }

    private VdsStatic getVdsStatic() {
        VdsStatic vds = new VdsStatic();
        vds.setVdsGroupId(clusterId);
        vds.setHostName(serverName);
        return vds;
    }

    private VDSGroup getVdsGroup(boolean glusterEnabled) {
        VDSGroup vdsGroup = new VDSGroup();
        vdsGroup.setId(clusterId);
        vdsGroup.setVirtService(false);
        vdsGroup.setGlusterService(glusterEnabled);
        vdsGroup.setCompatibilityVersion(Version.v3_1);
        return vdsGroup;
    }

    private void prepareMocks(CreateGlusterVolumeCommand command) {
        doReturn(vdsGroupDao).when(command).getVdsGroupDAO();
        doReturn(volumeDao).when(command).getGlusterVolumeDao();
        doReturn(vdsStaticDao).when(command).getVdsStaticDAO();
        doReturn(brickDao).when(command).getGlusterBrickDao();
        doReturn(validator).when(command).createVolumeValidator();

        doReturn(getVds(VDSStatus.Up)).when(command).getUpServer();
        doReturn(getVdsStatic()).when(vdsStaticDao).get(serverId);
        doReturn(getVdsGroup(true)).when(vdsGroupDao).get(Mockito.any(Guid.class));
        doReturn(ValidationResult.VALID).when(validator).isForceCreateVolumeAllowed(Version.v3_1, false);
        doReturn(new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_GLUSTER_VOLUME_ADD_BRICK_FORCE_NOT_SUPPORTED)).when(validator)
                .isForceCreateVolumeAllowed(Version.v3_1, true);
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

    @Test
    public void canDoActionSucceeds() {
        cmd = spy(createTestCommand(getVolume(2, false)));
        prepareMocks(cmd);
        assertTrue(cmd.canDoAction());
    }

    @Test
    public void canDoActionFailsWithClusterDoesNotSupportGluster() {
        cmd = spy(createTestCommand(getVolume(2, false)));
        prepareMocks(cmd);
        doReturn(getVdsGroup(false)).when(vdsGroupDao).get(Mockito.any(Guid.class));

        CanDoActionTestUtils.runAndAssertCanDoActionFailure(cmd,
                VdcBllMessages.ACTION_TYPE_FAILED_CLUSTER_DOES_NOT_SUPPORT_GLUSTER);
    }

    @Test
    public void canDoActionFailsWithDuplicateVolumeName() {
        cmd = spy(createTestCommand(getVolume(2, false)));
        prepareMocks(cmd);
        doReturn(getVolume(2, false)).when(volumeDao).getByName(clusterId, "vol1");

        CanDoActionTestUtils.runAndAssertCanDoActionFailure(cmd,
                VdcBllMessages.ACTION_TYPE_FAILED_GLUSTER_VOLUME_NAME_ALREADY_EXISTS);
    }

    @Test
    public void canDoActionFailsWithEmptyBricks() {
        cmd = spy(createTestCommand(getVolume(0, false)));
        prepareMocks(cmd);

        CanDoActionTestUtils.runAndAssertCanDoActionFailure(cmd,
                VdcBllMessages.ACTION_TYPE_FAILED_BRICKS_REQUIRED);
    }

    @Test
    public void canDoActionFailsWithForceNotSupported() {
        CreateGlusterVolumeParameters parameters = new CreateGlusterVolumeParameters(getVolume(2, true), true);
        CreateGlusterVolumeCommand command = new CreateGlusterVolumeCommand(parameters);
        cmd = spy(command);
        prepareMocks(cmd);

        CanDoActionTestUtils.runAndAssertCanDoActionFailure(cmd,
                VdcBllMessages.ACTION_TYPE_FAILED_GLUSTER_VOLUME_ADD_BRICK_FORCE_NOT_SUPPORTED);
    }

    @Test
    public void canDoActionFailsWithDuplicateBricks() {
        cmd = spy(createTestCommand(getVolume(2, true)));
        prepareMocks(cmd);

        CanDoActionTestUtils.runAndAssertCanDoActionFailure(cmd,
                VdcBllMessages.ACTION_TYPE_FAILED_DUPLICATE_BRICKS);
    }
}

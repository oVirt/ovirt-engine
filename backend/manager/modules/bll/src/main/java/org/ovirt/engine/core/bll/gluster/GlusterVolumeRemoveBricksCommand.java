package org.ovirt.engine.core.bll.gluster;

import java.util.List;

import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.gluster.GlusterVolumeRemoveBricksParameters;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.gluster.GlusterVolumeRemoveBricksVDSParameters;
import org.ovirt.engine.core.dal.VdcBllMessages;

/**
 * BLL command to Remove Bricks from Gluster volume
 */
@NonTransactiveCommandAttribute
public class GlusterVolumeRemoveBricksCommand extends GlusterVolumeCommandBase<GlusterVolumeRemoveBricksParameters> {
    private static final long serialVersionUID = 1465299601226267507L;

    public GlusterVolumeRemoveBricksCommand(GlusterVolumeRemoveBricksParameters params) {
        super(params);
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__REMOVE);
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__GLUSTER_VOLUME_BRICK);
    }

    @Override
    protected boolean canDoAction() {
        if (!super.canDoAction()) {
            return false;
        }
        if (getParameters().getBricks() == null || getParameters().getBricks().size() == 0) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_BRICKS_REQUIRED);
            return false;
        }
        if (getGlusterVolume().getBricks().size() == 1 ||
                getGlusterVolume().getBricks().size() <= getParameters().getBricks().size()) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_CAN_NOT_REMOVE_ALL_BRICKS_FROM_VOLUME);
            return false;
        }
        return true;
    }

    @Override
    protected void executeCommand() {
        VDSReturnValue returnValue =
                runVdsCommand(
                        VDSCommandType.GlusterVolumeRemoveBricks,
                        new GlusterVolumeRemoveBricksVDSParameters(getUpServer().getId(),
                                getGlusterVolumeName(), getParameters().getBricks()));
        setSucceeded(returnValue.getSucceeded());
        if (getSucceeded()) {
            removeBricksFromVolumeInDb(getGlusterVolume(), getParameters().getBricks());
        } else {
            getReturnValue().getExecuteFailedMessages().add(returnValue.getVdsError().getMessage());
            return;
        }
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        if (getSucceeded()) {
            return AuditLogType.GLUSTER_VOLUME_REMOVE_BRICKS;
        } else {
            return AuditLogType.GLUSTER_VOLUME_REMOVE_BRICKS_FAILED;
        }
    }

    private void removeBricksFromVolumeInDb(GlusterVolumeEntity volume, List<GlusterBrickEntity> brickList) {
        for (GlusterBrickEntity brick : brickList) {
            getGlusterVolumeDao().removeBrickFromVolume(brick);
        }
    }
}

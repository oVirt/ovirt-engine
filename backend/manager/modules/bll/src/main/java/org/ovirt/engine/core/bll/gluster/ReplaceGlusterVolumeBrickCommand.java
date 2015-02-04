package org.ovirt.engine.core.bll.gluster;

import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.gluster.GlusterVolumeReplaceBrickActionParameters;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.gluster.ReplaceGlusterVolumeBrickActionVDSParameters;

/**
 * BLL command to Replace Gluster volume brick
 */
@NonTransactiveCommandAttribute
public class ReplaceGlusterVolumeBrickCommand extends GlusterVolumeCommandBase<GlusterVolumeReplaceBrickActionParameters> {

    public ReplaceGlusterVolumeBrickCommand(GlusterVolumeReplaceBrickActionParameters params) {
        super(params);
    }

    @Override
    protected void setActionMessageParameters() {
        switch (getParameters().getAction()) {
        case START:
            addCanDoActionMessage(VdcBllMessages.VAR__ACTION__START);
            break;
        default:
            break;
        }
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__GLUSTER_BRICK);
    }

    @Override
    protected boolean canDoAction() {
        if (!super.canDoAction()) {
            return false;
        }

        if (!getGlusterVolume().isOnline()) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_GLUSTER_VOLUME_IS_DOWN);
            return false;
        }

        if (getParameters().getExistingBrick() == null || getParameters().getNewBrick() == null) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_BRICKS_REQUIRED);
            return false;
        }

        if (!updateBrickServerAndInterfaceName(getParameters().getExistingBrick(), true)) {
            return false;
        }

        if (!updateBrickServerAndInterfaceName(getParameters().getNewBrick(), true)) {
            return false;
        }

        if (!isValidVolumeBrick(getParameters().getExistingBrick())) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_NOT_A_GLUSTER_VOLUME_BRICK);
            return false;
        }

        return true;
    }

    @Override
    protected void executeCommand() {
        VDSReturnValue returnValue =
                runVdsCommand(
                        VDSCommandType.ReplaceGlusterVolumeBrick,
                        new ReplaceGlusterVolumeBrickActionVDSParameters(upServer.getId(),
                                getGlusterVolumeName(),
                                getParameters().getAction(),
                                getParameters().getExistingBrick().getQualifiedName(),
                                getParameters().getNewBrick().getQualifiedName(),
                                getParameters().isForceAction()));
        if (getSucceeded()) {
            setSucceeded(returnValue.getSucceeded());
        } else {
            handleVdsError(AuditLogType.GLUSTER_VOLUME_OPTION_SET_FAILED, returnValue.getVdsError().getMessage());
            return;
        }
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        switch (getParameters().getAction()) {
        case START:
            if (getSucceeded()) {
                return AuditLogType.GLUSTER_VOLUME_REPLACE_BRICK_START;
            } else {
                return AuditLogType.GLUSTER_VOLUME_REPLACE_BRICK_START_FAILED;
            }
        default:
            return AuditLogType.GLUSTER_VOLUME_REPLACE_BRICK_FAILED;
        }
    }

    private boolean isValidVolumeBrick(GlusterBrickEntity volumeBrick) {
        for (GlusterBrickEntity brick : getGlusterVolume().getBricks()) {
            if (brick.getQualifiedName().equals(volumeBrick.getQualifiedName())) {
                return true;
            }
        }
        return false;
    }

}

package org.ovirt.engine.core.bll.gluster;

import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.gluster.GlusterVolumeReplaceBrickActionParameters;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.gluster.ReplaceGlusterVolumeBrickActionVDSParameters;
import org.ovirt.engine.core.dal.VdcBllMessages;

/**
 * BLL command to Replace Gluster volume brick
 */
@NonTransactiveCommandAttribute
public class ReplaceGlusterVolumeBrickCommand extends GlusterVolumeCommandBase<GlusterVolumeReplaceBrickActionParameters> {

    private static final long serialVersionUID = 7453409042409619674L;

    public ReplaceGlusterVolumeBrickCommand(GlusterVolumeReplaceBrickActionParameters params) {
        super(params);
    }

    @Override
    protected void setActionMessageParameters() {
        switch (getParameters().getAction()) {
        case START:
            addCanDoActionMessage(VdcBllMessages.VAR__ACTION__START);
            break;
        }
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__GLUSTER_VOLUME_BRICK);
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
                        new ReplaceGlusterVolumeBrickActionVDSParameters(getUpServer().getId(),
                                getGlusterVolumeName(),
                                getParameters().getAction(),
                                getParameters().getExistingBrick().getQualifiedName(),
                                getParameters().getNewBrick().getQualifiedName(),
                                getParameters().isForceAction()));
        setSucceeded(returnValue.getSucceeded());
        if (returnValue.getSucceeded()) {
            replaceVolumeBrickInDb(getParameters().getExistingBrick(), getParameters().getNewBrick());
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
            if (brick.equals(volumeBrick)) {
                return true;
            }
        }
        return false;
    }

    private void replaceVolumeBrickInDb(GlusterBrickEntity existingBrick, GlusterBrickEntity newBrick) {
        getGlusterVolumeDao().removeBrickFromVolume(existingBrick);
        getGlusterVolumeDao().addBrickToVolume(newBrick);
    }
}

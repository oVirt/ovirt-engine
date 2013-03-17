package org.ovirt.engine.core.bll.gluster;

import org.ovirt.engine.core.bll.LockIdNameAttribute;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.gluster.ResetGlusterVolumeOptionsParameters;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeOptionEntity;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.gluster.ResetGlusterVolumeOptionsVDSParameters;

/**
 * BLL Command to Reset Gluster Volume Options
 */
@NonTransactiveCommandAttribute
@LockIdNameAttribute(isWait = true)
public class ResetGlusterVolumeOptionsCommand extends GlusterVolumeCommandBase<ResetGlusterVolumeOptionsParameters> {

    public ResetGlusterVolumeOptionsCommand(ResetGlusterVolumeOptionsParameters params) {
        super(params);
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__RESET);
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__GLUSTER_VOLUME_OPTION);
    }

    @Override
    protected void executeCommand() {
        VDSReturnValue returnValue = runVdsCommand(VDSCommandType.ResetGlusterVolumeOptions,
                new ResetGlusterVolumeOptionsVDSParameters(upServer.getId(),
                        getGlusterVolumeName(), getParameters().getVolumeOption(), getParameters().isForceAction()));
        setSucceeded(returnValue.getSucceeded());

        if (getSucceeded()) {

            if (getParameters().getVolumeOption() != null && !getParameters().getVolumeOption().isEmpty()) {
                removeOptionInDb(getGlusterVolume().getOption(getParameters().getVolumeOption()));
            } else {
                for (GlusterVolumeOptionEntity option : getGlusterVolume().getOptions()) {
                    removeOptionInDb(option);
                }
            }
        } else {
            handleVdsError(AuditLogType.GLUSTER_VOLUME_OPTIONS_RESET_FAILED, returnValue.getVdsError().getMessage());
            return;
        }
    }


    /**
     * Remove the volume option in DB. If the option with given key already exists for the volume, <br>
     * it will be deleted.
     *
     * @param option
     */
    private void removeOptionInDb(GlusterVolumeOptionEntity option) {
        getGlusterOptionDao().removeVolumeOption(option.getId());
    }


    @Override
    public AuditLogType getAuditLogTypeValue() {
        if (getSucceeded()) {
            return AuditLogType.GLUSTER_VOLUME_OPTIONS_RESET;
        } else {
            return errorType == null ? AuditLogType.GLUSTER_VOLUME_OPTIONS_RESET_FAILED : errorType;
        }
    }

}

package org.ovirt.engine.core.bll.gluster;

import org.ovirt.engine.core.bll.LockIdNameAttribute;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.gluster.GlusterVolumeOptionParameters;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeOptionEntity;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.gluster.GlusterVolumeOptionVDSParameters;
import org.ovirt.engine.core.dal.VdcBllMessages;

/**
 * BLL Command to set a Gluster Volume Option
 */
@NonTransactiveCommandAttribute
@LockIdNameAttribute(isWait = true)
public class SetGlusterVolumeOptionCommand extends GlusterVolumeCommandBase<GlusterVolumeOptionParameters> {
    private static final long serialVersionUID = 1072922951605958813L;

    public SetGlusterVolumeOptionCommand(GlusterVolumeOptionParameters params) {
        super(params);
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__SET);
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__GLUSTER_VOLUME_OPTION);
    }

    @Override
    protected boolean canDoAction() {
        return super.canDoAction();
    }

    @Override
    protected void executeCommand() {
        VDSReturnValue returnValue = runVdsCommand(VDSCommandType.SetGlusterVolumeOption,
                new GlusterVolumeOptionVDSParameters(upServer.getId(),
                        getGlusterVolumeName(), getParameters().getVolumeOption()));
        setSucceeded(returnValue.getSucceeded());
        if (getSucceeded()) {
            updateOptionInDb(getParameters().getVolumeOption());
        } else {
            handleVdsError(AuditLogType.GLUSTER_VOLUME_OPTION_SET_FAILED, returnValue.getVdsError().getMessage());
            return;
        }
    }

    /**
     * Updates the volume option in DB. If the option with given key already exists for the volume, <br>
     * it will be updated, else inserted.
     *
     * @param option
     */
    private void updateOptionInDb(final GlusterVolumeOptionEntity option) {
        // update the option value if it exists, else add it
        GlusterVolumeOptionEntity existingOption = getGlusterVolume().getOption(option.getKey());
        if (existingOption != null) {
            getGlusterOptionDao().updateVolumeOption(existingOption.getId(), option.getValue());
        } else {
            getGlusterOptionDao().save(option);
        }
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        if (getSucceeded()) {
            return AuditLogType.GLUSTER_VOLUME_OPTION_SET;
        } else {
            return errorType == null ? AuditLogType.GLUSTER_VOLUME_OPTION_SET_FAILED : errorType;
        }
    }

}

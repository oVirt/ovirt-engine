package org.ovirt.engine.core.bll.gluster;

import java.util.Map;

import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.LockProperties.Scope;
import org.ovirt.engine.core.common.action.gluster.GlusterVolumeOptionParameters;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeOptionEntity;
import org.ovirt.engine.core.common.constants.gluster.GlusterConstants;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.gluster.GlusterVolumeOptionVDSParameters;

/**
 * BLL Command to set a Gluster Volume Option
 */
@NonTransactiveCommandAttribute
public class SetGlusterVolumeOptionCommand extends GlusterVolumeCommandBase<GlusterVolumeOptionParameters> {

    private boolean optionValueExists;

    public SetGlusterVolumeOptionCommand(GlusterVolumeOptionParameters params, CommandContext commandContext) {
        super(params, commandContext);
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(Scope.Execution).withWaitForever();
    }

    @Override
    public Map<String, String> getCustomValues() {
        addCustomValue(GlusterConstants.OPTION_KEY, getParameters().getVolumeOption().getKey());
        addCustomValue(GlusterConstants.OPTION_VALUE, getParameters().getVolumeOption().getValue());
        return super.getCustomValues();
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__ACTION__SET);
        addValidationMessage(EngineMessage.VAR__TYPE__GLUSTER_VOLUME_OPTION);
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
     */
    private void updateOptionInDb(final GlusterVolumeOptionEntity option) {
        // update the option value if it exists, else add it
        GlusterVolumeOptionEntity existingOption = getGlusterVolume().getOption(option.getKey());
        if (existingOption != null) {
            if(option.getValue().equalsIgnoreCase(existingOption.getValue())) {
                return;
            }

            optionValueExists = true;
            addCustomValue(GlusterConstants.OPTION_OLD_VALUE, existingOption.getValue());
            glusterOptionDao.updateVolumeOption(existingOption.getId(), option.getValue());
        } else {
            optionValueExists = false;
            glusterOptionDao.save(option);
        }
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        if (getSucceeded()) {
            return optionValueExists ? AuditLogType.GLUSTER_VOLUME_OPTION_MODIFIED : AuditLogType.GLUSTER_VOLUME_OPTION_ADDED;
        } else {
            return errorType == null ? AuditLogType.GLUSTER_VOLUME_OPTION_SET_FAILED : errorType;
        }
    }

}

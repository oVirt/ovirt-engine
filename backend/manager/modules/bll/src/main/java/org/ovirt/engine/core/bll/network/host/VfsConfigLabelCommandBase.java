package org.ovirt.engine.core.bll.network.host;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.action.VfsConfigLabelParameters;
import org.ovirt.engine.core.common.errors.VdcBllMessages;

public abstract class VfsConfigLabelCommandBase extends VfsConfigCommandBase<VfsConfigLabelParameters> {

    public VfsConfigLabelCommandBase(VfsConfigLabelParameters parameters) {
        this(parameters, null);
    }

    public VfsConfigLabelCommandBase(VfsConfigLabelParameters parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }

    @Override
    protected boolean canDoAction() {
        return super.canDoAction() && validate(getVfsConfigValidator().settingSpecificNetworksAllowed());
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__HOST_NIC_VFS_CONFIG_LABEL);
    }

    public String getLabel() {
        return getParameters().getLabel();
    }
}

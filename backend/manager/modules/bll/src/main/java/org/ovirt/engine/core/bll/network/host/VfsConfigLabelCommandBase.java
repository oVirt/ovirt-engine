package org.ovirt.engine.core.bll.network.host;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.action.VfsConfigLabelParameters;
import org.ovirt.engine.core.common.errors.EngineMessage;

public abstract class VfsConfigLabelCommandBase extends VfsConfigCommandBase<VfsConfigLabelParameters> {

    public VfsConfigLabelCommandBase(VfsConfigLabelParameters parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }

    @Override
    protected boolean validate() {
        return super.validate() && validate(getVfsConfigValidator().settingSpecificNetworksAllowed());
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__TYPE__HOST_NIC_VFS_CONFIG_LABEL);
    }

    public String getLabel() {
        return getParameters().getLabel();
    }
}

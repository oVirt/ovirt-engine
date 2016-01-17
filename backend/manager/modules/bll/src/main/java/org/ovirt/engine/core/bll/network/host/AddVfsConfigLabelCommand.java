package org.ovirt.engine.core.bll.network.host;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.VfsConfigLabelParameters;
import org.ovirt.engine.core.common.errors.EngineMessage;

public class AddVfsConfigLabelCommand extends VfsConfigLabelCommandBase {

    public AddVfsConfigLabelCommand(VfsConfigLabelParameters parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }

    @Override
    protected void executeCommand() {
        super.executeCommand();

        getVfsConfigDao().addLabel(getVfsConfig().getId(), getLabel());

        setSucceeded(true);
        setActionReturnValue(getLabel());
    }

    @Override
    protected boolean validate() {
        return super.validate() && validate(getVfsConfigValidator().labelNotInVfsConfig(getLabel()));
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.ADD_VFS_CONFIG_LABEL
                : AuditLogType.ADD_VFS_CONFIG_LABEL_FAILED;
    }

    @Override
    protected void setActionMessageParameters() {
        super.setActionMessageParameters();
        addValidationMessage(EngineMessage.VAR__ACTION__ADD);
    }
}

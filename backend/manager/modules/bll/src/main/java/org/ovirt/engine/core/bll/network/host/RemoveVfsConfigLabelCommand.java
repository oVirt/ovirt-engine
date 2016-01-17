package org.ovirt.engine.core.bll.network.host;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.VfsConfigLabelParameters;
import org.ovirt.engine.core.common.errors.EngineMessage;

public class RemoveVfsConfigLabelCommand extends VfsConfigLabelCommandBase {

    public RemoveVfsConfigLabelCommand(VfsConfigLabelParameters parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }

    @Override
    protected void executeCommand() {
        super.executeCommand();

        getVfsConfigDao().removeLabel(getVfsConfig().getId(), getLabel());
        setSucceeded(true);
    }

    @Override
    protected boolean validate() {
        return super.validate() && validate(getVfsConfigValidator().labelInVfsConfig(getLabel()));
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.REMOVE_VFS_CONFIG_LABEL
                : AuditLogType.REMOVE_VFS_CONFIG_LABEL_FAILED;
    }

    @Override
    protected void setActionMessageParameters() {
        super.setActionMessageParameters();
        addValidationMessage(EngineMessage.VAR__ACTION__REMOVE);
    }
}

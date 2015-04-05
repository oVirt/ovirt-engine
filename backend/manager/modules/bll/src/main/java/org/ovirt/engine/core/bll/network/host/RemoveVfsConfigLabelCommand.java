package org.ovirt.engine.core.bll.network.host;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.VfsConfigLabelParameters;
import org.ovirt.engine.core.common.errors.VdcBllMessages;

public class RemoveVfsConfigLabelCommand extends VfsConfigLabelCommandBase {

    public RemoveVfsConfigLabelCommand(VfsConfigLabelParameters parameters) {
        this(parameters, null);
    }

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
    protected boolean canDoAction() {
        return super.canDoAction() && validate(getVfsConfigValidator().labelInVfsConfig(getLabel()));
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.REMOVE_VFS_CONFIG_LABEL
                : AuditLogType.REMOVE_VFS_CONFIG_LABEL_FAILED;
    }

    @Override
    protected void setActionMessageParameters() {
        super.setActionMessageParameters();
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__REMOVE);
    }
}

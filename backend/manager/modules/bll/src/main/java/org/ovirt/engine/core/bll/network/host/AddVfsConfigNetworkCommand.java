package org.ovirt.engine.core.bll.network.host;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.VfsConfigNetworkParameters;
import org.ovirt.engine.core.common.errors.VdcBllMessages;

public class AddVfsConfigNetworkCommand extends NetworkVfsConfigCommandBase {

    public AddVfsConfigNetworkCommand(VfsConfigNetworkParameters parameters) {
        this(parameters, null);
    }

    public AddVfsConfigNetworkCommand(VfsConfigNetworkParameters parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }

    @Override
    protected void executeCommand() {
        super.executeCommand();

        getVfsConfigDao().addNetwork(getVfsConfig().getId(), getNetworkId());
        setSucceeded(true);
    }

    @Override
    protected boolean canDoAction() {
        return super.canDoAction() && validate(getVfsConfigValidator().networkNotInVfsConfig(getNetworkId()));
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.ADD_VFS_CONFIG_NETWORK
                : AuditLogType.ADD_VFS_CONFIG_NETWORK_FAILED;
    }

    @Override
    protected void setActionMessageParameters() {
        super.setActionMessageParameters();
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__ADD);
    }
}

package org.ovirt.engine.core.bll.network.dc;

import org.ovirt.engine.core.bll.validator.NetworkValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.AddNetworkStoragePoolParameters;
import org.ovirt.engine.core.common.errors.VdcBllMessages;

public class RemoveNetworkCommand<T extends AddNetworkStoragePoolParameters> extends NetworkCommon<T> {
    public RemoveNetworkCommand(T parameters) {
        super(parameters);
    }

    @Override
    protected void executeCommand() {
        getNetworkDAO().remove(getNetwork().getId());
        setSucceeded(true);
    }

    @Override
    protected void setActionMessageParameters() {
        super.setActionMessageParameters();
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__REMOVE);
    }

    @Override
    protected boolean canDoAction() {
        NetworkValidator validator = new NetworkValidator(getNetworkDAO().get(getNetwork().getId()));
        return validate(validator.networkIsSet())
                && validate(validator.notManagementNetwork())
                && validate(validator.networkNotUsedByVms())
                && validate(validator.networkNotUsedByTemplates());
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.NETWORK_REMOVE_NETWORK : AuditLogType.NETWORK_REMOVE_NETWORK_FAILED;
    }
}

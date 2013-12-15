package org.ovirt.engine.core.bll.qos;

import org.ovirt.engine.core.bll.validator.NetworkQosValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.NetworkQoSParametersBase;
import org.ovirt.engine.core.common.errors.VdcBllMessages;

public class RemoveNetworkQoSCommand extends NetworkQoSCommandBase {

    public RemoveNetworkQoSCommand(NetworkQoSParametersBase parameters) {
        super(parameters);
    }

    @Override
    protected boolean canDoAction() {
        if (validateParameters()) {
            NetworkQosValidator validator = new NetworkQosValidator(getNetworkQoS());
            return validate(validator.qosExists()) && validate(validator.consistentDataCenter());
        }
        return true;
    }

    @Override
    protected void executeCommand() {
        getNetworkQoSDao().remove(getNetworkQoS().getId());
        getReturnValue().setActionReturnValue(getNetworkQoS().getId());
        setSucceeded(true);
    }

    @Override
    protected void setActionMessageParameters() {
        super.setActionMessageParameters();
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__REMOVE);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_REMOVED_NETWORK_QOS : AuditLogType.USER_FAILED_TO_REMOVE_NETWORK_QOS;
    }
}

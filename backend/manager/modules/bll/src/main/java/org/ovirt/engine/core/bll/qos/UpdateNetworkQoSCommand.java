package org.ovirt.engine.core.bll.qos;


import org.ovirt.engine.core.bll.validator.NetworkQosValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.NetworkQoSParametersBase;
import org.ovirt.engine.core.common.errors.VdcBllMessages;

public class UpdateNetworkQoSCommand extends NetworkQoSCommandBase {

    public UpdateNetworkQoSCommand(NetworkQoSParametersBase parameters) {
        super(parameters);
    }

    @Override
    protected boolean canDoAction() {
        NetworkQosValidator validator = new NetworkQosValidator(getNetworkQoS());
        return (validateParameters()
                && validate(validator.qosExists())
                && validate(validator.consistentDataCenter())
                && validateValues()
                && validate(validator.nameNotChangedOrNotTaken()));
    }

    @Override
    protected void executeCommand() {
        getNetworkQoSDao().update(getNetworkQoS());
        getReturnValue().setActionReturnValue(getNetworkQoS().getId());
        setSucceeded(true);
    }

    @Override
    protected void setActionMessageParameters() {
        super.setActionMessageParameters();
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__UPDATE);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_UPDATED_NETWORK_QOS : AuditLogType.USER_FAILED_TO_UPDATE_NETWORK_QOS;
    }
}

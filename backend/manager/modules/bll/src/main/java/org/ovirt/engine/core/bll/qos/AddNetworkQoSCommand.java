package org.ovirt.engine.core.bll.qos;


import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.NetworkQoSParametersBase;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.compat.Guid;

public class AddNetworkQoSCommand extends NetworkQoSCommandBase {

    public AddNetworkQoSCommand(NetworkQoSParametersBase parameters) {
        super(parameters);
    }

    @Override
    protected boolean canDoAction() {
        return validateParameters()
                && validateNameNotExistInDC()
                && validateValues();
    }

    @Override
    protected void executeCommand() {
        getNetworkQoS().setId(Guid.newGuid());
        getNetworkQoSDao().save(getNetworkQoS());
        getReturnValue().setActionReturnValue(getNetworkQoS().getId());
        setSucceeded(true);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_ADDED_NETWORK_QOS : AuditLogType.USER_FAILED_TO_ADD_NETWORK_QOS;
    }

    @Override
    protected void setActionMessageParameters() {
        super.setActionMessageParameters();
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__ADD);
    }
}

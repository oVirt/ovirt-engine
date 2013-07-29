package org.ovirt.engine.core.bll.qos;


import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.NetworkQoSParametersBase;
import org.ovirt.engine.core.common.businessentities.network.NetworkQoS;
import org.ovirt.engine.core.common.errors.VdcBllMessages;

public class UpdateNetworkQoSCommand extends NetworkQoSCommandBase {

    public UpdateNetworkQoSCommand(NetworkQoSParametersBase parameters) {
        super(parameters);
    }

    @Override
    protected boolean canDoAction() {
        if (validateParameters()) {
            NetworkQoS oldNetworkQoS =  getNetworkQoSDao().get(getNetworkQoS().getId());
            if (oldNetworkQoS == null) {
                return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_NETWORK_QOS_NOT_FOUND);
            } else if (!oldNetworkQoS.getStoragePoolId().equals(getNetworkQoS().getStoragePoolId())) {
                return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_NETWORK_QOS_INVALID_DC_ID);
            } else {
                if (validateValues() && !oldNetworkQoS.getName().equals(getNetworkQoS().getName())) {
                    return validateNameNotExistInDC();
                }
            }
        }
        return true;
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

package org.ovirt.engine.core.bll.qos;


import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.NetworkQoSParametersBase;
import org.ovirt.engine.core.common.businessentities.network.NetworkQoS;
import org.ovirt.engine.core.common.errors.VdcBllMessages;

public class RemoveNetworkQoSCommand extends NetworkQoSCommandBase {

    public RemoveNetworkQoSCommand(NetworkQoSParametersBase parameters) {
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
            }
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

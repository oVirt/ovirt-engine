package org.ovirt.engine.core.bll.network.dc;


import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.AddNetworkStoragePoolParameters;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.dal.VdcBllMessages;

@SuppressWarnings("serial")
public class RemoveNetworkCommand<T extends AddNetworkStoragePoolParameters> extends NetworkCommon<T> {
    private Network network;

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
        return validate(networkExists(getRemovedNetwork()))
                && validate(notManagementNetwork())
                && validate(networkNotUsedByVms())
                && validate(networkNotUsedByTemplates(getRemovedNetwork()))
                && validate(networkNotUsedByHosts(getRemovedNetwork()));
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.NETWORK_REMOVE_NETWORK : AuditLogType.NETWORK_REMOVE_NETWORK_FAILED;
    }

    private ValidationResult notManagementNetwork() {
        String managementNetwork = Config.<String> GetValue(ConfigValues.ManagementNetwork);
        return managementNetwork.equals(getRemovedNetwork().getName())
                ? new ValidationResult(VdcBllMessages.NETWORK_CAN_NOT_REMOVE_DEFAULT_NETWORK)
                : ValidationResult.VALID;
    }

    protected Network getRemovedNetwork() {
        if (network == null) {
            network = getNetworkDAO().get(super.getNetwork().getId());
        }

        return network;
    }
}

package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.NetworkClusterParameters;
import org.ovirt.engine.core.common.businessentities.Network;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.CustomLogField;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.CustomLogFields;

@SuppressWarnings("serial")
@CustomLogFields({ @CustomLogField("NetworkName") })
public class UpdateNetworkOnClusterCommand<T extends NetworkClusterParameters> extends
        VdsGroupCommandBase<T> {

    private Network network;

    public UpdateNetworkOnClusterCommand(T parameters) {
        super(parameters);
        setVdsGroupId(parameters.getVdsGroupId());
    }

    private Network getNetwork() {
        if (network == null) {
            network = getNetworkDAO().get(getParameters().getNetworkCluster().getnetwork_id());
        }

        return network;
    }

    public String getNetworkName() {
        return getNetwork().getname();
    }

    @Override
    protected void executeCommand() {
        getNetworkClusterDAO().update(getParameters().getNetworkCluster());

        if (getParameters().getNetworkCluster().getis_display()) {
            getNetworkClusterDAO().setNetworkExclusivelyAsDisplay(getVdsGroupId(), getNetwork().getId());
        }

        AttachNetworkToVdsGroupCommand.SetNetworkStatus(getVdsGroupId(), getNetwork());
        setSucceeded(true);
    }

    @Override
    protected boolean canDoAction() {
        return super.canDoAction() && validate(networkClusterAttachmentExists());
    }

    private ValidationResult networkClusterAttachmentExists() {
        return getNetworkClusterDAO().get(getParameters().getNetworkCluster().getId()) == null ?
                new ValidationResult(VdcBllMessages.NETWORK_NOT_EXISTS_IN_CURRENT_CLUSTER) : ValidationResult.VALID;
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.NETWORK_UPDTAE_NETWORK_ON_CLUSTER
                : AuditLogType.NETWORK_UPDTAE_NETWORK_ON_CLUSTER_FAILED;
    }
}

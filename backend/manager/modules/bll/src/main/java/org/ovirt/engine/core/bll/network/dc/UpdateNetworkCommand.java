package org.ovirt.engine.core.bll.network.dc;

import java.util.List;

import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.network.cluster.NetworkClusterHelper;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.AddNetworkStoragePoolParameters;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.validation.group.UpdateEntity;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NGuid;
import org.ovirt.engine.core.dal.VdcBllMessages;

@SuppressWarnings("serial")
public class UpdateNetworkCommand<T extends AddNetworkStoragePoolParameters> extends NetworkCommon<T> {
    private Network oldNetwork;

    public UpdateNetworkCommand(T parameters) {
        super(parameters);
    }

    @Override
    protected void executeCommand() {
        getNetworkDAO().update(getNetwork());

        for (NetworkCluster clusterAttachment : getClusterAttachments()) {
            NetworkClusterHelper.setStatus(clusterAttachment.getClusterId(), getNetwork());
        }
        setSucceeded(true);
    }

    @Override
    protected void setActionMessageParameters() {
        super.setActionMessageParameters();
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__UPDATE);
    }

    @Override
    protected boolean canDoAction() {
        return validate(storagePoolExists())
                && validate(vmNetworkSetCorrectly())
                && validate(stpForVmNetworkOnly())
                && validate(mtuValid())
                && validate(networkPrefixValid())
                && validate(vlanIsFree())
                && validate(networkExists(getOldNetwork()))
                && validate(notChangingManagementNetworkName())
                && validate(networkNameNotUsed())
                && validate(networkNotUsedByVms(getOldNetwork()))
                && validate(networkNotUsedByHosts(getOldNetwork()));
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.NETWORK_UPDATE_NETWORK : AuditLogType.NETWORK_UPDATE_NETWORK_FAILED;
    }

    @Override
    protected List<Class<?>> getValidationGroups() {
        addValidationGroup(UpdateEntity.class);
        return super.getValidationGroups();
    }

    private Network getOldNetwork(){
        if (oldNetwork == null) {
            oldNetwork = getNetworkById(getNetworks());
        }
        return oldNetwork;
    }

    private Network getNetworkById(List<Network> networks) {
        Guid networkId = getNetwork().getId();
        for (Network network : networks) {
            if (network.getId().equals(networkId)) {
                return network;
            }
        }
        return null;
    }

    private ValidationResult networkNameNotUsed() {
        Network networkWithSameName = getOtherNetworkWithSameName(getNetworks());
        return networkWithSameName != null
                ? new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_NETWORK_NAME_IN_USE)
                : ValidationResult.VALID;
    }

    private Network getOtherNetworkWithSameName(List<Network> networks) {
        String networkName = getNetworkName().toLowerCase();
        Guid networkId = getNetwork().getId();
        NGuid dataCenterId = getNetwork().getDataCenterId();
        for (Network network : networks) {
            if (network.getName().toLowerCase().equals(networkName)
                    && !network.getId().equals(networkId)
                    && dataCenterId.equals(network.getDataCenterId())) {
                return network;
            }
        }
        return null;
    }

    private ValidationResult notChangingManagementNetworkName() {
        String managementNetwork = Config.<String> GetValue(ConfigValues.ManagementNetwork);
        return getOldNetwork().getName().equals(managementNetwork) &&
                !getNetworkName().equals(managementNetwork)
                ? new ValidationResult(VdcBllMessages.NETWORK_CAN_NOT_REMOVE_DEFAULT_NETWORK)
                : ValidationResult.VALID;
    }
}

package org.ovirt.engine.core.bll.network.dc;

import java.util.List;

import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.network.cluster.NetworkClusterHelper;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.AddNetworkStoragePoolParameters;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.validation.group.UpdateEntity;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NGuid;
import org.ovirt.engine.core.dal.VdcBllMessages;

@SuppressWarnings("serial")
public class UpdateNetworkCommand<T extends AddNetworkStoragePoolParameters> extends NetworkCommon<T> {
    private List<VDSGroup> clusters;

    public UpdateNetworkCommand(T parameters) {
        super(parameters);
    }

    @Override
    protected void executeCommand() {
        getNetworkDAO().update(getNetwork());

        for (VDSGroup cluster : getClusters()) {
            NetworkClusterHelper.setStatus(cluster.getId(), getNetwork());
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
        if (!validate(storagePoolExists())) {
            return false;
        }

        if (!validate(vmNetworkSetCorrectly())) {
            return false;
        }

        if (!validate(stpForVmNetworkOnly())) {
            return false;
        }

        if (!validate(mtuValid())) {
            return false;
        }

        if (!validate(networkPrefixValid())) {
            return false;
        }

        List<Network> networks = getNetworkDAO().getAll();

        if (!validate(vlanIsFree(networks))) {
            return false;
        }

        Network oldNetwork = getNetworkById(networks);
        if (!validate(networkExists(oldNetwork))) {
            return false;
        }

        if (!validate(notChangingManagementNetworkName(oldNetwork))) {
            return false;
        }

        if (!validate(networkNameNotUsed(networks))) {
            return false;
        }

        if (!validate(networkNotUsedByRunningVm())) {
            return false;
        }

        return validate(networkNotAttachedToCluster(oldNetwork));
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

    private List<VDSGroup> getClusters() {
        if (clusters == null) {
            clusters = getVdsGroupDAO().getAllForStoragePool(getStoragePool().getId());
        }

        return clusters;
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

    private ValidationResult networkExists(Network oldNetwork) {
        return oldNetwork == null
            ? new ValidationResult(VdcBllMessages.NETWORK_NOT_EXISTS)
                : ValidationResult.VALID;
    }

    private ValidationResult networkNameNotUsed(List<Network> networks) {
        Network networkWithSameName = getOtherNetworkWithSameName(networks);
        return networkWithSameName != null
                ? new ValidationResult(VdcBllMessages.NETWORK_IN_USE)
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

    private ValidationResult networkNotUsedByRunningVm() {
        String networkName = getNetworkName();
        for (VDSGroup cluster : getClusters()) {
            List<VmStatic> vms = getVmStaticDAO().getAllByGroupAndNetworkName(cluster.getId(), networkName);
            if (vms.size() > 0) {
                return new ValidationResult(VdcBllMessages.NETWORK_INTERFACE_IN_USE_BY_VM);
            }
        }
        return ValidationResult.VALID;
    }

    private ValidationResult notChangingManagementNetworkName(Network oldNetwork) {
        String managementNetwork = Config.<String> GetValue(ConfigValues.ManagementNetwork);
        return oldNetwork.getName().equals(managementNetwork) &&
                !getNetworkName().equals(managementNetwork)
                ? new ValidationResult(VdcBllMessages.NETWORK_CAN_NOT_REMOVE_DEFAULT_NETWORK)
                : ValidationResult.VALID;
    }
}

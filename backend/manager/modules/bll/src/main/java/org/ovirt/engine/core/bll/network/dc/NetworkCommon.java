package org.ovirt.engine.core.bll.network.dc;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.AddNetworkStoragePoolParameters;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.CustomLogField;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.CustomLogFields;
import org.ovirt.engine.core.utils.ReplacementUtils;

@SuppressWarnings("serial")
@CustomLogFields({ @CustomLogField("NetworkName") })
public abstract class NetworkCommon<T extends AddNetworkStoragePoolParameters> extends CommandBase<T> {
    private List<Network> networks;
    private List<NetworkCluster> clusterAttachments;

    public NetworkCommon(T parameters) {
        super(parameters);
        this.setStoragePoolId(getNetwork().getDataCenterId());
    }

    protected Network getNetwork() {
        return getParameters().getNetwork();
    }

    public String getNetworkName() {
        return getNetwork().getName();
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__NETWORK);
    }

    protected ValidationResult vmNetworkSetCorrectly() {
        return getNetwork().isVmNetwork() || FeatureSupported.nonVmNetwork(getStoragePool().getcompatibility_version())
                ? ValidationResult.VALID
                : new ValidationResult(VdcBllMessages.NON_VM_NETWORK_NOT_SUPPORTED_FOR_POOL_LEVEL);
    }

    protected ValidationResult stpForVmNetworkOnly() {
        return getNetwork().isVmNetwork() || !getNetwork().getStp()
                ? ValidationResult.VALID
                : new ValidationResult(VdcBllMessages.NON_VM_NETWORK_CANNOT_SUPPORT_STP);
    }

    protected ValidationResult mtuValid() {
        return getNetwork().getMtu() == 0
                || FeatureSupported.mtuSpecification(getStoragePool().getcompatibility_version())
                ? ValidationResult.VALID
                : new ValidationResult(VdcBllMessages.NETWORK_MTU_OVERRIDE_NOT_SUPPORTED);
    }

    protected ValidationResult vlanIsFree() {
        if (getNetwork().getVlanId() != null) {
            for (Network network : getNetworks()) {
                if (network.getVlanId() != null
                        && network.getVlanId().equals(getNetwork().getVlanId())
                        && network.getDataCenterId().equals(getNetwork().getDataCenterId())
                        && !network.getId().equals(getNetwork().getId())) {
                    return new ValidationResult(VdcBllMessages.NETWORK_VLAN_IN_USE,
                            String.format("$vlanId %d", getNetwork().getVlanId()));
                }
            }
        }
        return ValidationResult.VALID;
    }

    protected ValidationResult networkPrefixValid() {
        return getNetworkName().toLowerCase().startsWith("bond")
                ? new ValidationResult(VdcBllMessages.NETWORK_CANNOT_CONTAIN_BOND_NAME)
                : ValidationResult.VALID;
    }

    protected ValidationResult storagePoolExists() {
        return getStoragePool() == null
                ? new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_POOL_NOT_EXIST)
                : ValidationResult.VALID;
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        Network network = getNetwork();
        Guid networkId = network == null ? null : network.getId();

        return Collections.singletonList(new PermissionSubject(networkId,
                VdcObjectType.Network,
                getActionType().getActionGroup()));
    }

    protected List<Network> getNetworks() {
        if (networks == null) {
            networks = getNetworkDAO().getAllForDataCenter(getNetwork().getDataCenterId());
        }
        return networks;
    }

    protected ValidationResult networkExists(Network network) {
        return network == null
                ? new ValidationResult(VdcBllMessages.NETWORK_NOT_EXISTS)
                : ValidationResult.VALID;
    }

    protected ValidationResult networkNotUsedByVms() {
        String networkName = getNetworkName();
        for (NetworkCluster clusterAttachment : getClusterAttachments()) {
            List<VmStatic> vms =
                    getVmStaticDAO().getAllByGroupAndNetworkName(clusterAttachment.getClusterId(), networkName);
            if (vms.size() > 0) {
                return new ValidationResult(VdcBllMessages.NETWORK_INTERFACE_IN_USE_BY_VM);
            }
        }
        return ValidationResult.VALID;
    }

    protected List<NetworkCluster> getClusterAttachments() {
        if (clusterAttachments == null) {
            clusterAttachments = getNetworkClusterDAO().getAllForNetwork(getNetwork().getId());
        }

        return clusterAttachments;
    }

    protected ValidationResult networkNotUsedByHosts(final Network network) {
        List<VDS> hostsWithNetwork = getVdsDAO().getAllForNetwork(network.getId());
        if (hostsWithNetwork.isEmpty()) {
            return ValidationResult.VALID;
        }

        Collection<String> replacements =
                ReplacementUtils.replaceWithNameable("ENTITIES_USING_NETWORK", hostsWithNetwork);
        replacements.add(VdcBllMessages.VAR__ENTITIES__HOSTS.name());
        return new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_NETWORK_IN_USE, replacements);
    }

    protected ValidationResult networkNotUsedByTemplates(final Network network) {
        List<VmTemplate> templates = getVmTemplateDAO().getAllForNetwork(network.getId());
        if (templates.isEmpty()) {
            return ValidationResult.VALID;
        }

        Collection<String> replacements = ReplacementUtils.replaceWithNameable("ENTITIES_USING_NETWORK", templates);
        replacements.add(VdcBllMessages.VAR__ENTITIES__VM_TEMPLATES.name());
        return new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_NETWORK_IN_USE, replacements);
    }
}

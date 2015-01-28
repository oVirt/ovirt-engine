package org.ovirt.engine.core.bll.validator;

import java.util.Collection;
import java.util.List;

import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.network.cluster.ManagementNetworkUtil;
import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.businessentities.IscsiBond;
import org.ovirt.engine.core.common.businessentities.Nameable;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.di.Injector;
import org.ovirt.engine.core.utils.NetworkUtils;
import org.ovirt.engine.core.utils.ReplacementUtils;

/**
 * This class is used to validate different traits of a network on the data center level. <br>
 * <br>
 * Usage: instantiate on a per-network basis, passing the network to be validated as an argument to the constructor.
 */
public class NetworkValidator {

    protected final Network network;

    private StoragePool dataCenter;
    private List<Network> networks;
    private List<VM> vms;
    private List<VmTemplate> templates;

    public NetworkValidator(Network network) {
        this.network = network;
    }

    protected DbFacade getDbFacade() {
        return DbFacade.getInstance();
    }

    protected StoragePool getDataCenter() {
        if (dataCenter == null) {
            dataCenter = getDbFacade().getStoragePoolDao().get(network.getDataCenterId());
        }
        return dataCenter;
    }

    /**
     * @return All existing networks in same data center.
     */
    protected List<Network> getNetworks() {
        if (networks == null) {
            networks = getDbFacade().getNetworkDao().getAllForDataCenter(network.getDataCenterId());
        }
        return networks;
    }

    /**
     * @return An error iff network is defined as non-VM when that feature is not supported.
     */
    public ValidationResult vmNetworkSetCorrectly() {
        return ValidationResult.failWith(VdcBllMessages.NON_VM_NETWORK_NOT_SUPPORTED_FOR_POOL_LEVEL)
                .unless(network.isVmNetwork()
                        || FeatureSupported.nonVmNetwork(getDataCenter().getCompatibilityVersion()));
    }

    /**
     * @return An error iff STP is specified for a non-VM network.
     */
    public ValidationResult stpForVmNetworkOnly() {
        return ValidationResult.failWith(VdcBllMessages.NON_VM_NETWORK_CANNOT_SUPPORT_STP)
                .unless(network.isVmNetwork() || !network.getStp());
    }

    /**
     * @return An error iff nonzero MTU was specified when the MTU feature is not supported.
     */
    public ValidationResult mtuValid() {
        return ValidationResult.failWith(VdcBllMessages.NETWORK_MTU_OVERRIDE_NOT_SUPPORTED)
                .unless(network.getMtu() == 0
                        || FeatureSupported.mtuSpecification(getDataCenter().getCompatibilityVersion()));
    }

    /**
     * @return An error iff a different network in the data center is already using the specified VLAN ID.
     */
    public ValidationResult vlanIdNotUsed() {
        if (NetworkUtils.isVlan(network)) {
            for (Network otherNetwork : getNetworks()) {
                if (NetworkUtils.isVlan(otherNetwork)
                        && otherNetwork.getVlanId().equals(network.getVlanId())
                        && !otherNetwork.getId().equals(network.getId())) {
                    return new ValidationResult(VdcBllMessages.NETWORK_VLAN_IN_USE,
                            String.format("$vlanId %d", network.getVlanId()));
                }
            }
        }
        return ValidationResult.VALID;
    }

    /**
     * @return An error iff network is named as if it were a bond.
     */
    public ValidationResult networkPrefixValid() {
        return ValidationResult.failWith(VdcBllMessages.NETWORK_CANNOT_CONTAIN_BOND_NAME)
                .when(network.getName().toLowerCase().startsWith("bond"));
    }

    /**
     * @return An error iff the data center to which the network belongs doesn't exist.
     */
    public ValidationResult dataCenterExists() {
        return ValidationResult.failWith(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_POOL_NOT_EXIST)
                .when(getDataCenter() == null);
    }

    /**
     * @return An error iff the network isn't set.
     */
    public ValidationResult networkIsSet() {
        return ValidationResult.failWith(VdcBllMessages.NETWORK_NOT_EXISTS)
                .when(network == null);
    }

    /**
     * @return An error if the network's name is already used by another network in the same data center.
     */
    public ValidationResult networkNameNotUsed() {
        for (Network otherNetwork : getNetworks()) {
            if (otherNetwork.getName().equals(network.getName()) &&
                    !otherNetwork.getId().equals(network.getId())) {
                return new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_NETWORK_NAME_IN_USE,
                        getNetworkNameReplacement());
            }
        }
        return ValidationResult.VALID;
    }

    public ValidationResult notManagementNetwork() {
        final boolean isManagementNetwork = isManagementNetwork();
        return getManagementNetworkValidationResult(isManagementNetwork);
    }

    protected boolean isManagementNetwork() {
        return getManagementNetworkUtil().isManagementNetwork(network.getId());
    }

    private ValidationResult getManagementNetworkValidationResult(final boolean isManagementNetwork) {
        return isManagementNetwork
                                  ? new ValidationResult(VdcBllMessages.NETWORK_CANNOT_REMOVE_MANAGEMENT_NETWORK,
                                          getNetworkNameReplacement())
                                  : ValidationResult.VALID;
    }

    protected ManagementNetworkUtil getManagementNetworkUtil() {
        return Injector.get(ManagementNetworkUtil.class);
    }

    public ValidationResult notIscsiBondNetwork() {
        List<IscsiBond> iscsiBonds = getDbFacade().getIscsiBondDao().getIscsiBondsByNetworkId(network.getId());
        if (!iscsiBonds.isEmpty()) {
            Collection<String> replaceNameables = ReplacementUtils.replaceWithNameable("IscsiBonds", iscsiBonds);
            replaceNameables.add(getNetworkNameReplacement());
            return new ValidationResult(VdcBllMessages.NETWORK_CANNOT_REMOVE_ISCSI_BOND_NETWORK,
                    replaceNameables);
        }
        return ValidationResult.VALID;
    }

    protected String getNetworkNameReplacement() {
        return String.format("$NetworkName %s", network.getName());
    }

    protected ValidationResult networkNotUsed(List<? extends Nameable> entities, VdcBllMessages entitiesReplacementPlural, VdcBllMessages entitiesReplacementSingular) {
        if (entities.isEmpty()) {
            return ValidationResult.VALID;
        }

        Collection<String> replacements = ReplacementUtils.replaceWithNameable("ENTITIES_USING_NETWORK", entities);
        VdcBllMessages replacementMessageToUse = entities.size() == 1 ? entitiesReplacementSingular : entitiesReplacementPlural;
        replacements.add(replacementMessageToUse.name());
        return new ValidationResult(getNetworkInUseValidationMessage(entities.size()), replacements);
    }

    private VdcBllMessages getNetworkInUseValidationMessage(int numberOfEntities) {
        boolean singular = numberOfEntities == 1;
        if (singular) {
            return VdcBllMessages.ACTION_TYPE_FAILED_NETWORK_IN_ONE_USE;
        } else {
            return VdcBllMessages.ACTION_TYPE_FAILED_NETWORK_IN_MANY_USES;
        }
    }

    /**
     * @return An error iff the network is in use by any VMs.
     */
    public ValidationResult networkNotUsedByVms() {
        return networkNotUsed(getVms(), VdcBllMessages.VAR__ENTITIES__VMS, VdcBllMessages.VAR__ENTITIES__VM);
    }

    /**
     * @return An error iff the network is in use by any hosts.
     */
    public ValidationResult networkNotUsedByHosts() {
        return networkNotUsed(getDbFacade().getVdsDao().getAllForNetwork(network.getId()),
                VdcBllMessages.VAR__ENTITIES__HOSTS, VdcBllMessages.VAR__ENTITIES__HOST);
    }

    /**
     * @return An error iff the network is in use by any templates.
     */
    public ValidationResult networkNotUsedByTemplates() {
        return networkNotUsed(getTemplates(), VdcBllMessages.VAR__ENTITIES__VM_TEMPLATES, VdcBllMessages.VAR__ENTITIES__VM_TEMPLATE);
    }

    /**
     * @return An error iff the QoS entity attached to the network isn't null, but doesn't exist in the database or
     *         belongs to the wrong DC.
     */
    public ValidationResult qosExistsInDc() {
        HostNetworkQosValidator qosValidator =
                new HostNetworkQosValidator(getDbFacade().getHostNetworkQosDao().get(network.getQosId()));
        ValidationResult res = qosValidator.qosExists();
        return (res == ValidationResult.VALID) ? qosValidator.consistentDataCenter() : res;
    }

    public ValidationResult notLabeled() {
        return ValidationResult.failWith(VdcBllMessages.ACTION_TYPE_FAILED_NETWORK_ALREADY_LABELED)
                .when(NetworkUtils.isLabeled(network));
    }

    public ValidationResult notExternalNetwork() {
        return ValidationResult.failWith(VdcBllMessages.ACTION_TYPE_FAILED_NOT_SUPPORTED_FOR_EXTERNAL_NETWORK)
                .when(network.isExternal());
    }

    protected List<VM> getVms() {
        if (vms == null) {
            vms = getDbFacade().getVmDao().getAllForNetwork(network.getId());
        }

        return vms;
    }

    protected List<VmTemplate> getTemplates() {
        if (templates == null) {
            templates = getDbFacade().getVmTemplateDao().getAllForNetwork(network.getId());
        }

        return templates;
    }

    public boolean canNetworkCompatabilityBeDecreased() {
        return vmNetworkSetCorrectly().isValid() && mtuValid().isValid();
    }

    public void setDataCenter(StoragePool dataCenter) {
        this.dataCenter = dataCenter;
    }
}

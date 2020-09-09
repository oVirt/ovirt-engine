package org.ovirt.engine.core.bll.validator;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.network.cluster.ManagementNetworkUtil;
import org.ovirt.engine.core.common.businessentities.IscsiBond;
import org.ovirt.engine.core.common.businessentities.Nameable;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.IscsiBondDao;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.dao.VmTemplateDao;
import org.ovirt.engine.core.dao.network.HostNetworkQosDao;
import org.ovirt.engine.core.dao.network.NetworkDao;
import org.ovirt.engine.core.di.Injector;
import org.ovirt.engine.core.utils.NetworkUtils;
import org.ovirt.engine.core.utils.ReplacementUtils;

/**
 * This class is used to validate different traits of a network on the data center level. <br>
 * <br>
 * Usage: instantiate on a per-network basis, passing the network to be validated as an argument to the constructor.
 */
public class NetworkValidator {

    public static final String NETWORK_LIST_REPLACEMENT = "NETWORK_LIST";

    protected final Network network;

    private List<Network> networks;
    private List<VM> vms;
    private List<VmTemplate> templates;

    public NetworkValidator(Network network) {
        this.network = network;
    }

    /**
     * @return All existing networks in same data center.
     */
    protected List<Network> getNetworks() {
        if (networks == null) {
            networks = getNetworkDao().getAllForDataCenter(network.getDataCenterId());
        }
        return networks;
    }

    /**
     * @return An error iff STP is specified for a non-VM network.
     */
    public ValidationResult stpForVmNetworkOnly() {
        return ValidationResult.failWith(EngineMessage.NON_VM_NETWORK_CANNOT_SUPPORT_STP)
                .unless(network.isVmNetwork() || !network.getStp());
    }

    public ValidationResult portIsolationForVmNetworkOnly() {
        return ValidationResult.failWith(EngineMessage.NON_VM_NETWORK_CANNOT_SUPPORT_PORT_ISOLATION)
                .when(network.isPortIsolation() && !network.isVmNetwork());
    }

    public ValidationResult portIsolationNoExternalNetwork() {
        return ValidationResult.failWith(EngineMessage.EXTERNAL_NETWORK_CANNOT_SUPPORT_PORT_ISOLATION)
                .when(network.isExternal() && network.isPortIsolation());
    }
    /**
     * @return An error iff network is named as if it were a bond.
     */
    public ValidationResult networkPrefixValid() {
        return ValidationResult.failWith(EngineMessage.NETWORK_CANNOT_CONTAIN_BOND_NAME)
                .when(network.getName().toLowerCase().startsWith("bond"));
    }

    /**
     * @return An error iff the network isn't set.
     */
    public ValidationResult networkIsSet(Guid networkId) {
        return networkIsSet(String.valueOf(networkId));
    }

    /**
     * @return An error iff the network isn't set.
     */
    public ValidationResult networkIsSet(String networkId) {
        EngineMessage engineMessage = EngineMessage.NETWORK_HAVING_ID_NOT_EXISTS;
        return ValidationResult.failWith(engineMessage,
            ReplacementUtils.getVariableAssignmentString(engineMessage, networkId))
            .when(network == null);
    }

    /**
     * @return An error if the network's name is already used by another network in the same data center.
     */
    public ValidationResult networkNameNotUsed() {
        for (Network otherNetwork : getNetworks()) {
            if (otherNetwork.getName().equals(network.getName()) &&
                    !otherNetwork.getId().equals(network.getId())) {
                return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_NETWORK_NAME_IN_USE,
                        getNetworkNameReplacement());
            }
        }
        return ValidationResult.VALID;
    }

    public ValidationResult networkNameNotUsedAsVdsmName() {
        String conflictingNetwork = getNetworkDao()
                .getAllForDataCenter(network.getDataCenterId())
                .stream()
                .filter(net -> !net.getId().equals(network.getId()))
                .filter(net -> net.getVdsmName().equals(network.getName()))
                .map(net -> net.getName())
                .findFirst()
                .orElse(null);
        if (conflictingNetwork == null) {
            return ValidationResult.VALID;
        }
        Collection<String> nameReplacements = ReplacementUtils.replaceWith(
                "ConflictingNetwork", singletonList(conflictingNetwork));
        nameReplacements.add(getNetworkNameReplacement());
        return new ValidationResult(EngineMessage.NETWORK_NAME_USED_AS_VDSM_NETWORK_NAME, nameReplacements);
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
            ? new ValidationResult(EngineMessage.NETWORK_CANNOT_REMOVE_MANAGEMENT_NETWORK,
                                          getNetworkNameReplacement())
                                  : ValidationResult.VALID;
    }

    protected NetworkDao getNetworkDao() {
        return Injector.get(NetworkDao.class);
    }

    protected ManagementNetworkUtil getManagementNetworkUtil() {
        return Injector.get(ManagementNetworkUtil.class);
    }

    public ValidationResult notRemovingManagementNetwork() {
        return isManagementNetwork()
            ? new ValidationResult(EngineMessage.NETWORK_CANNOT_REMOVE_MANAGEMENT_NETWORK,
                        getNetworkNameReplacement())
                : ValidationResult.VALID;
    }

    public ValidationResult notIscsiBondNetwork() {
        List<IscsiBond> iscsiBonds = Injector.get(IscsiBondDao.class).getIscsiBondsByNetworkId(network.getId());
        if (!iscsiBonds.isEmpty()) {
            Collection<String> replaceNameables = ReplacementUtils.replaceWithNameable("IscsiBonds", iscsiBonds);
            replaceNameables.add(getNetworkNameReplacement());
            return new ValidationResult(EngineMessage.NETWORK_CANNOT_REMOVE_ISCSI_BOND_NETWORK,
                    replaceNameables);
        }
        return ValidationResult.VALID;
    }

    protected String getNetworkNameReplacement() {
        return String.format("$NetworkName %s", network.getName());
    }

    protected Collection<String> getEntitiesNames(List<? extends Nameable> entities) {
        List<String> result = new ArrayList<>(entities.size());

        for (Nameable itemName : entities) {
            result.add(itemName.getName());
        }

        return result;
    }

    /**
     * @return An error iff the network is in use by any VMs.
     */
    public ValidationResult networkNotUsedByVms() {
        List<VM> vms = getVms();
        return networkNotUsedByVms(getEntitiesNames(vms));
    }

    public ValidationResult networkNotUsedByVms(Collection<String> vmNames) {
        return new PluralMessages(EngineMessage.VAR__ENTITIES__VM, EngineMessage.VAR__ENTITIES__VMS)
            .getNetworkInUse(vmNames);
    }

    /**
     * @return An error iff the network is in use by any hosts.
     */
    public ValidationResult networkNotUsedByHosts() {
        List<VDS> allForNetwork = Injector.get(VdsDao.class).getAllForNetwork(network.getId());
        return new PluralMessages(EngineMessage.VAR__ENTITIES__HOST, EngineMessage.VAR__ENTITIES__HOSTS)
            .getNetworkInUse(getEntitiesNames(allForNetwork));
    }

    /**
     * @return An error iff the network is in use by any templates.
     */
    public ValidationResult networkNotUsedByTemplates() {
        return new PluralMessages(EngineMessage.VAR__ENTITIES__VM_TEMPLATE, EngineMessage.VAR__ENTITIES__VM_TEMPLATES)
            .getNetworkInUse(getEntitiesNames(getTemplates()));
    }

    /**
     * @return An error iff the QoS entity attached to the network isn't null, but doesn't exist in the database or
     *         belongs to the wrong DC.
     */
    public ValidationResult qosExistsInDc() {
        HostNetworkQosValidator qosValidator =
                new HostNetworkQosValidator(Injector.get(HostNetworkQosDao.class).get(network.getQosId()));
        ValidationResult res = qosValidator.qosExists();
        return (res == ValidationResult.VALID) ? qosValidator.consistentDataCenter() : res;
    }

    public ValidationResult notLabeled() {
        return ValidationResult.failWith(EngineMessage.ACTION_TYPE_FAILED_NETWORK_ALREADY_LABELED)
                .when(NetworkUtils.isLabeled(network));
    }

    public ValidationResult notExternalNetwork() {
        return ValidationResult.failWith(EngineMessage.ACTION_TYPE_FAILED_NOT_SUPPORTED_FOR_EXTERNAL_NETWORK)
                .when(network.isExternal());
    }

    public ValidationResult notLinkedToExternalNetwork() {
        List<Network> linkedExternalNetworks =
                getNetworkDao().getAllExternalNetworksLinkedToPhysicalNetwork(network.getId());
        String linkedExternalNetworkNames = linkedExternalNetworks.stream()
                .map(Network::getName)
                .collect(joining(", "));

        return ValidationResult.failWith(EngineMessage.ACTION_TYPE_FAILED_CANNOT_REMOVE_PHYSICAL_NETWORK_LINKED_TO_EXTERNAL_NETWORK,
                ReplacementUtils.createSetVariableString(NETWORK_LIST_REPLACEMENT, linkedExternalNetworkNames))
                .when(!linkedExternalNetworks.isEmpty());
    }

    protected List<VM> getVms() {
        if (vms == null) {
            vms = getVmDao().getAllForNetwork(network.getId());
        }

        return vms;
    }

    protected VmDao getVmDao() {
        return Injector.get(VmDao.class);
    }

    protected List<VmTemplate> getTemplates() {
        if (templates == null) {
            templates = Injector.get(VmTemplateDao.class).getAllForNetwork(network.getId());
        }

        return templates;
    }

    public ValidationResult isVmNetwork() {
        return ValidationResult.failWith(EngineMessage.ACTION_TYPE_FAILED_NOT_A_VM_NETWORK)
                .when(!network.isVmNetwork());
    }

    protected static class PluralMessages {

        private final EngineMessage entitiesReplacementPlural;
        private final EngineMessage entitiesReplacementSingular;

        public PluralMessages(EngineMessage entitiesReplacementSingular, EngineMessage entitiesReplacementPlural) {
            this.entitiesReplacementPlural = entitiesReplacementPlural;
            this.entitiesReplacementSingular = entitiesReplacementSingular;
        }

        /**
         * @param names names of entities using the network
         */
        public ValidationResult getNetworkInUse(Collection<String> names) {
            if (names.isEmpty()) {
                return ValidationResult.VALID;
            }

            int numberOfEntities = names.size();
            boolean useSingular = numberOfEntities == 1;

            return useSingular ? getNetworkInUseSingular(names) : getNetworkInUsePlural(names);


        }

        private ValidationResult getNetworkInUsePlural(Collection<String> names) {
            EngineMessage engineMessage = EngineMessage.ACTION_TYPE_FAILED_NETWORK_IN_MANY_USES;

            List<String> replacements = Stream.concat(
                    ReplacementUtils.getListVariableAssignmentString(engineMessage, names).stream(),
                    Stream.of(entitiesReplacementPlural.name()))
                    .collect(toList());

            return new ValidationResult(engineMessage, replacements);
        }

        private ValidationResult getNetworkInUseSingular(Collection<String> names) {
            String name = names.iterator().next();
            EngineMessage engineMessage = EngineMessage.ACTION_TYPE_FAILED_NETWORK_IN_ONE_USE;

            return new ValidationResult(engineMessage,
                ReplacementUtils.getVariableAssignmentString(engineMessage, name),
                entitiesReplacementSingular.name());
        }

    }
}

package org.ovirt.engine.core.bll.validator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.utils.NetworkCommonUtils;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.NetworkUtils;
import org.ovirt.engine.core.utils.ReplacementUtils;

public class HostInterfaceValidator {

    public static final String VAR_LABELED_NIC = "LabeledNic ";
    public static final String VAR_NIC_LABEL = "NicLabel";
    public static final String VAR_BOND_NAME = "bondName";
    public static final String VAR_ASSIGNED_NETWORKS = "AssignedNetworks";
    public static final String VAR_NIC_NAME = "nicName";
    public static final String VAR_NIC_ID = "nicId";
    private final VdsNetworkInterface iface;

    public HostInterfaceValidator(VdsNetworkInterface iface) {
        this.iface = iface;
    }

    /**
     *
     * @param nicName name of interface to be reported as a inexisting one.
     * @return validation result
     */
    public ValidationResult interfaceExists(String nicName) {
        EngineMessage engineMessage = EngineMessage.HOST_NETWORK_INTERFACE_HAVING_NAME_DOES_NOT_EXIST;
        String nicNameReplacement = ReplacementUtils.getVariableAssignmentString(engineMessage, nicName);
        return ValidationResult.failWith(engineMessage, nicNameReplacement).when(iface == null);
    }

    public ValidationResult interfaceExists(Guid nicId) {
        return ValidationResult.failWith(EngineMessage.HOST_NETWORK_INTERFACE_HAVING_ID_DOES_NOT_EXIST,
            ReplacementUtils.createSetVariableString(VAR_NIC_ID, nicId))
            .when(iface == null);
    }

    public ValidationResult nicIsNotLabeledWithSpecifiedLabel(String label) {

        boolean shouldFail = !NetworkUtils.isLabeled(iface) || !iface.getLabels().contains(label);
        EngineMessage engineMessage = EngineMessage.INTERFACE_NOT_LABELED;
        String nicNameReplacement = ReplacementUtils.getVariableAssignmentString(engineMessage, iface.getName());
        String labelReplacement = ReplacementUtils.createSetVariableString(VAR_NIC_LABEL, label);

        return ValidationResult.failWith(engineMessage, nicNameReplacement, labelReplacement).when(shouldFail);
    }

    public ValidationResult interfaceHasNameSet() {
        return ValidationResult.failWith(EngineMessage.HOST_NETWORK_INTERFACE_DOES_NOT_HAVE_NAME_SET)
                .when(iface.getName() == null);
    }

    public  ValidationResult interfaceAlreadyLabeledWith(String label) {
        return ValidationResult.failWith(EngineMessage.INTERFACE_ALREADY_LABELED,
            ReplacementUtils.createSetVariableString(VAR_LABELED_NIC, iface.getName()),
            ReplacementUtils.createSetVariableString(VAR_NIC_LABEL, label))

                .when(NetworkUtils.isLabeled(iface) && iface.getLabels().contains(label));
    }

    public ValidationResult interfaceInHost(Guid hostId) {
        final EngineMessage engineMessage = EngineMessage.NIC_NOT_EXISTS_ON_HOST;
        return ValidationResult.failWith(engineMessage,
            ReplacementUtils.getVariableAssignmentString(engineMessage, hostId.toString()))
            .when(!iface.getVdsId().equals(hostId));
    }

    public ValidationResult interfaceIsBondOrNull() {
        String ifaceName = iface == null ? "null" : iface.getName();
        final EngineMessage engineMessage = EngineMessage.NETWORK_INTERFACE_IS_NOT_BOND;
        return ValidationResult.failWith(engineMessage,
            ReplacementUtils.getVariableAssignmentString(engineMessage, ifaceName))

            .when(iface != null && !iface.isBond());
    }

    public ValidationResult validBond(List<VdsNetworkInterface> nics) {
        if (!Boolean.TRUE.equals(iface.getBonded())) {
            return ValidationResult.VALID;
        }

        int slavesCount = getSlaveCount(nics);
        String ifaceName = iface.getName();
        return ValidationResult.failWith(EngineMessage.NETWORK_BONDS_INVALID_SLAVE_COUNT,
            ReplacementUtils.getVariableAssignmentString(EngineMessage.NETWORK_BONDS_INVALID_SLAVE_COUNT, ifaceName))
            .when(slavesCount < 2);
    }

    public ValidationResult interfaceIsValidSlave() {
        return ValidationResult.failWith(EngineMessage.NETWORK_INTERFACE_BOND_OR_VLAN_CANNOT_BE_SLAVE,
            ReplacementUtils.createSetVariableString(VAR_NIC_NAME, iface.getName()))

            .when(NetworkCommonUtils.isVlan(iface) || iface.isBond());
    }

    public ValidationResult anotherInterfaceAlreadyLabeledWithThisLabel(String label,
            List<VdsNetworkInterface> interfacesToCheck) {

        for (VdsNetworkInterface nic : interfacesToCheck) {
            // do not compare with self.
            boolean notTheSameNic = !StringUtils.equals(nic.getName(), iface.getName());

            if (notTheSameNic && NetworkUtils.isLabeled(nic) && nic.getLabels().contains(label)) {
                return new ValidationResult(EngineMessage.OTHER_INTERFACE_ALREADY_LABELED,
                        ReplacementUtils.createSetVariableString(VAR_LABELED_NIC, nic.getName()),
                        ReplacementUtils.createSetVariableString(VAR_NIC_LABEL, label));
            }
        }

        return ValidationResult.VALID;
    }

    /**
     * verifies if given (labeled) networks are assigned to any other interface (taken from hostInterfaces),
     * than <code>this.iface</code>. If there's such interface, it's considered as an error.
     */
    public ValidationResult networksAreAttachedToThisInterface(List<VdsNetworkInterface> hostInterfaces,
            List<Network> clusterNetworksWithLabel) {

        List<String> assignedNetworks = validateNetworksNotAssignedToIncorrectNics(hostInterfaces,
                clusterNetworksWithLabel);

        return ValidationResult.failWith(EngineMessage.LABELED_NETWORK_ATTACHED_TO_WRONG_INTERFACE,
            ReplacementUtils.replaceWith(VAR_ASSIGNED_NETWORKS, assignedNetworks))
                .when(!assignedNetworks.isEmpty());
    }

    /**
     * @param hostInterfaces interfaces to check (originally all interfaces for VDS)
     * @param networks networks to check (originally all networks on given cluster with given label).
     * @return list of networks names, which are assigned to one of given <code>hostInterfaces</code> and such interface
     * is not related to <code>this.iface</code> such that neither NIC assigned to network nor it's base interface is
     * <code>this.iface</code>.
     */
    private List<String> validateNetworksNotAssignedToIncorrectNics(List<VdsNetworkInterface> hostInterfaces,
            List<Network> networks) {

        Map<String, VdsNetworkInterface> networkNameToNicMap = NetworkUtils.hostInterfacesByNetworkName(hostInterfaces);
        List<String> badlyAssignedNetworks = new ArrayList<>();

        for (Network network : networks) {
            boolean networkIsAssignedToHostInterface = networkNameToNicMap.containsKey(network.getName());
            if (networkIsAssignedToHostInterface) {
                VdsNetworkInterface assignedHostInterface = networkNameToNicMap.get(network.getName());
                if (!StringUtils.equals(iface.getName(), NetworkCommonUtils.stripVlan(assignedHostInterface))) {
                    badlyAssignedNetworks.add(network.getName());
                }
            }
        }

        return badlyAssignedNetworks;
    }

    private int getSlaveCount(List<VdsNetworkInterface> nics) {
        return getSlaveCount(nics, Integer.MAX_VALUE);
    }

    private int getSlaveCount(List<VdsNetworkInterface> nics, int maxSlavesCount) {
        int slavesCount = 0;

        for (int i = 0; i < nics.size() && slavesCount < maxSlavesCount; i++) {
            VdsNetworkInterface nic = nics.get(i);
            if (StringUtils.equals(iface.getName(), nic.getBondName())) {
                slavesCount++;
            }
        }
        return slavesCount;
    }
}

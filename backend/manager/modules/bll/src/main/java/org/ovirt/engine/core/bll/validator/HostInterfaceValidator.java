package org.ovirt.engine.core.bll.validator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.businessentities.Entities;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.utils.ValidationUtils;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.NetworkUtils;

public class HostInterfaceValidator {

    private final VdsNetworkInterface iface;

    public HostInterfaceValidator(VdsNetworkInterface iface) {
        this.iface = iface;
    }

    public ValidationResult interfaceExists() {
        return ValidationResult.failWith(VdcBllMessages.HOST_NETWORK_INTERFACE_NOT_EXIST).when(iface == null);
    }

    public ValidationResult interfaceByNameExists() {
        return ValidationResult.failWith(VdcBllMessages.HOST_NETWORK_INTERFACE_NOT_EXIST)
                .when(iface == null || iface.getName() == null);
    }

    public  ValidationResult interfaceAlreadyLabeledWith(String label) {
        return ValidationResult.failWith(VdcBllMessages.INTERFACE_ALREADY_LABELED)
                .when(NetworkUtils.isLabeled(iface) && iface.getLabels().contains(label));
    }

    public ValidationResult interfaceInHost(Guid hostId) {
        return ValidationResult.failWith(VdcBllMessages.NIC_NOT_EXISTS_ON_HOST).when(!iface.getVdsId().equals(hostId));
    }

    public ValidationResult interfaceIsBondOrNull() {
        return ValidationResult.failWith(VdcBllMessages.NETWORK_INTERFACE_IS_NOT_BOND)
            .when(iface != null && !iface.isBond());
    }

    /**
     * @param nics existing host interfaces
     * @return Validation result evaluated as: isBond ==> isCorrectBond. If <code>iface</code> is not a bond, validation
     * is successful.
     */
    public ValidationResult labeledValidBond(List<VdsNetworkInterface> nics) {
        if (!Boolean.TRUE.equals(iface.getBonded())) {
            return ValidationResult.VALID;
        }

        return ValidationResult.failWith(VdcBllMessages.IMPROPER_BOND_IS_LABELED).when(getSlaveCount(nics, 2) < 2);
    }

    /**
     * Label validation. Written in way, which tries to reuse javax.validation && existing validator
     * {@link org.ovirt.engine.core.common.validation.annotation.ValidNetworkLabelFormat}.
     * So this method expects, that NIC is valid prior to calling this method thus potential validation fail can be only
     * caused by added label.
     *
     * requires to be called after it's verified that getNic() is at least 2 slaves bond.
     *
     * @param label label to add to NIC.
     * @param commandValidationGroups validationGroups of calling command.
     * @return
     */
    public ValidationResult addLabelToNicAndValidate(String label, List<Class<?>> commandValidationGroups) {
        iface.getLabels().add(label);
        List<String> validationResult = ValidationUtils.validateInputs(commandValidationGroups, iface);
        return ValidationResult
                .failWith(VdcBllMessages.IMPROPER_INTERFACE_IS_LABELED)
                .when(!validationResult.isEmpty());
    }

    public ValidationResult validBond(List<VdsNetworkInterface> nics) {
        if (!Boolean.TRUE.equals(iface.getBonded())) {
            return ValidationResult.VALID;
        }

        int slavesCount = getSlaveCount(nics);
        return ValidationResult.failWith(VdcBllMessages.NETWORK_BONDS_INVALID_SLAVE_COUNT,
                "$NETWORK_BONDS_INVALID_SLAVE_COUNT_LIST " + slavesCount).when(slavesCount < 2);
    }

    public ValidationResult interfaceIsValidSlave() {
        return ValidationResult.failWith(VdcBllMessages.NETWORK_INTERFACE_BOND_OR_VLAN_CANNOT_BE_SLAVE)
            .when(NetworkUtils.isVlan(iface) || iface.isBond());
    }

    public ValidationResult anotherInterfaceAlreadyLabeledWithThisLabel(String label,
            List<VdsNetworkInterface> interfacesToCheck) {

        for (VdsNetworkInterface nic : interfacesToCheck) {
            //do not compare with self.
            boolean notTheSameNic = !StringUtils.equals(nic.getName(), iface.getName());

            if (notTheSameNic) {
                return ValidationResult.failWith(VdcBllMessages.OTHER_INTERFACE_ALREADY_LABELED,
                        "$LabeledNic " + nic.getName())
                        .when(NetworkUtils.isLabeled(nic) && nic.getLabels().contains(label));
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

        return ValidationResult.failWith(VdcBllMessages.LABELED_NETWORK_ATTACHED_TO_WRONG_INTERFACE,
                "$AssignedNetworks " + StringUtils.join(assignedNetworks, ", "))
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

        Map<String, VdsNetworkInterface> networkNameToNicMap = Entities.hostInterfacesByNetworkName(hostInterfaces);
        List<String> badlyAssignedNetworks = new ArrayList<>();

        for (Network network : networks) {
            boolean networkIsAssignedToHostInterface = networkNameToNicMap.containsKey(network.getName());
            if (networkIsAssignedToHostInterface) {
                VdsNetworkInterface assignedHostInterface = networkNameToNicMap.get(network.getName());
                if (!StringUtils.equals(iface.getName(), NetworkUtils.stripVlan(assignedHostInterface))) {
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

    public ValidationResult networkCanBeAttached() {
        return ValidationResult.failWith(VdcBllMessages.CANNOT_ADD_NETWORK_ATTACHMENT_ON_SLAVE_OR_VLAN)
                .when(NetworkUtils.isVlan(iface) || iface.isPartOfBond());
    }

}

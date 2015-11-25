package org.ovirt.engine.core.bll.validator.network;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.utils.ReplacementUtils;

public class DetachNetworkUsedByVmValidator {

    static final String VAR_NETWORK_NAME = "networkName";
    static final String VAR_NETWORK_NAMES = "networkNames";
    static final String VAR_VM_NAME = "vmName";
    static final String VAR_VM_NAMES = "vmNames";

    private final List<String> vmNamesList;
    private final List<String> removedNetworks;

    public DetachNetworkUsedByVmValidator(List<String> vmNamesList, List<String> removedNetworks) {
        this.vmNamesList = vmNamesList;
        this.removedNetworks = removedNetworks;
    }

    public ValidationResult validate() {
        if (vmNamesList.isEmpty()) {
            return ValidationResult.VALID;
        }
        final int vmsAmount = vmNamesList.size();
        final int removedNetworksAmount = removedNetworks.size();
        if (vmsAmount > 1 && removedNetworksAmount > 1) {
            return new ValidationResult(EngineMessage.MULTIPLE_NETWORKS_CANNOT_DETACH_NETWORKS_USED_BY_VMS,
                    Stream.concat(
                            ReplacementUtils.replaceWith(VAR_NETWORK_NAMES, removedNetworks).stream(),
                            ReplacementUtils.replaceWith(VAR_VM_NAMES, vmNamesList).stream()).collect(Collectors.toList()));

        } else if (vmsAmount > 1) {
            return new ValidationResult(EngineMessage.SINGLE_NETWORK_CANNOT_DETACH_NETWORK_USED_BY_VMS,
                    Stream.concat(
                            ReplacementUtils.replaceWith(VAR_NETWORK_NAME, removedNetworks).stream(),
                            ReplacementUtils.replaceWith(VAR_VM_NAMES, vmNamesList).stream()).collect(Collectors.toList()));
        } else if (removedNetworksAmount > 1) {
            return new ValidationResult(EngineMessage.MULTIPLE_NETWORKS_CANNOT_DETACH_NETWORKS_USED_BY_SINGLE_VM,
                    Stream.concat(
                            ReplacementUtils.replaceWith(VAR_NETWORK_NAMES, removedNetworks).stream(),
                            ReplacementUtils.replaceWith(VAR_VM_NAME, vmNamesList).stream()).collect(Collectors.toList()));
        } else {
            return new ValidationResult(EngineMessage.SINGLE_NETWORK_CANNOT_DETACH_NETWORK_USED_BY_SINGLE_VM,
                    Stream.concat(
                            ReplacementUtils.replaceWith(VAR_NETWORK_NAME, removedNetworks).stream(),
                            ReplacementUtils.replaceWith(VAR_VM_NAME, vmNamesList).stream()).collect(Collectors.toList()));
        }
    }
}

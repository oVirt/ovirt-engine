package org.ovirt.engine.core.bll.network.host;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Singleton;

import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.action.HostSetupNetworksParameters;
import org.ovirt.engine.core.common.businessentities.BusinessEntityMap;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkAttachment;
import org.ovirt.engine.core.common.businessentities.network.NicLabel;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.utils.NetworkCommonUtils;
import org.ovirt.engine.core.utils.ReplacementUtils;

/**
 * Validates network changes associated with unmanaged networks
 * The following use cases are validated:
 *
 * Labels:
 * - a label is added to a nic with an unmanaged network
 *
 * Network attachments:
 * - check if a network attachment containing a nic with an unmanaged network is modified/added
 *
 * Removed unmanaged network
 * - check network in removed unmanaged network list is not a managed network on the cluster
 * - check network in removed unmanaged network list is an unmanaged network on the host
 */

@Singleton
public class UnmanagedNetworkValidator {

    static final String NIC = "nic";
    static final String BOND = "bond";
    static final String NETWORK = "network";
    static final String LABEL = "label";

    public UnmanagedNetworkValidator() {
    }

    public ValidationResult validate(HostSetupNetworksParameters params,
            List<VdsNetworkInterface> existingInterfaces,
            BusinessEntityMap<Network> networkBusinessEntityMap){

        ValidationResult result = validateRemovedUnmanagedNetworks(params.getRemovedUnmanagedNetworks(), existingInterfaces, networkBusinessEntityMap);
        if (!result.isValid()) {
            return result;
        }

        Set<String> nicsWithUnmanagedNetworks = filterNicsWithUnmanagedNetworks(
                existingInterfaces, params.getRemovedUnmanagedNetworks());
        for (String nicWithUnmanagedNetwork : nicsWithUnmanagedNetworks){
            result = validateLabels(nicWithUnmanagedNetwork, params.getLabels());
            if (!result.isValid()) {
                return result;
            }
            result = validateAttachements(nicWithUnmanagedNetwork, params.getNetworkAttachments());
            if (!result.isValid()) {
                return result;
            }
        }

        return ValidationResult.VALID;
    }

    protected ValidationResult validateRemovedUnmanagedNetworks(
            Collection<String> removedUnmanagedNetworks,
            Collection<VdsNetworkInterface> existingInterfaces,
            BusinessEntityMap<Network> networkBusinessEntityMap) {

       for (String removedUnmanagedNetworkName : removedUnmanagedNetworks){

            Network network = networkBusinessEntityMap.get(removedUnmanagedNetworkName);
            if (network != null){
                EngineMessage engineMessage = EngineMessage.REMOVED_UNMANAGED_NETWORK_IS_A_CLUSTER_NETWORK;
                return new ValidationResult(engineMessage,
                        ReplacementUtils.createSetVariableString(NETWORK, removedUnmanagedNetworkName));
            }
            ValidationResult result = validateNetworkIsAnUnmanagedNetworkOnHost(removedUnmanagedNetworkName, existingInterfaces);
            if (!result.isValid()){
                return result;
            }
        }
        return ValidationResult.VALID;

    }

    private ValidationResult validateNetworkIsAnUnmanagedNetworkOnHost(
            String removedUnmanagedNetworkName,
            Collection<VdsNetworkInterface> existingInterfaces) {

        for (VdsNetworkInterface existingInterface : existingInterfaces){
            if (removedUnmanagedNetworkName.equals(existingInterface.getNetworkName()) &&
                existingInterface.getNetworkImplementationDetails() != null &&
                !existingInterface.getNetworkImplementationDetails().isManaged()){
                return ValidationResult.VALID;
            }
        }
        return new ValidationResult(EngineMessage.REMOVED_UNMANAGED_NETWORK_DOES_NOT_EXISIT_ON_HOST,
                ReplacementUtils.createSetVariableString(NETWORK, removedUnmanagedNetworkName));
    }

    ValidationResult validateLabels(String nicWithUnmanagedNetwork, Collection<NicLabel> labels) {
        for (NicLabel label : labels){
            if (label.getNicName().equals(nicWithUnmanagedNetwork)) {
                return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_HOST_NETWORK_LABEL_ON_UNMANAGED_NETWORK,
                        ReplacementUtils.createSetVariableString(LABEL, label.getName()),
                        ReplacementUtils.createSetVariableString(NIC, label.getNicName()));
            }
        }
        return ValidationResult.VALID;
    }

    ValidationResult validateAttachements(String nicWithUnmanagedNetwork, List<NetworkAttachment> networkAttachments) {
        for (NetworkAttachment attachement:networkAttachments){
            if (attachement.getNicName().equals(nicWithUnmanagedNetwork)) {
                return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_HOST_NETWORK_ATTACHEMENT_ON_UNMANAGED_NETWORK,
                        ReplacementUtils.createSetVariableString(NETWORK, attachement.getNetworkName()),
                        ReplacementUtils.createSetVariableString(NIC, attachement.getNicName()));
            }
        }
        return ValidationResult.VALID;
    }

    Set<String> filterNicsWithUnmanagedNetworks( List<VdsNetworkInterface> existingInterfaces, Collection<String> removedUnmanagedNetworks){
        Set<String> nicsWithUnmanagedNetworks = new HashSet<>();
        for(VdsNetworkInterface nic : existingInterfaces){
            if(nic.getNetworkImplementationDetails() != null && !nic.getNetworkImplementationDetails().isManaged()) {
                if (!removedUnmanagedNetworks.contains(nic.getNetworkName())) {
                    nicsWithUnmanagedNetworks.add(NetworkCommonUtils.stripVlan(nic));
                }
            }
        }
        return nicsWithUnmanagedNetworks;
    }
}

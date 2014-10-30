package org.ovirt.engine.core.bll.validator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.collections.Bag;
import org.apache.commons.collections.bag.HashBag;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkAttachment;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.NetworkUtils;
import org.ovirt.engine.core.utils.ReplacementUtils;
import org.ovirt.engine.core.utils.collections.MultiValueMapUtils;

/**
 * The {@code NetworkAttachmentsValidator} performs validation on the entire network attachments as a whole and for
 * cross network attachments configuration. For a specific network attachment entity validation use
 * {@link org.ovirt.engine.core.bll.validator.NetworkAttachmentValidator};
 */
public class NetworkAttachmentsValidator {

    private final Collection<NetworkAttachment> attachmentsToConfigure;
    private final Map<Guid, Network> clusterNetworks;

    public NetworkAttachmentsValidator(Collection<NetworkAttachment> attachmentsToConfigure,
            Map<Guid, Network> clusterNetworks) {
        this.attachmentsToConfigure = attachmentsToConfigure;
        this.clusterNetworks = clusterNetworks;
    }

    public ValidationResult validateNetworkExclusiveOnNics() {
        Map<String, List<NetworkType>> nicsToNetworks = new HashMap<>();
        for (NetworkAttachment attachment : attachmentsToConfigure) {
            String nicName = attachment.getNicName();
            if (!nicsToNetworks.containsKey(nicName)) {
                nicsToNetworks.put(nicName, new ArrayList<NetworkType>());
            }

            Network networkToConfigure = clusterNetworks.get(attachment.getNetworkId());
            nicsToNetworks.get(nicName).add(determineNetworkType(networkToConfigure));
        }

        List<String> violatedNics = new ArrayList<>();
        for (Entry<String, List<NetworkType>> nicToNetworkTypes : nicsToNetworks.entrySet()) {
            if (!validateNetworkExclusiveOnNic(nicToNetworkTypes.getKey(), nicToNetworkTypes.getValue())) {
                violatedNics.add(nicToNetworkTypes.getKey());
            }
        }

        if (violatedNics.isEmpty()) {
            return ValidationResult.VALID;
        } else {
            return new ValidationResult(VdcBllMessages.NETWORK_INTERFACES_NOT_EXCLUSIVELY_USED_BY_NETWORK, violatedNics);
        }
    }

    private NetworkType determineNetworkType(Network network) {
        return NetworkUtils.isVlan(network)
                ? NetworkType.VLAN
                : network.isVmNetwork() ? NetworkType.VM : NetworkType.NON_VM;
    }

    /**
     * Make sure that if the given interface has a VM network on it then there is nothing else on the interface, or if
     * the given interface is a VLAN network, than there is no VM network on the interface.<br>
     * Other combinations are either legal or illegal but are not a concern of this method.
     */
    private boolean validateNetworkExclusiveOnNic(String nicName, List<NetworkType> networksOnIface) {
        if (networksOnIface.size() <= 1) {
            return true;
        }

        Bag networkTypes = new HashBag(networksOnIface);
        if (networkTypes.contains(NetworkType.VM) ||
                (networkTypes.contains(NetworkType.NON_VM) && (networkTypes.getCount(NetworkType.NON_VM) > 1))) {
            return false;
        }

        return true;
    }

    public ValidationResult verifyUserAttachmentsDoesNotReferenceSameNetworkDuplicately() {
        Map<String, List<Guid>> networkNameToIdsOfReferencingAttachments = new HashMap<>();
        MultiValueMapUtils.ListCreator<Guid> creator = new MultiValueMapUtils.ListCreator<>();

        for (NetworkAttachment networkAttachment : attachmentsToConfigure) {
            Network network = clusterNetworks.get(networkAttachment.getNetworkId());

            MultiValueMapUtils.addToMap(network.getName(),
                networkAttachment.getId(),
                networkNameToIdsOfReferencingAttachments,
                creator);
        }


        for (Entry<String, List<Guid>> entry : networkNameToIdsOfReferencingAttachments.entrySet()) {
            List<Guid> referencingAttachments = entry.getValue();
            String networkName = entry.getKey();
            if (referencingAttachments.size() > 1) {
                List<String> replacements = new ArrayList<>();
                replacements.addAll(ReplacementUtils.replaceWith(
                    "ACTION_TYPE_FAILED_NETWORK_ATTACHMENTS_REFERENCES_SAME_NETWORK_DUPLICATELY_LIST",
                    referencingAttachments));
                replacements.add(ReplacementUtils.createSetVariableString(
                    "ACTION_TYPE_FAILED_NETWORK_ATTACHMENTS_REFERENCES_SAME_NETWORK_DUPLICATELY_ENTITY", networkName));

                return new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_NETWORK_ATTACHMENTS_REFERENCES_SAME_NETWORK_DUPLICATELY,
                    replacements);
            }
        }

        return ValidationResult.VALID;
    }

    private enum NetworkType {
        VM,
        NON_VM,
        VLAN
    }
}

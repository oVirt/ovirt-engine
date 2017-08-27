package org.ovirt.engine.core.bll.validator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;

import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.validator.network.NetworkExclusivenessValidator;
import org.ovirt.engine.core.bll.validator.network.NetworkType;
import org.ovirt.engine.core.common.businessentities.BusinessEntityMap;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkAttachment;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.NetworkUtils;
import org.ovirt.engine.core.utils.ReplacementUtils;

/**
 * The {@code NetworkAttachmentsValidator} performs validation on the entire network attachments as a whole and for
 * cross network attachments configuration. For a specific network attachment entity validation use
 * {@link org.ovirt.engine.core.bll.validator.NetworkAttachmentValidator};
 */
public class NetworkAttachmentsValidator {

    private static final String LIST_SUFFIX = "_LIST";

    private final Collection<NetworkAttachment> attachmentsToConfigure;
    private final BusinessEntityMap<Network> networkBusinessEntityMap;
    private final NetworkExclusivenessValidator networkExclusivenessValidator;

    public NetworkAttachmentsValidator(Collection<NetworkAttachment> attachmentsToConfigure,
            BusinessEntityMap<Network> networkBusinessEntityMap,
            NetworkExclusivenessValidator networkExclusivenessValidator) {
        Objects.requireNonNull(networkExclusivenessValidator, "networkExclusivenessValidator cannot be null");

        this.attachmentsToConfigure = attachmentsToConfigure;
        this.networkBusinessEntityMap = networkBusinessEntityMap;
        this.networkExclusivenessValidator = networkExclusivenessValidator;
    }

    public ValidationResult validateNetworkExclusiveOnNics() {
        Map<String, List<NetworkType>> nicNameToNetworkTypesMap = createNicNameToNetworkTypesMap();
        List<String> violatedNics = findViolatedNics(nicNameToNetworkTypesMap);

        if (violatedNics.isEmpty()) {
            return ValidationResult.VALID;
        } else {
            final EngineMessage violationMessage = networkExclusivenessValidator.getViolationMessage();
            return new ValidationResult(violationMessage,
                ReplacementUtils.replaceWith(violationMessage + LIST_SUFFIX, violatedNics));
        }
    }

    private List<String> findViolatedNics(Map<String, List<NetworkType>> nicNameToNetworkTypesMap) {
        List<String> violatedNics = new ArrayList<>();
        for (Entry<String, List<NetworkType>> nicNameToNetworkTypes : nicNameToNetworkTypesMap.entrySet()) {
            String nicName = nicNameToNetworkTypes.getKey();
            List<NetworkType> networkTypes = nicNameToNetworkTypes.getValue();
            if (!networkExclusivenessValidator.isNetworkExclusive(networkTypes)) {
                violatedNics.add(nicName);
            }
        }
        return violatedNics;
    }

    private Map<String, List<NetworkType>> createNicNameToNetworkTypesMap() {
        Map<String, List<NetworkType>> nicNameToNetworkTypes = new HashMap<>();
        for (NetworkAttachment attachment : attachmentsToConfigure) {
            String nicName = attachment.getNicName();
            // have to check since if null, multiple results would be merged producing invalid results.
            if (nicName == null) {
                throw new IllegalArgumentException("nic name cannot be null");
            }

            Network networkToConfigure = networkBusinessEntityMap.get(attachment.getNetworkId());
            NetworkType networkTypeToAdd = determineNetworkType(networkToConfigure);

            nicNameToNetworkTypes.computeIfAbsent(nicName, k -> new ArrayList<>()).add(networkTypeToAdd);
        }
        return nicNameToNetworkTypes;
    }

    NetworkType determineNetworkType(Network network) {
        return NetworkUtils.isVlan(network)
                ? NetworkType.VLAN
                : network.isVmNetwork() ? NetworkType.VM : NetworkType.NON_VM;
    }

    public ValidationResult verifyUserAttachmentsDoesNotReferenceSameNetworkDuplicately() {
        Map<String, List<Guid>> networkNameToIdsOfReferencingAttachments =
                attachmentsToConfigure.stream()
                        .collect(Collectors.groupingBy(
                                n -> networkBusinessEntityMap.get(n.getNetworkId()).getName(),
                                Collectors.mapping(NetworkAttachment::getId, Collectors.toList())));

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

                return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_NETWORK_ATTACHMENTS_REFERENCES_SAME_NETWORK_DUPLICATELY,
                    replacements);
            }
        }

        return ValidationResult.VALID;
    }
}

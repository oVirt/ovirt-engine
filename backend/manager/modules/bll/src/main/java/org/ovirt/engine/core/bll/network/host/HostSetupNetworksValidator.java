package org.ovirt.engine.core.bll.network.host;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.Backend;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.network.VmInterfaceManager;
import org.ovirt.engine.core.bll.network.cluster.ManagementNetworkUtil;
import org.ovirt.engine.core.bll.validator.HostInterfaceValidator;
import org.ovirt.engine.core.bll.validator.NetworkAttachmentValidator;
import org.ovirt.engine.core.bll.validator.NetworkAttachmentsValidator;
import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.action.HostSetupNetworksParameters;
import org.ovirt.engine.core.common.businessentities.BusinessEntityMap;
import org.ovirt.engine.core.common.businessentities.Entities;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.network.Bond;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkAttachment;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.utils.customprop.SimpleCustomPropertiesUtil;
import org.ovirt.engine.core.common.utils.customprop.ValidationError;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.NetworkUtils;
import org.ovirt.engine.core.utils.ReplacementUtils;
import org.ovirt.engine.core.utils.collections.MultiValueMapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HostSetupNetworksValidator {
    private static final Logger log = LoggerFactory.getLogger(HostSetupNetworksValidator.class);
    private HostSetupNetworksParameters params;
    private VDS host;
    private Map<String, VdsNetworkInterface> existingIfaces;
    private Map<Guid, VdsNetworkInterface> existingIfacesById;      //TODO MM: will be removed by latter patch.
    private List<NetworkAttachment> existingAttachments;
    private final ManagementNetworkUtil managementNetworkUtil;
    private boolean networkCustomPropertiesSupported;
    private List<VdsNetworkInterface> removedBondVdsNetworkInterface;
    private BusinessEntityMap<VdsNetworkInterface> removedBondVdsNetworkInterfaceMap;
    private List<NetworkAttachment> removedNetworkAttachments;
    private BusinessEntityMap<Network> networkBusinessEntityMap;
    private final Map<Guid, NetworkAttachment> attachmentsById;

    public HostSetupNetworksValidator(VDS host,
        HostSetupNetworksParameters params,
        List<VdsNetworkInterface> existingNics,
        List<NetworkAttachment> existingAttachments,
        BusinessEntityMap<Network> networkBusinessEntityMap,
        ManagementNetworkUtil managementNetworkUtil) {

        this.host = host;
        this.params = params;
        this.existingAttachments = existingAttachments;
        this.managementNetworkUtil = managementNetworkUtil;
        this.existingIfaces = Entities.entitiesByName(existingNics);
        this.existingIfacesById = Entities.businessEntitiesById(existingNics);
        this.networkBusinessEntityMap = networkBusinessEntityMap;

        this.removedBondVdsNetworkInterface = Entities.filterEntitiesByRequiredIds(params.getRemovedBonds(),
            existingNics);
        this.removedBondVdsNetworkInterfaceMap = new BusinessEntityMap<>(removedBondVdsNetworkInterface);
        this.removedNetworkAttachments = Entities.filterEntitiesByRequiredIds(params.getRemovedNetworkAttachments(),
            existingAttachments);

        setSupportedFeatures();

        attachmentsById = Entities.businessEntitiesById(existingAttachments);
    }

    private void setSupportedFeatures() {
        networkCustomPropertiesSupported =
            FeatureSupported.networkCustomProperties(host.getVdsGroupCompatibilityVersion());
    }

    private List<String> translateErrorMessages(List<String> messages) {
        return Backend.getInstance().getErrorsTranslator().TranslateErrorText(messages);
    }

    public ValidationResult validate() {
        Collection<NetworkAttachment> attachmentsToConfigure = getAttachmentsToConfigure();

        ValidationResult vr = ValidationResult.VALID;
        vr = skipValidation(vr) ? vr : validNewOrModifiedNetworkAttachments();
        vr = skipValidation(vr) ? vr : validRemovedNetworkAttachments();
        vr = skipValidation(vr) ? vr : validNewOrModifiedBonds();
        vr = skipValidation(vr) ? vr : validRemovedBonds();
        vr = skipValidation(vr) ? vr : attachmentsDontReferenceSameNetworkDuplicately(attachmentsToConfigure);
        vr = skipValidation(vr) ? vr : networksUniquelyConfiguredOnHost(attachmentsToConfigure);
        vr = skipValidation(vr) ? vr : validateNetworkExclusiveOnNics(attachmentsToConfigure);
        vr = skipValidation(vr) ? vr : validateMtu(attachmentsToConfigure);
        vr = skipValidation(vr) ? vr : validateCustomProperties();

        // TODO: Cover qos change not supported and network sync. see SetupNetworkHelper.validateNetworkQos()
        // Violation - VdcBllMessages.ACTION_TYPE_FAILED_HOST_NETWORK_QOS_NOT_SUPPORTED
        // Violation - VdcBllMessages.NETWORKS_NOT_IN_SYNC

        return vr;
    }

    private ValidationResult attachmentsDontReferenceSameNetworkDuplicately(Collection<NetworkAttachment> attachments) {
        return new NetworkAttachmentsValidator(attachments, networkBusinessEntityMap)
            .verifyUserAttachmentsDoesNotReferenceSameNetworkDuplicately();
    }

    private ValidationResult validateNetworkExclusiveOnNics(Collection<NetworkAttachment> attachmentsToConfigure) {
        NetworkAttachmentsValidator validator =
            new NetworkAttachmentsValidator(attachmentsToConfigure, networkBusinessEntityMap);
        return validator.validateNetworkExclusiveOnNics();
    }

    private ValidationResult networksUniquelyConfiguredOnHost(Collection<NetworkAttachment> attachmentsToConfigure) {
        Set<Guid> networkIds = new HashSet<>(attachmentsToConfigure.size());
        for (NetworkAttachment attachment : attachmentsToConfigure) {
            if (networkIds.contains(attachment.getNetworkId())) {
                return new ValidationResult(VdcBllMessages.NETWORKS_ALREADY_ATTACHED_TO_IFACES);
            } else {
                networkIds.add(attachment.getNetworkId());
            }
        }

        return ValidationResult.VALID;
    }

    private ValidationResult validateNotRemovingUsedNetworkByVms() {
        Collection<String> removedNetworks = new HashSet<>();
        for (NetworkAttachment removedAttachment : removedNetworkAttachments) {
            removedNetworks.add(existingNetworkRelatedToAttachment(removedAttachment).getName());
        }

        List<String> vmNames = getVmInterfaceManager().findActiveVmsUsingNetworks(host.getId(), removedNetworks);
        if (vmNames.isEmpty()) {
            return ValidationResult.VALID;
        } else {
            return new ValidationResult(VdcBllMessages.NETWORK_CANNOT_DETACH_NETWORK_USED_BY_VMS,
                commaSeparated(vmNames));
        }
    }

    private ValidationResult validRemovedBonds() {
        List<Guid> invalidBondIds = Entities.idsNotReferencingExistingRecords(params.getRemovedBonds(),
            existingIfacesById);
        if (!invalidBondIds.isEmpty()) {
            return new ValidationResult(VdcBllMessages.NETWORK_BOND_NOT_EXISTS, commaSeparated(invalidBondIds));
        }

        Set<String> requiredNicsNames = getRemovedBondsUsedByNetworks();

        for (VdsNetworkInterface removedBond : removedBondVdsNetworkInterface) {
            String bondName = removedBond.getName();
            VdsNetworkInterface existingBond = existingIfaces.get(bondName);
            ValidationResult interfaceIsBondOrNull = new HostInterfaceValidator(existingBond).interfaceIsBondOrNull();
            if (!interfaceIsBondOrNull.isValid()) {
                return interfaceIsBondOrNull;
            }

            if (requiredNicsNames.contains(bondName)) {
                return new ValidationResult(VdcBllMessages.BOND_USED_BY_NETWORK_ATTACHMENTS, bondName);
            }
        }

        return ValidationResult.VALID;
    }

    private Set<String> getRemovedBondsUsedByNetworks() {
        Collection<NetworkAttachment> attachmentsToConfigure = getAttachmentsToConfigure();
        Set<String> requiredNicsNames = new HashSet<>();
        for (NetworkAttachment attachment : attachmentsToConfigure) {
            requiredNicsNames.add(attachment.getNicName());
        }

        return requiredNicsNames;
    }

    private Collection<NetworkAttachment> getAttachmentsToConfigure() {
        Map<Guid, NetworkAttachment> attachmentsToConfigure = new HashMap<>(Entities.businessEntitiesById(
            existingAttachments));
        // ignore removed attachments
        for (NetworkAttachment removedAttachment : removedNetworkAttachments) {
            attachmentsToConfigure.remove(removedAttachment.getId());
        }

        List<NetworkAttachment> newAttachments = new ArrayList<>();

        // add attachments which planned to be configured on removed bonds and ignore those which aren't
        for (NetworkAttachment attachment : params.getNetworkAttachments()) {

            // new attachment to add
            if (attachment.getId() == null) {
                newAttachments.add(attachment);
                continue;
            }

            if (removedBondVdsNetworkInterfaceMap.containsKey(attachment.getNicName())) {
                attachmentsToConfigure.put(attachment.getId(), attachment);
            } else {
                attachmentsToConfigure.remove(attachment.getId());
            }
        }

        Collection<NetworkAttachment> candidateAttachments = new ArrayList<>(attachmentsToConfigure.values());
        candidateAttachments.addAll(newAttachments);
        return candidateAttachments;
    }

    private ValidationResult validNewOrModifiedBonds() {
        for (Bond modifiedOrNewBond : params.getBonds()) {
            String bondName = modifiedOrNewBond.getName();
            ValidationResult validateCoherentNicIdentification = validateCoherentNicIdentification(modifiedOrNewBond);
            if (!validateCoherentNicIdentification.isValid()) {
                return validateCoherentNicIdentification;
            }

            ValidationResult interfaceByNameExists = new HostInterfaceValidator(modifiedOrNewBond).interfaceByNameExists();
            if (!interfaceByNameExists.isValid()) {
                return interfaceByNameExists;
            }

            //either it's newly create bond, thus non existing, or given name must reference existing bond.
            ValidationResult interfaceIsBondOrNull = new HostInterfaceValidator(existingIfaces.get(bondName)).interfaceIsBondOrNull();
            if (!interfaceIsBondOrNull.isValid()) {
                return interfaceIsBondOrNull;
            }

            //count of bond slaves must be at least two.
            if (modifiedOrNewBond.getSlaves().size() < 2) {
                return new ValidationResult(VdcBllMessages.NETWORK_BONDS_INVALID_SLAVE_COUNT, bondName);
            }

            for (String slaveName : modifiedOrNewBond.getSlaves()) {
                VdsNetworkInterface potentialSlave = getExistingIfaces().get(slaveName);
                HostInterfaceValidator slaveHostInterfaceValidator = new HostInterfaceValidator(potentialSlave);

                ValidationResult interfaceExists = slaveHostInterfaceValidator.interfaceExists();
                if (!interfaceExists.isValid()) {
                    return interfaceExists;
                }

                ValidationResult interfaceIsValidSlave = slaveHostInterfaceValidator.interfaceIsValidSlave();
                if (!interfaceIsValidSlave.isValid()) {
                    return interfaceIsValidSlave;
                }

                /* definition of currently processed bond references this slave, but this slave already 'slaves' for
                another bond. This is ok only when this bond will be removed as a part of this request
                or the slave will be removed from its former bond, as a part of this request. */
                String currentSlavesBondName = potentialSlave.getBondName();
                if (potentialSlave.isPartOfBond() &&
                        /* we're creating new bond, and it's definition contains reference to slave already assigned
                        to a different bond. */
                    (!potentialSlave.isPartOfBond(bondName)
                        //…but this bond is also removed in this request, so it's ok.
                        && !isBondRemoved(currentSlavesBondName)

                        //… or slave was removed from its former bond
                        && !bondIsUpdatedAndDoesNotContainCertainSlave(slaveName, currentSlavesBondName))) {
                    return new ValidationResult(VdcBllMessages.NETWORK_INTERFACE_ALREADY_IN_BOND, slaveName);
                }

                if (slaveUsedMultipleTimesInDifferentBonds(slaveName)) {
                    return new ValidationResult(VdcBllMessages.NETWORK_INTERFACE_REFERENCED_AS_A_SLAVE_MULTIPLE_TIMES,
                        ReplacementUtils.createSetVariableString(
                            "NETWORK_INTERFACE_REFERENCED_AS_A_SLAVE_MULTIPLE_TIMES_ENTITY",
                            slaveName));
                }

                /* slave has network assigned and there isn't request for unassigning it;
                so this check, that nic is part of newly crated bond, and any previously attached network has
                to be unattached. */
                if (potentialSlave.getNetworkName() != null && !isNetworkAttachmentRemoved(potentialSlave)) {
                    return new ValidationResult(VdcBllMessages.NETWORK_INTERFACE_ATTACHED_TO_NETWORK_CANNOT_BE_SLAVE,
                        potentialSlave.getName());
                }
            }
        }

        return ValidationResult.VALID;
    }

    private boolean slaveUsedMultipleTimesInDifferentBonds(String potentiallyDuplicateSlaveName) {
        int count = 0;
        for (Bond bond : params.getBonds()) {
            for (String slaveName : bond.getSlaves()) {
                if (slaveName.equals(potentiallyDuplicateSlaveName)) {
                    count++;
                }
            }
        }

        return count >= 2;
    }

    /**
     * @return true if there's network attachment related to given nic, which gets removed upon request.
     */
    private boolean isNetworkAttachmentRemoved(VdsNetworkInterface nic) {
        for (NetworkAttachment attachment : removedNetworkAttachments) {
            if (Objects.equals(nic.getName(), attachment.getNicName())) {
                return true;
            }
        }

        return false;
    }

    /**
     * looks into new/modified bonds for bond of given name, whether it contains certain slave.
     *
     * @param slaveName slave which should not be present
     * @param bondName name of bond we're examining
     *
     * @return true if bond specified by name is present in request and does not contain given slave.
     */
    private boolean bondIsUpdatedAndDoesNotContainCertainSlave(String slaveName, String bondName) {
        for (Bond bond : params.getBonds()) {
            boolean isRequiredBond = bond.getName().equals(bondName);
            if (isRequiredBond) {
                boolean updatedBondDoesNotContainGivenSlave = !bond.getSlaves().contains(slaveName);
                return updatedBondDoesNotContainGivenSlave;
            }
        }

        return false;
    }

    private boolean isBondRemoved(String bondName) {
        for (VdsNetworkInterface removedBond : removedBondVdsNetworkInterface) {
            if (bondName.equals(removedBond.getName())) {
                return true;
            }
        }

        return false;
    }

    private ValidationResult validNewOrModifiedNetworkAttachments() {
        ValidationResult vr = ValidationResult.VALID;

        Iterator<NetworkAttachment> iterator = params.getNetworkAttachments().iterator();
        while (iterator.hasNext() && vr.isValid()) {
            NetworkAttachment attachment = iterator.next();
            NetworkAttachmentValidator validator =
                new NetworkAttachmentValidator(attachment, host, managementNetworkUtil);


            vr = skipValidation(vr) ? vr : validator.networkAttachmentIsSet();

            //TODO MM: complain about unset network id.
            vr = skipValidation(vr) ? vr : validator.networkExists();
            vr = skipValidation(vr) ? vr : validateCoherentNicIdentification(attachment);
            vr = skipValidation(vr) ? vr : modifiedAttachmentExists(attachment.getId());
            vr = skipValidation(vr) ? vr : validator.notExternalNetwork();
            vr = skipValidation(vr) ? vr : validator.networkAttachedToCluster();
            vr = skipValidation(vr) ? vr : validator.ipConfiguredForStaticBootProtocol();
            vr = skipValidation(vr) ? vr : validator.bootProtocolSetForDisplayNetwork();
            vr = skipValidation(vr) ? vr : validator.nicExists();
            vr = skipValidation(vr) ? vr : validator.networkIpAddressWasSameAsHostnameAndChanged(getExistingIfaces());
            vr = skipValidation(vr) ? vr : validator.networkNotChanged(attachmentsById.get(attachment.getId()));
            vr = skipValidation(vr) ? vr : validator.validateGateway();
        }

        return vr;
    }

    private ValidationResult validateCoherentNicIdentification(NetworkAttachment attachment) {
        return validateCoherentNicIdentification(attachment.getId(),
            attachment.getNicId(),
            attachment.getNicName(),
            VdcBllMessages.NETWORK_ATTACHMENT_REFERENCES_NICS_INCOHERENTLY);
    }

    private ValidationResult validateCoherentNicIdentification(Bond bond) {
        Guid nicId = bond.getId();
        String nicName = bond.getName();
        VdcBllMessages message = VdcBllMessages.BOND_REFERENCES_NICS_INCOHERENTLY;
        return validateCoherentNicIdentification(bond.getId(), nicId, nicName, message);

    }

    private ValidationResult validateCoherentNicIdentification(Guid violatingEntityId,
        Guid nicId,
        String nicName,
        VdcBllMessages message) {

        boolean bothIdentificationSet = nicId != null && nicName != null;
        String[] replacements = createIncoherentNicIdentificationErrorReplacements(violatingEntityId, nicId, nicName);
        return ValidationResult
            .failWith(message,
                replacements)
            .when(bothIdentificationSet && isNicNameAndNicIdIncoherent(nicId, nicName));
    }

    private String[] createIncoherentNicIdentificationErrorReplacements(Guid violatingEntityId,
        Guid nicId,
        String nicName) {
        return new String[] {
            String.format("ENTITY_ID %s", violatingEntityId),
            String.format("$nicId %s", nicId),
            String.format("$nicName %s", nicName)
        };
    }

    private boolean isNicNameAndNicIdIncoherent(Guid nicId, String nicName) {
        VdsNetworkInterface interfaceById = existingIfacesById.get(nicId);
        VdsNetworkInterface interfaceByName = existingIfaces.get(nicName);
        return !Objects.equals(interfaceById, interfaceByName);
    }

    private ValidationResult modifiedAttachmentExists(Guid networkAttachmentId) {
        boolean doesNotReferenceExistingNetworkAttachment = networkAttachmentId == null;
        if (doesNotReferenceExistingNetworkAttachment) {
            return ValidationResult.VALID;
        }

        for (NetworkAttachment existingAttachment : existingAttachments) {
            if (existingAttachment.getId().equals(networkAttachmentId)) {
                return ValidationResult.VALID;
            }
        }

        return new ValidationResult(VdcBllMessages.NETWORK_ATTACHMENT_NOT_EXISTS);
    }

    private ValidationResult validRemovedNetworkAttachments() {
        List<Guid> invalidIds = Entities.idsNotReferencingExistingRecords(params.getRemovedNetworkAttachments(),
            existingAttachments);
        if (!invalidIds.isEmpty()) {
            return new ValidationResult(VdcBllMessages.NETWORK_ATTACHMENT_NOT_EXISTS, commaSeparated(invalidIds));
        }

        ValidationResult vr = ValidationResult.VALID;
        Iterator<NetworkAttachment> iterator = removedNetworkAttachments.iterator();
        while (iterator.hasNext() && vr.isValid()) {
            NetworkAttachment attachment = iterator.next();
            NetworkAttachment attachmentToValidate = attachmentsById.get(attachment.getId());
            NetworkAttachmentValidator validator =
                new NetworkAttachmentValidator(attachmentToValidate, host, managementNetworkUtil);

            vr = skipValidation(vr) ? vr : validator.notExternalNetwork();
            vr = skipValidation(vr) ? vr : validator.notManagementNetwork();
            vr = skipValidation(vr) ? vr : notRemovingLabeledNetworks(attachment, getExistingIfaces());
            vr = skipValidation(vr) ? vr : validateNotRemovingUsedNetworkByVms();
        }

        return vr;
    }

    private ValidationResult notRemovingLabeledNetworks(NetworkAttachment attachment,
        Map<String, VdsNetworkInterface> existingNics) {
        Network removedNetwork = existingNetworkRelatedToAttachment(attachment);
        if (!NetworkUtils.isLabeled(removedNetwork)) {
            return ValidationResult.VALID;
        }

        VdsNetworkInterface nic = existingNics.get(attachment.getNicName());
        if (nic != null && !removedBondVdsNetworkInterfaceMap.containsKey(nic.getName())) {
            if (NetworkUtils.isLabeled(nic) && nic.getLabels().contains(removedNetwork.getLabel())) {
                return new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_CANNOT_REMOVE_LABELED_NETWORK_FROM_NIC,
                    removedNetwork.getName());
            }
        }

        return ValidationResult.VALID;
    }

    /**
     * Validates there is no differences on MTU value between non-VM network to Vlans over the same interface/bond
     */
    private ValidationResult validateMtu(Collection<NetworkAttachment> attachmentsToConfigure) {
        Map<String, List<Network>> nicsToNetworks = getNicNameToNetworksMap(attachmentsToConfigure);

        for (Entry<String, List<Network>> nicToNetworks : nicsToNetworks.entrySet()) {
            List<Network> networksOnNic = nicToNetworks.getValue();
            if (!networksOnNicMatchMtu(networksOnNic)) {
                ValidationResult validationResult = reportMtuDifferences(networksOnNic);
                if (!validationResult.isValid()) {
                    return validationResult;
                }
            }
        }

        return ValidationResult.VALID;
    }

    private boolean networksOnNicMatchMtu(List<Network> networksOnNic) {
        Set<String> checkNetworks = new HashSet<>(networksOnNic.size());

        for (Network networkOnNic : networksOnNic) {
            for (Network otherNetworkOnNic : networksOnNic) {
                if (!checkNetworks.contains(networkOnNic.getName())
                    && networkOnNic.getMtu() != otherNetworkOnNic.getMtu()
                    && (NetworkUtils.isNonVmNonVlanNetwork(networkOnNic)
                    || NetworkUtils.isNonVmNonVlanNetwork(otherNetworkOnNic))) {
                    return false;
                }
            }

            checkNetworks.add(networkOnNic.getName());
        }

        return true;
    }

    private Map<String, List<Network>> getNicNameToNetworksMap(Collection<NetworkAttachment> attachmentsToConfigure) {
        Map<String, List<Network>> nicNameToNetworksMap = new HashMap<>();
        for (NetworkAttachment attachment : attachmentsToConfigure) {
            String mapKey = attachment.getNicName();
            Network networkToConfigure = existingNetworkRelatedToAttachment(attachment);

            MultiValueMapUtils.addToMap(mapKey,
                networkToConfigure,
                nicNameToNetworksMap,
                new MultiValueMapUtils.ListCreator<Network>());
        }

        return nicNameToNetworksMap;
    }

    private ValidationResult reportMtuDifferences(List<Network> ifaceNetworks) {
        List<String> mtuDiffNetworks = new ArrayList<>();
        for (Network net : ifaceNetworks) {
            mtuDiffNetworks.add(String.format("%s(%s)",
                net.getName(),
                net.getMtu() == 0 ? "default" : String.valueOf(net.getMtu())));
        }
        String replacements = String.format("[%s]", commaSeparated(mtuDiffNetworks));
        return new ValidationResult(VdcBllMessages.NETWORK_MTU_DIFFERENCES, replacements);
    }

    private ValidationResult validateCustomProperties() {
        String version = host.getVdsGroupCompatibilityVersion().getValue();
        SimpleCustomPropertiesUtil util = SimpleCustomPropertiesUtil.getInstance();
        Map<String, String> validProperties =
            util.convertProperties(Config.<String> getValue(ConfigValues.PreDefinedNetworkCustomProperties, version));
        validProperties.putAll(util.convertProperties(Config.<String> getValue(ConfigValues.UserDefinedNetworkCustomProperties,
            version)));
        Map<String, String> validPropertiesNonVm = new HashMap<>(validProperties);
        validPropertiesNonVm.remove("bridge_opts");
        for (NetworkAttachment attachment : params.getNetworkAttachments()) {
            Network network = existingNetworkRelatedToAttachment(attachment);
            if (attachment.hasProperties()) {
                if (!networkCustomPropertiesSupported) {
                    return new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_NETWORK_CUSTOM_PROPERTIES_NOT_SUPPORTED,
                        network.getName());
                }

                List<ValidationError> errors =
                    util.validateProperties(network.isVmNetwork() ? validProperties : validPropertiesNonVm,
                        attachment.getProperties());
                if (!errors.isEmpty()) {
                    handleCustomPropertiesError(util, errors);
                    return new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_NETWORK_CUSTOM_PROPERTIES_BAD_INPUT,
                        network.getName());
                }
            }
        }

        return ValidationResult.VALID;
    }

    private void handleCustomPropertiesError(SimpleCustomPropertiesUtil util, List<ValidationError> errors) {
        List<String> messages = new ArrayList<>();
        util.handleCustomPropertiesError(errors, messages);
        log.error(StringUtils.join(translateErrorMessages(messages), ','));
    }



    private Network existingNetworkRelatedToAttachment(NetworkAttachment attachment) {
        return networkBusinessEntityMap.get(attachment.getNetworkId());
    }

    private Map<String, VdsNetworkInterface> getExistingIfaces() {
        return existingIfaces;
    }

    public VmInterfaceManager getVmInterfaceManager() {
        return new VmInterfaceManager();
    }

    private boolean skipValidation(ValidationResult validationResult) {
        return !validationResult.isValid();
    }

    private String commaSeparated(List<?> invalidBondIds) {
        return StringUtils.join(invalidBondIds, ", ");
    }
}

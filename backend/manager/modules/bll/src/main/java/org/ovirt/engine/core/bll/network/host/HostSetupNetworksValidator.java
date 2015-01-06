package org.ovirt.engine.core.bll.network.host;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.utils.customprop.SimpleCustomPropertiesUtil;
import org.ovirt.engine.core.common.utils.customprop.ValidationError;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.dao.network.NetworkAttachmentDao;
import org.ovirt.engine.core.dao.network.NetworkClusterDao;
import org.ovirt.engine.core.dao.network.NetworkDao;
import org.ovirt.engine.core.utils.NetworkUtils;
import org.ovirt.engine.core.utils.ReplacementUtils;
import org.ovirt.engine.core.utils.collections.MultiValueMapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HostSetupNetworksValidator {
    private static final Logger log = LoggerFactory.getLogger(HostSetupNetworksValidator.class);

    static final String ACTION_TYPE_FAILED_CANNOT_MOVE_LABELED_NETWORK_TO_ANOTHER_NIC_ENTITY = "ACTION_TYPE_FAILED_CANNOT_MOVE_LABELED_NETWORK_TO_ANOTHER_NIC_ENTITY";

    private HostSetupNetworksParameters params;
    private VDS host;
    private Map<String, VdsNetworkInterface> existingInterfaces;
    private Map<Guid, VdsNetworkInterface> existingIfacesById;      //TODO MM: will be removed by latter patch.
    private List<NetworkAttachment> existingAttachments;
    private final ManagementNetworkUtil managementNetworkUtil;
    private boolean networkCustomPropertiesSupported;
    private List<VdsNetworkInterface> removedBondVdsNetworkInterface;
    private BusinessEntityMap<VdsNetworkInterface> removedBondVdsNetworkInterfaceMap;
    private List<NetworkAttachment> removedNetworkAttachments;
    private BusinessEntityMap<Network> networkBusinessEntityMap;
    private final Map<Guid, NetworkAttachment> attachmentsById;
    private final NetworkClusterDao networkClusterDao;
    private final NetworkAttachmentDao networkAttachmentDao;
    private final NetworkDao networkDao;
    private final VdsDao vdsDao;

    public HostSetupNetworksValidator(VDS host,
        HostSetupNetworksParameters params,
        List<VdsNetworkInterface> existingInterfaces,
        List<NetworkAttachment> existingAttachments,
        BusinessEntityMap<Network> networkBusinessEntityMap,
        ManagementNetworkUtil managementNetworkUtil,
        NetworkClusterDao networkClusterDao,
        NetworkAttachmentDao networkAttachmentDao,
        NetworkDao networkDao,
        VdsDao vdsDao) {

        this.host = host;
        this.params = params;
        this.existingAttachments = existingAttachments;
        this.managementNetworkUtil = managementNetworkUtil;
        this.networkClusterDao = networkClusterDao;
        this.networkAttachmentDao = networkAttachmentDao;
        this.networkDao = networkDao;
        this.vdsDao = vdsDao;
        this.existingInterfaces = Entities.entitiesByName(existingInterfaces);
        this.existingIfacesById = Entities.businessEntitiesById(existingInterfaces);
        this.networkBusinessEntityMap = networkBusinessEntityMap;

        this.removedBondVdsNetworkInterface = Entities.filterEntitiesByRequiredIds(params.getRemovedBonds(),
            existingInterfaces);
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

    List<String> translateErrorMessages(List<String> messages) {
        return Backend.getInstance().getErrorsTranslator().TranslateErrorText(messages);
    }

    public ValidationResult validate() {
        Collection<NetworkAttachment> attachmentsToConfigure = getAttachmentsToConfigure();

        ValidationResult vr = ValidationResult.VALID;
        vr = skipValidation(vr) ? vr : validNewOrModifiedNetworkAttachments();
        vr = skipValidation(vr) ? vr : validRemovedNetworkAttachments();
        vr = skipValidation(vr) ? vr : validNewOrModifiedBonds();
        vr = skipValidation(vr) ? vr : validRemovedBonds(attachmentsToConfigure);
        vr = skipValidation(vr) ? vr : attachmentsDontReferenceSameNetworkDuplicately(attachmentsToConfigure);
        vr = skipValidation(vr) ? vr : networksUniquelyConfiguredOnHost(attachmentsToConfigure);
        vr = skipValidation(vr) ? vr : validateNetworkExclusiveOnNics(attachmentsToConfigure);
        vr = skipValidation(vr) ? vr : validateMtu(attachmentsToConfigure);
        vr = skipValidation(vr) ? vr : validateCustomProperties();

        // TODO: Cover qos change not supported and network sync. see SetupNetworkHelper.validateNetworkQos()
        // Violation - EngineMessage.ACTION_TYPE_FAILED_HOST_NETWORK_QOS_NOT_SUPPORTED
        // Violation - EngineMessage.NETWORKS_NOT_IN_SYNC

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

    ValidationResult networksUniquelyConfiguredOnHost(Collection<NetworkAttachment> attachmentsToConfigure) {
        Set<Guid> usedNetworkIds = new HashSet<>(attachmentsToConfigure.size());
        for (NetworkAttachment attachment : attachmentsToConfigure) {
            boolean alreadyUsedNetworkId = usedNetworkIds.contains(attachment.getNetworkId());
            if (alreadyUsedNetworkId) {
                return new ValidationResult(EngineMessage.NETWORKS_ALREADY_ATTACHED_TO_IFACES);
            } else {
                usedNetworkIds.add(attachment.getNetworkId());
            }
        }

        return ValidationResult.VALID;
    }

    ValidationResult validateNotRemovingUsedNetworkByVms() {
        Collection<String> removedNetworks = new HashSet<>();
        for (NetworkAttachment removedAttachment : removedNetworkAttachments) {
            removedNetworks.add(existingNetworkRelatedToAttachment(removedAttachment).getName());
        }

        List<String> vmNames = getVmInterfaceManager().findActiveVmsUsingNetworks(host.getId(), removedNetworks);
        if (vmNames.isEmpty()) {
            return ValidationResult.VALID;
        } else {
            return new ValidationResult(EngineMessage.NETWORK_CANNOT_DETACH_NETWORK_USED_BY_VMS,
                commaSeparated(vmNames));
        }
    }

    ValidationResult validRemovedBonds(Collection<NetworkAttachment> attachmentsToConfigure) {
        List<Guid> invalidBondIds = Entities.idsNotReferencingExistingRecords(params.getRemovedBonds(),
            existingIfacesById);
        if (!invalidBondIds.isEmpty()) {
            return new ValidationResult(EngineMessage.NETWORK_BOND_NOT_EXISTS, commaSeparated(invalidBondIds));
        }

        Set<String> requiredInterfaceNames = getNetworkAttachmentInterfaceNames(attachmentsToConfigure);

        for (VdsNetworkInterface removedBond : removedBondVdsNetworkInterface) {
            String bondName = removedBond.getName();
            VdsNetworkInterface existingBond = existingInterfaces.get(bondName);
            ValidationResult interfaceIsBondOrNull = createHostInterfaceValidator(existingBond).interfaceIsBondOrNull();
            if (!interfaceIsBondOrNull.isValid()) {
                return interfaceIsBondOrNull;
            }

            boolean cantRemoveRequiredInterface = requiredInterfaceNames.contains(bondName);
            if (cantRemoveRequiredInterface) {
                return new ValidationResult(EngineMessage.BOND_USED_BY_NETWORK_ATTACHMENTS, bondName);
            }
        }

        return ValidationResult.VALID;
    }

    private Set<String> getNetworkAttachmentInterfaceNames(Collection<NetworkAttachment> networkAttachments) {
        Set<String> networkAttachmentNicNames = new HashSet<>();
        for (NetworkAttachment attachment : networkAttachments) {
            networkAttachmentNicNames.add(attachment.getNicName());
        }

        return networkAttachmentNicNames;
    }

    /**
     * @return all attachments passed in {@link HostSetupNetworksParameters#networkAttachments} plus
     * all previously existing attachments not mentioned in user request, but except for those listed in
     * {@link org.ovirt.engine.core.common.action.HostSetupNetworksParameters#removedNetworkAttachments}
     */
    Collection<NetworkAttachment> getAttachmentsToConfigure() {
        Map<Guid, NetworkAttachment> networkAttachmentsMap = new HashMap<>(
            existingAttachments.size() + params.getNetworkAttachments().size());

        List<NetworkAttachment> newAttachments = new ArrayList<>();

        for (NetworkAttachment attachment : params.getNetworkAttachments()) {
            if (attachment.getId() == null) {
                newAttachments.add(attachment);
                continue;
            } else {
                networkAttachmentsMap.put(attachment.getId(), attachment);
            }
        }

        Map<Guid, NetworkAttachment> removedNetworkAttachments =
            Entities.businessEntitiesById(this.removedNetworkAttachments);
        for (NetworkAttachment existingAttachment : existingAttachments) {
            Guid existingAttachmentId = existingAttachment.getId();
            if (!networkAttachmentsMap.containsKey(existingAttachmentId) &&
                !removedNetworkAttachments.containsKey(existingAttachmentId)) {
                networkAttachmentsMap.put(existingAttachmentId, existingAttachment);
            }
        }

        List<NetworkAttachment> result = new ArrayList<>(networkAttachmentsMap.values());
        result.addAll(newAttachments);
        return result;
    }

    ValidationResult validNewOrModifiedBonds() {
        for (Bond modifiedOrNewBond : params.getBonds()) {
            String bondName = modifiedOrNewBond.getName();
            ValidationResult validateCoherentNicIdentification = validateCoherentNicIdentification(modifiedOrNewBond);
            if (!validateCoherentNicIdentification.isValid()) {
                return validateCoherentNicIdentification;
            }

            ValidationResult interfaceByNameExists = createHostInterfaceValidator(modifiedOrNewBond).interfaceByNameExists();
            if (!interfaceByNameExists.isValid()) {
                return interfaceByNameExists;
            }

            //either it's newly create bond, thus non existing, or given name must reference existing bond.
            ValidationResult interfaceIsBondOrNull = createHostInterfaceValidator(existingInterfaces.get(bondName)).interfaceIsBondOrNull();
            if (!interfaceIsBondOrNull.isValid()) {
                return interfaceIsBondOrNull;
            }

            //count of bond slaves must be at least two.
            if (modifiedOrNewBond.getSlaves().size() < 2) {
                return new ValidationResult(EngineMessage.NETWORK_BONDS_INVALID_SLAVE_COUNT, bondName);
            }

            ValidationResult validateModifiedBondSlaves = validateModifiedBondSlaves(modifiedOrNewBond);
            if (!validateModifiedBondSlaves.isValid()) {
                return validateModifiedBondSlaves;
            }
        }

        return ValidationResult.VALID;
    }

    ValidationResult validateModifiedBondSlaves(Bond modifiedOrNewBond) {
        for (String slaveName : modifiedOrNewBond.getSlaves()) {
            VdsNetworkInterface potentialSlave = existingInterfaces.get(slaveName);
            HostInterfaceValidator slaveHostInterfaceValidator = createHostInterfaceValidator(potentialSlave);

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
                (!potentialSlave.isPartOfBond(modifiedOrNewBond.getName())
                    //…but this bond is also removed in this request, so it's ok.
                    && !isBondRemoved(currentSlavesBondName)

                    //… or slave was removed from its former bond
                    && !bondIsUpdatedAndDoesNotContainCertainSlave(slaveName, currentSlavesBondName))) {
                return new ValidationResult(EngineMessage.NETWORK_INTERFACE_ALREADY_IN_BOND, slaveName);
            }

                if (slaveUsedMultipleTimesInDifferentBonds(slaveName)) {
                    return new ValidationResult(EngineMessage.NETWORK_INTERFACE_REFERENCED_AS_A_SLAVE_MULTIPLE_TIMES,
                        ReplacementUtils.createSetVariableString(
                            "NETWORK_INTERFACE_REFERENCED_AS_A_SLAVE_MULTIPLE_TIMES_ENTITY",
                            slaveName));
                }

                /* slave has network assigned and there isn't request for unassigning it;
                so this check, that nic is part of newly crated bond, and any previously attached network has
                to be unattached. */
            if (potentialSlave.getNetworkName() != null && !isNetworkAttachmentRemoved(potentialSlave)) {
                return new ValidationResult(EngineMessage.NETWORK_INTERFACE_ATTACHED_TO_NETWORK_CANNOT_BE_SLAVE,
                    potentialSlave.getName());
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

    HostInterfaceValidator createHostInterfaceValidator(VdsNetworkInterface vdsNetworkInterface) {
        return new HostInterfaceValidator(vdsNetworkInterface);
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

    /**
     * @param bondName name of bonded interface.
     *
     * @return true if there's request to remove bond of given name.
     */
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
            NetworkAttachmentValidator validator = createNetworkAttachmentValidator(attachment);

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
            vr = skipValidation(vr) ? vr : validator.networkIpAddressWasSameAsHostnameAndChanged(existingInterfaces);
            vr = skipValidation(vr) ? vr : validator.networkNotChanged(attachmentsById.get(attachment.getId()));
            vr = skipValidation(vr) ? vr : validator.validateGateway();

            boolean attachmentUpdated = attachment.getId() != null;
            if (attachmentUpdated) {
                vr = skipValidation(vr) ? vr : validator.networkNotUsedByVms();
                vr = skipValidation(vr) ? vr : notMovingLabeledNetworkToDifferentNic(attachment);
            }
        }

        return vr;
    }

    private ValidationResult validateCoherentNicIdentification(NetworkAttachment attachment) {
        return validateCoherentNicIdentification(attachment.getId(),
            attachment.getNicId(),
            attachment.getNicName(),
            EngineMessage.NETWORK_ATTACHMENT_REFERENCES_NICS_INCOHERENTLY);
    }

    private ValidationResult validateCoherentNicIdentification(Bond bond) {
        Guid nicId = bond.getId();
        String nicName = bond.getName();
        EngineMessage message = EngineMessage.BOND_REFERENCES_NICS_INCOHERENTLY;
        return validateCoherentNicIdentification(bond.getId(), nicId, nicName, message);

    }

    private ValidationResult validateCoherentNicIdentification(Guid violatingEntityId,
        Guid nicId,
        String nicName,
        EngineMessage message) {

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
        VdsNetworkInterface interfaceByName = existingInterfaces.get(nicName);
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

        return new ValidationResult(EngineMessage.NETWORK_ATTACHMENT_NOT_EXISTS);
    }

    private ValidationResult validRemovedNetworkAttachments() {
        List<Guid> invalidIds = Entities.idsNotReferencingExistingRecords(params.getRemovedNetworkAttachments(),
            existingAttachments);
        if (!invalidIds.isEmpty()) {
            return new ValidationResult(EngineMessage.NETWORK_ATTACHMENT_NOT_EXISTS, commaSeparated(invalidIds));
        }

        ValidationResult vr = ValidationResult.VALID;
        Iterator<NetworkAttachment> iterator = removedNetworkAttachments.iterator();
        while (iterator.hasNext() && vr.isValid()) {
            NetworkAttachment attachment = iterator.next();
            NetworkAttachment attachmentToValidate = attachmentsById.get(attachment.getId());
            NetworkAttachmentValidator validator = createNetworkAttachmentValidator(attachmentToValidate);

            vr = skipValidation(vr) ? vr : validator.networkAttachmentIsSet();
            vr = skipValidation(vr) ? vr : validator.notExternalNetwork();
            vr = skipValidation(vr) ? vr : validator.notRemovingManagementNetwork();
            vr = skipValidation(vr) ? vr : notRemovingLabeledNetworks(attachment, existingInterfaces);
            vr = skipValidation(vr) ? vr : validateNotRemovingUsedNetworkByVms();
        }

        return vr;
    }

    private NetworkAttachmentValidator createNetworkAttachmentValidator(NetworkAttachment attachmentToValidate) {
        return new NetworkAttachmentValidator(attachmentToValidate,
            host,
            managementNetworkUtil,
            networkAttachmentDao,
            new VmInterfaceManager(),
            networkClusterDao,
            networkDao,
            vdsDao);
    }

    /**
     * @param attachment attachment obtained from db, record validity is assumed.
     * @param existingNics nicNameToNic map of existing nics.
     *
     * @return removed attachment relates to network and nic. Method returns true such network is not labeled,
     * such nic is currently being removed bond,
     * or such nic is not labeled by same label as network is.
     */
    ValidationResult notRemovingLabeledNetworks(NetworkAttachment attachment,
        Map<String, VdsNetworkInterface> existingNics) {

        Network removedNetwork = existingNetworkRelatedToAttachment(attachment);
        if (!NetworkUtils.isLabeled(removedNetwork)) {
            return ValidationResult.VALID;
        }

        VdsNetworkInterface nic = existingNics.get(attachment.getNicName());

        /*
            When attachment is related to labeled network and bond being removed, it's considered to be valid,
            because with disappearance of bond its label also disappears, so technically we cannot detach such network,
            since it shouldn't be present there anyways.
        * */
        if (nic != null && !removedBondVdsNetworkInterfaceMap.containsKey(nic.getName())) {
            if (NetworkUtils.isLabeled(nic) && nic.getLabels().contains(removedNetwork.getLabel())) {
                return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_CANNOT_REMOVE_LABELED_NETWORK_FROM_NIC,
                    removedNetwork.getName());
            }
        }

        return ValidationResult.VALID;
    }

    /**
     * Validates there is no differences on MTU value between non-VM network to Vlans over the same interface/bond
     */
    private ValidationResult validateMtu(Collection<NetworkAttachment> attachmentsToConfigure) {
        return validateMtu(getNicNameToNetworksMap(attachmentsToConfigure));
    }

    ValidationResult validateMtu(Map<String, List<Network>> nicsToNetworks) {
        for (List<Network> networksOnNic : nicsToNetworks.values()) {
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

    Map<String, List<Network>> getNicNameToNetworksMap(Collection<NetworkAttachment> attachmentsToConfigure) {
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
        return new ValidationResult(EngineMessage.NETWORK_MTU_DIFFERENCES, replacements);
    }

    ValidationResult notMovingLabeledNetworkToDifferentNic(NetworkAttachment attachment) {
        Network movedNetwork = existingNetworkRelatedToAttachment(attachment);

        if (!NetworkUtils.isLabeled(movedNetwork)) {
            return ValidationResult.VALID;
        }

        NetworkAttachment existingAttachment = attachmentsById.get(attachment.getId());
        boolean movedToDifferentNic = !existingAttachment.getNicId().equals(attachment.getNicId());

        return ValidationResult.failWith(EngineMessage.ACTION_TYPE_FAILED_CANNOT_MOVE_LABELED_NETWORK_TO_ANOTHER_NIC,
            ReplacementUtils.createSetVariableString(
                ACTION_TYPE_FAILED_CANNOT_MOVE_LABELED_NETWORK_TO_ANOTHER_NIC_ENTITY, movedNetwork.getLabel()))
            .when(movedToDifferentNic);

    }

    private ValidationResult validateCustomProperties() {
        String version = host.getVdsGroupCompatibilityVersion().getValue();
        SimpleCustomPropertiesUtil util = SimpleCustomPropertiesUtil.getInstance();

        Map<String, String> validPropertiesForVmNetwork =
            util.convertProperties(Config.<String> getValue(ConfigValues.PreDefinedNetworkCustomProperties, version));
        validPropertiesForVmNetwork.putAll(util.convertProperties(Config.<String> getValue(ConfigValues.UserDefinedNetworkCustomProperties,
            version)));

        Map<String, String> validPropertiesForNonVm = new HashMap<>(validPropertiesForVmNetwork);
        validPropertiesForNonVm.remove("bridge_opts");

        return validateCustomProperties(util, validPropertiesForVmNetwork, validPropertiesForNonVm);
    }

    ValidationResult validateCustomProperties(SimpleCustomPropertiesUtil util,
        Map<String, String> validPropertiesForVm,
        Map<String, String> validPropertiesForNonVm) {
        for (NetworkAttachment attachment : params.getNetworkAttachments()) {
            Network network = existingNetworkRelatedToAttachment(attachment);
            if (attachment.hasProperties()) {
                if (!networkCustomPropertiesSupported) {
                    return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_NETWORK_CUSTOM_PROPERTIES_NOT_SUPPORTED,
                        network.getName());
                }

                List<ValidationError> errors =
                    util.validateProperties(network.isVmNetwork() ? validPropertiesForVm : validPropertiesForNonVm,
                        attachment.getProperties());
                if (!errors.isEmpty()) {
                    handleCustomPropertiesError(util, errors);
                    return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_NETWORK_CUSTOM_PROPERTIES_BAD_INPUT,
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

    VmInterfaceManager getVmInterfaceManager() {
        return new VmInterfaceManager();
    }

    private boolean skipValidation(ValidationResult validationResult) {
        return !validationResult.isValid();
    }

    private String commaSeparated(List<?> invalidBondIds) {
        return StringUtils.join(invalidBondIds, ", ");
    }
}

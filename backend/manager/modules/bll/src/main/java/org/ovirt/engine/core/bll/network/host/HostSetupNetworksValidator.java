package org.ovirt.engine.core.bll.network.host;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
import org.ovirt.engine.core.common.businessentities.BusinessEntitiesDefinitions;
import org.ovirt.engine.core.common.businessentities.BusinessEntityMap;
import org.ovirt.engine.core.common.businessentities.Entities;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.network.Bond;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkAttachment;
import org.ovirt.engine.core.common.businessentities.network.NicLabel;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.utils.MapNetworkAttachments;
import org.ovirt.engine.core.common.utils.customprop.SimpleCustomPropertiesUtil;
import org.ovirt.engine.core.common.utils.customprop.ValidationError;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.dao.network.NetworkClusterDao;
import org.ovirt.engine.core.dao.network.NetworkDao;
import org.ovirt.engine.core.utils.NetworkUtils;
import org.ovirt.engine.core.utils.ReplacementUtils;
import org.ovirt.engine.core.utils.collections.MultiValueMapUtils;
import org.ovirt.engine.core.utils.linq.LinqUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HostSetupNetworksValidator {
    private static final Logger log = LoggerFactory.getLogger(HostSetupNetworksValidator.class);

    private static final String LIST_SUFFIX = "_LIST";
    private static final String ENTITY_SUFFIX  = "_ENTITY";

    //TODO MM: here? EngineMessage? Elsewhere? (note: some of these are related to this file and it's test only, some even elsewhere
    public static final String VAR_NETWORK_ATTACHMENT_NOT_EXISTS_ENTITY =
            EngineMessage.NETWORK_ATTACHMENT_NOT_EXISTS + ENTITY_SUFFIX;
    public static final String VAR_NETWORK_BONDS_INVALID_SLAVE_COUNT_LIST =
            EngineMessage.NETWORK_BONDS_INVALID_SLAVE_COUNT + LIST_SUFFIX;

    static final String NETWORK_INTERFACE_ADDED_TO_BOND_AND_NETWORK_IS_ATTACHED_TO_IT_AT_THE_SAME_TIME_ENTITY =
            EngineMessage.NETWORK_INTERFACE_ADDED_TO_BOND_AND_NETWORK_IS_ATTACHED_TO_IT_AT_THE_SAME_TIME
                    + ENTITY_SUFFIX;
    static final String ACTION_TYPE_FAILED_CANNOT_MOVE_LABELED_NETWORK_TO_ANOTHER_NIC_ENTITY =
            EngineMessage.ACTION_TYPE_FAILED_CANNOT_MOVE_LABELED_NETWORK_TO_ANOTHER_NIC + ENTITY_SUFFIX;
    static final String VAR_NETWORKS_ALREADY_ATTACHED_TO_IFACES_LIST =
            EngineMessage.NETWORKS_ALREADY_ATTACHED_TO_IFACES + LIST_SUFFIX;
    static final String VAR_NETWORK_BOND_RECORD_DOES_NOT_EXISTS_LIST =
            EngineMessage.NETWORK_BOND_RECORD_DOES_NOT_EXISTS + LIST_SUFFIX;
    static final String VAR_NETWORK_CANNOT_DETACH_NETWORK_USED_BY_VMS_LIST =
            EngineMessage.NETWORK_CANNOT_DETACH_NETWORK_USED_BY_VMS + LIST_SUFFIX;
    static final String VAR_NETWORK_BOND_NAME_BAD_FORMAT_ENTITY =
            EngineMessage.NETWORK_BOND_NAME_BAD_FORMAT + ENTITY_SUFFIX;
    static final String VAR_NETWORK_INTERFACE_ALREADY_IN_BOND_ENTITY =
            EngineMessage.NETWORK_INTERFACE_ALREADY_IN_BOND + ENTITY_SUFFIX;
    static final String NETWORK_INTERFACE_ATTACHED_TO_NETWORK_CANNOT_BE_SLAVE_ENTITY =
            EngineMessage.NETWORK_INTERFACE_ATTACHED_TO_NETWORK_CANNOT_BE_SLAVE + ENTITY_SUFFIX;
    static final String VAR_NETWORK_ATTACHMENTS_NOT_EXISTS_LIST =
            EngineMessage.NETWORK_ATTACHMENT_NOT_EXISTS + LIST_SUFFIX;
    static final String VAR_ACTION_TYPE_FAILED_CANNOT_REMOVE_LABELED_NETWORK_FROM_NIC_LIST =
            EngineMessage.ACTION_TYPE_FAILED_CANNOT_REMOVE_LABELED_NETWORK_FROM_NIC + LIST_SUFFIX;
    static final String VAR_ACTION_TYPE_FAILED_NETWORK_CUSTOM_PROPERTIES_NOT_SUPPORTED_LIST =
            EngineMessage.ACTION_TYPE_FAILED_NETWORK_CUSTOM_PROPERTIES_NOT_SUPPORTED + LIST_SUFFIX;
    static final String VAR_ACTION_TYPE_FAILED_NETWORK_CUSTOM_PROPERTIES_BAD_INPUT_LIST =
            EngineMessage.ACTION_TYPE_FAILED_NETWORK_CUSTOM_PROPERTIES_BAD_INPUT + LIST_SUFFIX;
    static final String VAR_BOND_NAME = "BondName";
    static final String VAR_NETWORK_NAME = "networkName";
    static final String VAR_NETWORK_NAMES = "networkNames";
    static final String VAR_ATTACHMENT_IDS = "attachmentIds";

    private HostSetupNetworksParameters params;
    private VDS host;
    private BusinessEntityMap<VdsNetworkInterface> existingInterfacesMap;
    private List<NetworkAttachment> existingAttachments;
    private final ManagementNetworkUtil managementNetworkUtil;
    private boolean networkCustomPropertiesSupported;
    private List<VdsNetworkInterface> removedBondVdsNetworkInterface;
    private BusinessEntityMap<VdsNetworkInterface> removedBondVdsNetworkInterfaceMap;
    private List<NetworkAttachment> removedNetworkAttachments;
    private BusinessEntityMap<Network> networkBusinessEntityMap;
    private final Map<Guid, NetworkAttachment> attachmentsById;
    private final NetworkClusterDao networkClusterDao;
    private final NetworkDao networkDao;
    private final VdsDao vdsDao;
    private final BusinessEntityMap<Bond> bondsMap;
    private final VmDao vmDao;
    private Map<Guid, NetworkAttachment> networkAttachmentsByNetworkId;
    private Map<String, NicLabel> nicLabelByLabel;
    private HostSetupNetworksValidatorHelper hostSetupNetworksValidatorHelper;

    public HostSetupNetworksValidator(VDS host,
            HostSetupNetworksParameters params,
            List<VdsNetworkInterface> existingInterfaces,
            List<NetworkAttachment> existingAttachments,
            BusinessEntityMap<Network> networkBusinessEntityMap,
            ManagementNetworkUtil managementNetworkUtil,
            NetworkClusterDao networkClusterDao,
            NetworkDao networkDao,
            VdsDao vdsDao,
            HostSetupNetworksValidatorHelper hostSetupNetworksValidatorHelper,
            VmDao vmDao) {

        this.host = host;
        this.params = params;
        this.existingAttachments = existingAttachments;
        this.managementNetworkUtil = managementNetworkUtil;
        this.networkClusterDao = networkClusterDao;
        this.networkDao = networkDao;
        this.vdsDao = vdsDao;
        this.vmDao = vmDao;
        this.existingInterfacesMap = new BusinessEntityMap<>(existingInterfaces);
        this.networkBusinessEntityMap = networkBusinessEntityMap;

        this.removedBondVdsNetworkInterface = Entities.filterEntitiesByRequiredIds(params.getRemovedBonds(),
            existingInterfaces);
        this.removedBondVdsNetworkInterfaceMap = new BusinessEntityMap<>(removedBondVdsNetworkInterface);
        this.removedNetworkAttachments = Entities.filterEntitiesByRequiredIds(params.getRemovedNetworkAttachments(),
            existingAttachments);

        setSupportedFeatures();

        attachmentsById = Entities.businessEntitiesById(existingAttachments);
        bondsMap = new BusinessEntityMap<>(params.getBonds());
        networkAttachmentsByNetworkId = new MapNetworkAttachments(params.getNetworkAttachments()).byNetworkId();

        nicLabelByLabel = Entities.entitiesByName(params.getLabels());

        this.hostSetupNetworksValidatorHelper = hostSetupNetworksValidatorHelper;
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
        vr = skipValidation(vr) ? vr : new NicLabelValidator(params, existingInterfacesMap, bondsMap, hostSetupNetworksValidatorHelper).validate();
        vr = skipValidation(vr) ? vr : validNewOrModifiedNetworkAttachments();
        vr = skipValidation(vr) ? vr : validRemovedNetworkAttachments();
        vr = skipValidation(vr) ? vr : validNewOrModifiedBonds();
        vr = skipValidation(vr) ? vr : validRemovedBonds(attachmentsToConfigure);
        vr = skipValidation(vr) ? vr : attachmentsDontReferenceSameNetworkDuplicately(attachmentsToConfigure);
        vr = skipValidation(vr) ? vr : networksUniquelyConfiguredOnHost(attachmentsToConfigure);
        vr = skipValidation(vr) ? vr : validateNetworkExclusiveOnNics(attachmentsToConfigure);
        vr = skipValidation(vr) ? vr : new NetworkMtuValidator(networkBusinessEntityMap).validateMtu(
            attachmentsToConfigure);
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
                Network network = existingNetworkRelatedToAttachment(attachment);
                return new ValidationResult(EngineMessage.NETWORKS_ALREADY_ATTACHED_TO_IFACES,
                    ReplacementUtils.createSetVariableString(VAR_NETWORKS_ALREADY_ATTACHED_TO_IFACES_LIST,
                        network.getName()));

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

        final List<String> vmNames = getVmInterfaceManager().findActiveVmsUsingNetworks(host.getId(), removedNetworks);

        if (vmNames.isEmpty()) {
            return ValidationResult.VALID;
        } else {
            final List<String> sortedRemovedNetsworks = new ArrayList<>(removedNetworks);
            Collections.sort(sortedRemovedNetsworks);

            return new ValidationResult(
                    EngineMessage.NETWORK_CANNOT_DETACH_NETWORK_USED_BY_VMS,
                    LinqUtils.concat(
                            ReplacementUtils.replaceWith(VAR_NETWORK_NAMES, sortedRemovedNetsworks),
                            ReplacementUtils.replaceWith(VAR_NETWORK_CANNOT_DETACH_NETWORK_USED_BY_VMS_LIST, vmNames)));
        }
    }

    ValidationResult validRemovedBonds(Collection<NetworkAttachment> attachmentsToConfigure) {
        List<Guid> invalidBondIds = Entities.idsNotReferencingExistingRecords(params.getRemovedBonds(),
            existingInterfacesMap.unmodifiableEntitiesByIdMap());
        if (!invalidBondIds.isEmpty()) {
            return new ValidationResult(EngineMessage.NETWORK_BOND_RECORD_DOES_NOT_EXISTS,
                ReplacementUtils.replaceWith(VAR_NETWORK_BOND_RECORD_DOES_NOT_EXISTS_LIST, invalidBondIds));

        }

        Map<String, List<Guid>> nicNameToAttachedNetworkAttachmentIds =
            getIdsOfNetworkAttachmentsRelatedToInterfaceNames(attachmentsToConfigure);

        for (VdsNetworkInterface removedBond : removedBondVdsNetworkInterface) {
            String bondName = removedBond.getName();
            VdsNetworkInterface existingBond = existingInterfacesMap.get(bondName);
            ValidationResult interfaceIsBondOrNull = createHostInterfaceValidator(existingBond).interfaceIsBondOrNull();
            if (!interfaceIsBondOrNull.isValid()) {
                return interfaceIsBondOrNull;
            }

            boolean cantRemoveRequiredInterface = nicNameToAttachedNetworkAttachmentIds.containsKey(bondName);
            if (cantRemoveRequiredInterface) {
                List<Guid> networkAttachmentsForNic = nicNameToAttachedNetworkAttachmentIds.get(bondName);

                List<String> replacements = new ArrayList<>();
                replacements.add(ReplacementUtils.createSetVariableString(VAR_BOND_NAME, bondName));
                replacements.addAll(ReplacementUtils.replaceWith(VAR_ATTACHMENT_IDS, networkAttachmentsForNic));

                return new ValidationResult(EngineMessage.BOND_USED_BY_NETWORK_ATTACHMENTS, replacements);

            }
        }

        return ValidationResult.VALID;
    }

    private Map<String, List<Guid>> getIdsOfNetworkAttachmentsRelatedToInterfaceNames(Collection<NetworkAttachment> networkAttachments) {
        Map<String, List<Guid>> map = new HashMap<>();
        for (NetworkAttachment attachment : networkAttachments) {
            MultiValueMapUtils.addToMap(attachment.getNicName(),
                attachment.getId(),
                map,
                new MultiValueMapUtils.ListCreator<Guid>());

        }

        return map;
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

            //does not test, whether interface exists, but only if the instance is non-null and its name is set.
            ValidationResult interfaceByNameExists = createHostInterfaceValidator(modifiedOrNewBond).interfaceByNameExists();
            if (!interfaceByNameExists.isValid()) {
                return interfaceByNameExists;
            }

            boolean validBondName = bondName != null && bondName.matches(BusinessEntitiesDefinitions.BOND_NAME_PATTERN);

            if (!validBondName) {
                return new ValidationResult(EngineMessage.NETWORK_BOND_NAME_BAD_FORMAT,
                    ReplacementUtils.createSetVariableString(VAR_NETWORK_BOND_NAME_BAD_FORMAT_ENTITY, bondName));

            }

            //either it's newly create bond, thus non existing, or given name must reference existing bond.
            ValidationResult interfaceIsBondOrNull = createHostInterfaceValidator(existingInterfacesMap.get(bondName)).interfaceIsBondOrNull();
            if (!interfaceIsBondOrNull.isValid()) {
                return interfaceIsBondOrNull;
            }

            //count of bond slaves must be at least two.
            if (modifiedOrNewBond.getSlaves().size() < 2) {
                return new ValidationResult(EngineMessage.NETWORK_BONDS_INVALID_SLAVE_COUNT,
                    ReplacementUtils.createSetVariableString(VAR_NETWORK_BONDS_INVALID_SLAVE_COUNT_LIST, bondName));

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
            VdsNetworkInterface potentialSlave = existingInterfacesMap.get(slaveName);
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
                return new ValidationResult(EngineMessage.NETWORK_INTERFACE_ALREADY_IN_BOND,
                    ReplacementUtils.createSetVariableString(VAR_NETWORK_INTERFACE_ALREADY_IN_BOND_ENTITY, slaveName));

            }

            ValidationResult slaveHasAttachedNetworksValidationResult =
                    validateSlaveHasNoNetworks(potentialSlave.getName());
            if (!slaveHasAttachedNetworksValidationResult.isValid()) {
                return slaveHasAttachedNetworksValidationResult;
            }

            if (slaveUsedMultipleTimesInDifferentBonds(slaveName)) {
                return new ValidationResult(EngineMessage.NETWORK_INTERFACE_REFERENCED_AS_A_SLAVE_MULTIPLE_TIMES,
                    ReplacementUtils.createSetVariableString(
                        "NETWORK_INTERFACE_REFERENCED_AS_A_SLAVE_MULTIPLE_TIMES_ENTITY",
                        slaveName));
            }

            ValidationResult slaveHasNoLabelsValidationResult = validateSlaveHasNoLabels(slaveName);

            if (!slaveHasNoLabelsValidationResult.isValid()) {
                return slaveHasNoLabelsValidationResult;
            }
        }

        return ValidationResult.VALID;
    }

    ValidationResult validateSlaveHasNoLabels(String slaveName) {
        Set<String> labelsToConfigureOnNic = getLabelsToConfigureOnNic(slaveName);
        return ValidationResult.failWith(EngineMessage.LABEL_ATTACH_TO_IMPROPER_INTERFACE,
                ReplacementUtils.createSetVariableString(
                        "LABEL_ATTACH_TO_IMPROPER_INTERFACE_ENTITY",
                        slaveName)).unless(labelsToConfigureOnNic == null || labelsToConfigureOnNic.isEmpty());
    }

    private ValidationResult validateSlaveHasNoNetworks(String slaveName) {
        for (NetworkAttachment attachment : getAttachmentsToConfigure()) {
            if (Objects.equals(attachment.getNicName(), slaveName)) {
                if (attachment.getId() == null) {
                    return new ValidationResult(EngineMessage.NETWORK_INTERFACE_ADDED_TO_BOND_AND_NETWORK_IS_ATTACHED_TO_IT_AT_THE_SAME_TIME,
                            ReplacementUtils.createSetVariableString(
                                    NETWORK_INTERFACE_ADDED_TO_BOND_AND_NETWORK_IS_ATTACHED_TO_IT_AT_THE_SAME_TIME_ENTITY,
                                    slaveName),
                            ReplacementUtils.createSetVariableString(VAR_NETWORK_NAME, attachment.getNetworkName()));
                } else {
                    return new ValidationResult(EngineMessage.NETWORK_INTERFACE_ATTACHED_TO_NETWORK_CANNOT_BE_SLAVE,
                            ReplacementUtils.createSetVariableString(
                                    NETWORK_INTERFACE_ATTACHED_TO_NETWORK_CANNOT_BE_SLAVE_ENTITY,
                                    slaveName),
                            ReplacementUtils.createSetVariableString(VAR_NETWORK_NAME, attachment.getNetworkName()));
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

    HostInterfaceValidator createHostInterfaceValidator(VdsNetworkInterface vdsNetworkInterface) {
        return new HostInterfaceValidator(vdsNetworkInterface);
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
        Bond bond = this.bondsMap.get(bondName);
        return bond != null && !bond.getSlaves().contains(slaveName);
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
            vr = skipValidation(vr) ? vr : validateCoherentNetworkIdentification(attachment);
            vr = skipValidation(vr) ? vr : modifiedAttachmentExists(attachment.getId());
            vr = skipValidation(vr) ? vr : modifiedAttachmentNotRemoved(attachment);
            vr = skipValidation(vr) ? vr : validateAttachmentNotReferenceVlanDevice(attachment);
            vr = skipValidation(vr) ? vr : validateAttachmentAndNicReferenceSameLabelNotConflict(attachment);
            vr = skipValidation(vr) ? vr : validator.notExternalNetwork();
            vr = skipValidation(vr) ? vr : validator.networkAttachedToCluster();
            vr = skipValidation(vr) ? vr : validator.ipConfiguredForStaticBootProtocol();
            vr = skipValidation(vr) ? vr : validator.bootProtocolSetForDisplayNetwork();

            //this is not nic exist, but only nic is set.
            vr = skipValidation(vr) ? vr : validator.nicExists();
            vr = skipValidation(vr) ? vr : nicActuallyExistsOrReferencesNewBond(attachment);

            vr = skipValidation(vr) ? vr : validator.networkIpAddressWasSameAsHostnameAndChanged(existingInterfacesMap);
            vr = skipValidation(vr) ? vr : validator.networkNotChanged(attachmentsById.get(attachment.getId()));
            vr = skipValidation(vr) ? vr : validator.validateGateway();

            boolean attachmentUpdated = !isNewAttachment(attachment.getId());
            if (attachmentUpdated) {
                vr = skipValidation(vr) ? vr : validator.networkNotUsedByVms();
                vr = skipValidation(vr) ? vr : notMovingLabeledNetworkToDifferentNic(attachment);
            }
        }

        return vr;
    }

    private ValidationResult validateCoherentNetworkIdentification(NetworkAttachment attachment) {
        Guid networkId = attachment.getNetworkId();
        String networkName = attachment.getNetworkName();
        Guid violatingEntityId = attachment.getId();

        return hostSetupNetworksValidatorHelper.validateCoherentIdentification(String.valueOf(violatingEntityId),
            networkId,
            networkName,
            EngineMessage.NETWORK_ATTACHMENT_REFERENCES_NETWORK_INCOHERENTLY,
            networkBusinessEntityMap);
    }

    private ValidationResult validateCoherentNicIdentification(NetworkAttachment attachment) {
        return hostSetupNetworksValidatorHelper.validateCoherentIdentification(String.valueOf(attachment.getId()),
                attachment.getNicId(),
                attachment.getNicName(),
                EngineMessage.NETWORK_ATTACHMENT_REFERENCES_NICS_INCOHERENTLY, existingInterfacesMap);
    }

    private ValidationResult validateCoherentNicIdentification(Bond bond) {
        Guid nicId = bond.getId();
        String nicName = bond.getName();
        EngineMessage message = EngineMessage.BOND_REFERENCES_NICS_INCOHERENTLY;
        return hostSetupNetworksValidatorHelper.validateCoherentIdentification(bond.getName(), nicId, nicName, message, existingInterfacesMap);

    }

    private ValidationResult modifiedAttachmentExists(Guid networkAttachmentId) {
        if (isNewAttachment(networkAttachmentId)) {
            return ValidationResult.VALID;
        }

        for (NetworkAttachment existingAttachment : existingAttachments) {
          if (existingAttachment.getId().equals(networkAttachmentId)) {
                return ValidationResult.VALID;
            }
        }

        return new ValidationResult(EngineMessage.NETWORK_ATTACHMENT_NOT_EXISTS,
            ReplacementUtils.createSetVariableString(VAR_NETWORK_ATTACHMENT_NOT_EXISTS_ENTITY,
                networkAttachmentId.toString()));

    }

    ValidationResult modifiedAttachmentNotRemoved(NetworkAttachment networkAttachment) {
        Guid networkAttachmentId = networkAttachment.getId();
        if (isNewAttachment(networkAttachmentId)) {
            return ValidationResult.VALID;
        }

        boolean attachmentInRemoveList = params.getRemovedNetworkAttachments().contains(networkAttachmentId);

        return ValidationResult.failWith(EngineMessage.NETWORK_ATTACHMENT_IN_BOTH_LISTS,
                ReplacementUtils.createSetVariableString("NETWORK_ATTACHMENT_IN_BOTH_LISTS_ENTITY",
                        networkAttachmentId.toString())).when(attachmentInRemoveList);

    }

    private boolean isNewAttachment(Guid networkAttachmentId) {
        return networkAttachmentId == null;
    }

    private ValidationResult nicActuallyExistsOrReferencesNewBond(NetworkAttachment attachment) {
        boolean nicActuallyExistsOrReferencesNewBond =
                isNicActuallyExistsOrReferencesNewBond(attachment.getNicName(), attachment.getNicId());

        if (nicActuallyExistsOrReferencesNewBond) {
            return ValidationResult.VALID;
        }

        // TODO MM: this message also exist in different code without interface id being mentioned. How to fix?
        // Duplicate message / fix other code as well?
        return new ValidationResult(EngineMessage.HOST_NETWORK_INTERFACE_NOT_EXIST);
    }

    private boolean isNicActuallyExistsOrReferencesNewBond(String nicName, Guid nicId) {
        return hostSetupNetworksValidatorHelper.isNicActuallyExistsOrReferencesNewBond(existingInterfacesMap, bondsMap, nicName, nicId);
    }

    private ValidationResult validRemovedNetworkAttachments() {
        List<Guid> invalidIds = Entities.idsNotReferencingExistingRecords(params.getRemovedNetworkAttachments(),
            existingAttachments);
        if (!invalidIds.isEmpty()) {
            return new ValidationResult(EngineMessage.NETWORK_ATTACHMENT_NOT_EXISTS,
                ReplacementUtils.replaceWith(VAR_NETWORK_ATTACHMENTS_NOT_EXISTS_LIST, invalidIds));

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
            vr = skipValidation(vr) ? vr : notRemovingLabeledNetworks(attachment);
            vr = skipValidation(vr) ? vr : validateNotRemovingUsedNetworkByVms();
        }

        return vr;
    }

    private NetworkAttachmentValidator createNetworkAttachmentValidator(NetworkAttachment attachmentToValidate) {
        return new NetworkAttachmentValidator(attachmentToValidate,
            host,
            managementNetworkUtil,
            new VmInterfaceManager(),
            networkClusterDao,
            networkDao,
            vdsDao,
            vmDao);
    }

    /**
     * @param attachment attachment obtained from db, record validity is assumed.
     *
     * @return removed attachment relates to network and nic. Method returns true such network is not labeled,
     * such nic is currently being removed bond,
     * or such nic is not labeled by same label as network is.
     */
    ValidationResult notRemovingLabeledNetworks(NetworkAttachment attachment) {
        Network removedNetwork = existingNetworkRelatedToAttachment(attachment);

        if (!NetworkUtils.isLabeled(removedNetwork)) {
            return ValidationResult.VALID;
        }

        boolean networkAttachedToNicByAnotherAttachment =
                networkAttachmentsByNetworkId.get(removedNetwork.getId()) != null;
        return ValidationResult.failWith(EngineMessage.ACTION_TYPE_FAILED_CANNOT_REMOVE_LABELED_NETWORK_FROM_NIC,
                ReplacementUtils.createSetVariableString(
                        VAR_ACTION_TYPE_FAILED_CANNOT_REMOVE_LABELED_NETWORK_FROM_NIC_LIST,
                        removedNetwork.getName()))
                .when(!networkAttachedToNicByAnotherAttachment
                        && isNicToConfigureContainTheLabel(attachment.getNicName(), removedNetwork.getLabel()));

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
                        "networkName", movedNetwork.getName()),
                ReplacementUtils.createSetVariableString(
                        ACTION_TYPE_FAILED_CANNOT_MOVE_LABELED_NETWORK_TO_ANOTHER_NIC_ENTITY, movedNetwork.getLabel()))
                .when(movedToDifferentNic
                        && isNicToConfigureContainTheLabel(existingAttachment.getNicName(), movedNetwork.getLabel()));

    }

    ValidationResult validateAttachmentAndNicReferenceSameLabelNotConflict(NetworkAttachment attachment) {
        Network network = existingNetworkRelatedToAttachment(attachment);

        if (!NetworkUtils.isLabeled(network)) {
            return ValidationResult.VALID;
        }

        String label = network.getLabel();
        String nicThatShouldHaveTheLabel =
                nicLabelByLabel.containsKey(label) ? nicLabelByLabel.get(label).getNicName() : null;

        return ValidationResult.failWith(EngineMessage.NETWORK_SHOULD_BE_ATTACHED_VIA_LABEL_TO_ANOTHER_NIC,
                ReplacementUtils.createSetVariableString("NETWORK_SHOULD_BE_ATTACHED_VIA_LABEL_TO_ANOTHER_NIC_ENTITY",
                        network.getName()),
                ReplacementUtils.createSetVariableString("interfaceName", attachment.getNicName()),
                ReplacementUtils.createSetVariableString("labeledInterfaceName", nicThatShouldHaveTheLabel))
                .unless(nicThatShouldHaveTheLabel == null || nicThatShouldHaveTheLabel.equals(attachment.getNicName()));

    }

    private Set<String> getLabelsToConfigureOnNic(String nicName) {
        VdsNetworkInterface existingNic = existingInterfacesMap.get(nicName);
        Set<String> labelsToConfigure = new HashSet<>();

        if (existingNic != null) {
            boolean nicWasRemoved = removedBondVdsNetworkInterfaceMap.containsKey(existingNic.getName());

            if (nicWasRemoved) {
                return null;
            }

            Set<String> oldLabels = existingNic.getLabels();

            if (oldLabels != null) {
                for (String label : oldLabels) {
                    NicLabel nicLabel = nicLabelByLabel.get(label);
                    boolean labelRemovedFromNic =
                            params.getRemovedLabels().contains(label)
                                    || (nicLabel != null && !Objects.equals(nicLabel.getNicName(),
                                            existingNic.getName()));
                    if (!labelRemovedFromNic) {
                        labelsToConfigure.add(label);
                    }
                }
            }

            for (NicLabel nicLabel : params.getLabels()) {
                if (existingNic.getName().equals(nicLabel.getNicName())) {
                    labelsToConfigure.add(nicLabel.getLabel());
                }
            }
        }
        return labelsToConfigure;
    }

    private boolean isNicToConfigureContainTheLabel(String nicName, String label) {
        Set<String> labelsToConfigure = getLabelsToConfigureOnNic(nicName);

        return labelsToConfigure != null && labelsToConfigure.contains(label);
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
                        ReplacementUtils.createSetVariableString(
                            VAR_ACTION_TYPE_FAILED_NETWORK_CUSTOM_PROPERTIES_NOT_SUPPORTED_LIST,
                            network.getName()));

                }

                List<ValidationError> errors =
                    util.validateProperties(network.isVmNetwork() ? validPropertiesForVm : validPropertiesForNonVm,
                        attachment.getProperties());
                if (!errors.isEmpty()) {
                    handleCustomPropertiesError(util, errors);
                    return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_NETWORK_CUSTOM_PROPERTIES_BAD_INPUT,
                        ReplacementUtils.createSetVariableString(
                            VAR_ACTION_TYPE_FAILED_NETWORK_CUSTOM_PROPERTIES_BAD_INPUT_LIST,
                            network.getName()));

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

    private ValidationResult validateAttachmentNotReferenceVlanDevice(NetworkAttachment attachment) {
        VdsNetworkInterface nic = existingInterfacesMap.get(attachment.getNicName());
        return ValidationResult.failWith(EngineMessage.ATTACHMENT_REFERENCE_VLAN_DEVICE,
                ReplacementUtils.createSetVariableString("ATTACHMENT_REFERENCE_VLAN_DEVICE_ENTITY", attachment.getNetworkName()),
                ReplacementUtils.createSetVariableString("nicName", attachment.getNicName())).when(nic != null
                && NetworkUtils.isVlan(nic));
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

}

package org.ovirt.engine.core.bll.network.host;

import static org.ovirt.engine.core.common.utils.NetworkCommonUtils.isEl8;

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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.bll.network.FindActiveVmsUsingNetwork;
import org.ovirt.engine.core.bll.validator.HostInterfaceValidator;
import org.ovirt.engine.core.bll.validator.HostNetworkQosValidator;
import org.ovirt.engine.core.bll.validator.NetworkAttachmentValidator;
import org.ovirt.engine.core.bll.validator.NetworkAttachmentsValidator;
import org.ovirt.engine.core.bll.validator.network.NetworkAttachmentIpConfigurationValidator;
import org.ovirt.engine.core.bll.validator.network.NetworkExclusivenessValidator;
import org.ovirt.engine.core.bll.validator.network.NetworkExclusivenessValidatorResolver;
import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.action.CreateOrUpdateBond;
import org.ovirt.engine.core.common.action.HostSetupNetworksParameters;
import org.ovirt.engine.core.common.businessentities.BusinessEntitiesDefinitions;
import org.ovirt.engine.core.common.businessentities.BusinessEntityMap;
import org.ovirt.engine.core.common.businessentities.Entities;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.network.Bond;
import org.ovirt.engine.core.common.businessentities.network.BondMode;
import org.ovirt.engine.core.common.businessentities.network.HostNetworkQos;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkAttachment;
import org.ovirt.engine.core.common.businessentities.network.NicLabel;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface.NetworkImplementationDetails;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.utils.MapNetworkAttachments;
import org.ovirt.engine.core.common.utils.NetworkCommonUtils;
import org.ovirt.engine.core.common.utils.customprop.SimpleCustomPropertiesUtil;
import org.ovirt.engine.core.common.utils.customprop.ValidationError;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.dao.network.NetworkClusterDao;
import org.ovirt.engine.core.dao.network.NetworkDao;
import org.ovirt.engine.core.utils.NetworkUtils;
import org.ovirt.engine.core.utils.ReplacementUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HostSetupNetworksValidator {
    private static final Logger log = LoggerFactory.getLogger(HostSetupNetworksValidator.class);

    static final String VAR_BOND_NAME = "BondName";
    static final String VAR_NETWORK_NAME = "networkName";
    static final String VAR_VM_NAMES = "vmNames";
    static final String VAR_ATTACHMENT_IDS = "attachmentIds";
    static final String VAR_INTERFACE_NAME = "interfaceName";
    static final String VAR_NIC_ID = "nicId";
    static final String VAR_LABELED_NIC_NAME = "labeledNicName";
    static final String VAR_NIC_NAME = "nicName";
    static final String VAR_LABEL = "label";
    private static final String SEPARATOR = ",";

    private final NetworkExclusivenessValidator networkExclusivenessValidator;

    private HostSetupNetworksParameters params;
    private VDS host;
    private BusinessEntityMap<VdsNetworkInterface> existingInterfacesMap;
    private List<NetworkAttachment> existingAttachments;
    private NetworkAttachment currentDefaultRouteNetworkAttachment;
    private List<VdsNetworkInterface> removedBondVdsNetworkInterface;
    private BusinessEntityMap<VdsNetworkInterface> removedBondVdsNetworkInterfaceMap;
    private List<NetworkAttachment> removedNetworkAttachments;
    private BusinessEntityMap<Network> networkBusinessEntityMap;
    private final Map<Guid, NetworkAttachment> existingAttachmentsById;
    private final NetworkClusterDao networkClusterDao;
    private final NetworkDao networkDao;
    private final VdsDao vdsDao;
    private final BusinessEntityMap<CreateOrUpdateBond> createOrUpdateBondBusinessEntityMap;
    private final FindActiveVmsUsingNetwork findActiveVmsUsingNetwork;
    private Map<Guid, NetworkAttachment> existingAttachmentsByNetworkId;
    private Map<String, NicLabel> nicLabelByLabel;
    private HostSetupNetworksValidatorHelper hostSetupNetworksValidatorHelper;
    private List<VdsNetworkInterface> existingInterfaces;
    private NetworkAttachmentIpConfigurationValidator networkAttachmentIpConfigurationValidator;
    private UnmanagedNetworkValidator unmanagedNetworkValidator;
    private BackendInternal backendInternal;

    public HostSetupNetworksValidator(VDS host,
            HostSetupNetworksParameters params,
            List<VdsNetworkInterface> existingInterfaces,
            List<NetworkAttachment> existingAttachments,
            NetworkAttachment currentDefaultRouteNetworkAttachment,
            BusinessEntityMap<Network> networkBusinessEntityMap,
            NetworkClusterDao networkClusterDao,
            NetworkDao networkDao,
            VdsDao vdsDao,
            FindActiveVmsUsingNetwork findActiveVmsUsingNetwork,
            HostSetupNetworksValidatorHelper hostSetupNetworksValidatorHelper,
            NetworkExclusivenessValidatorResolver networkExclusivenessValidatorResolver,
            NetworkAttachmentIpConfigurationValidator networkAttachmentIpConfigurationValidator,
            UnmanagedNetworkValidator unmanagedNetworkValidator,
            BackendInternal backendInternal) {

        this.host = host;
        this.params = params;
        this.existingAttachments = existingAttachments;
        this.currentDefaultRouteNetworkAttachment = currentDefaultRouteNetworkAttachment;
        this.networkClusterDao = networkClusterDao;
        this.networkDao = networkDao;
        this.vdsDao = vdsDao;
        this.findActiveVmsUsingNetwork = findActiveVmsUsingNetwork;
        this.existingInterfacesMap = new BusinessEntityMap<>(existingInterfaces);
        this.networkBusinessEntityMap = networkBusinessEntityMap;
        this.existingInterfaces = existingInterfaces;

        this.removedBondVdsNetworkInterface = Entities.filterEntitiesByRequiredIds(params.getRemovedBonds(),
            existingInterfaces);
        this.removedBondVdsNetworkInterfaceMap = new BusinessEntityMap<>(removedBondVdsNetworkInterface);
        this.removedNetworkAttachments = Entities.filterEntitiesByRequiredIds(params.getRemovedNetworkAttachments(),
            existingAttachments);
        existingAttachmentsByNetworkId = new MapNetworkAttachments(existingAttachments).byNetworkId();

        networkExclusivenessValidator = networkExclusivenessValidatorResolver.resolveNetworkExclusivenessValidator();

        existingAttachmentsById = Entities.businessEntitiesById(existingAttachments);
        createOrUpdateBondBusinessEntityMap = new BusinessEntityMap<>(params.getCreateOrUpdateBonds());

        nicLabelByLabel = Entities.entitiesByName(params.getLabels());

        this.hostSetupNetworksValidatorHelper = hostSetupNetworksValidatorHelper;

        this.networkAttachmentIpConfigurationValidator = networkAttachmentIpConfigurationValidator;

        this.unmanagedNetworkValidator = unmanagedNetworkValidator;

        this.backendInternal = backendInternal;
    }

    List<String> translateErrorMessages(List<String> messages) {
        return backendInternal.getErrorsTranslator().translateErrorText(messages);
    }

    public ValidationResult validate() {
        Collection<NetworkAttachment> attachmentsToConfigure = getAttachmentsToConfigure();

        ValidationResult vr = ValidationResult.VALID;
        vr = skipValidation(vr) ? vr : new NicLabelValidator(params, existingInterfacesMap,
                createOrUpdateBondBusinessEntityMap, hostSetupNetworksValidatorHelper).validate();
        vr = skipValidation(vr) ? vr : validNewOrModifiedNetworkAttachments();
        vr = skipValidation(vr) ? vr : validRemovedNetworkAttachments();
        vr = skipValidation(vr) ? vr : validNewOrModifiedBonds();
        vr = skipValidation(vr) ? vr : validRemovedBonds(attachmentsToConfigure);
        vr = skipValidation(vr) ? vr : bondNotUpdatedAndRemovedSimultaneously();
        vr = skipValidation(vr) ? vr : attachmentsDontReferenceSameNetworkDuplicately(attachmentsToConfigure);
        vr = skipValidation(vr) ? vr : networksUniquelyConfiguredOnHost(attachmentsToConfigure);
        vr = skipValidation(vr) ? vr : validateNetworkExclusiveOnNics(attachmentsToConfigure);
        vr = skipValidation(vr) ? vr : new NetworkMtuValidator(networkBusinessEntityMap).validateMtu(
            attachmentsToConfigure);
        vr = skipValidation(vr) ? vr : validateCustomProperties();
        vr = skipValidation(vr) ? vr : validateQos(attachmentsToConfigure);
        vr = skipValidation(vr) ? vr : validateBondModeVsNetworksAttachedToIt(attachmentsToConfigure);
        vr = skipValidation(vr) ? vr : unmanagedNetworkValidator.validate(params, existingInterfaces, networkBusinessEntityMap);

        return vr;
    }

    protected ValidationResult validateBondModeVsNetworksAttachedToIt(
            Collection<NetworkAttachment> attachmentsToConfigure) {
        Map<String, VdsNetworkInterface> hostInterfacesByNetworkName =
                NetworkUtils.hostInterfacesByNetworkName(existingInterfaces);

        for (NetworkAttachment attachment : attachmentsToConfigure){
            if (!mustAttachementBeCheckedForBondMode(attachment, hostInterfacesByNetworkName)){
                continue;
            }
            CreateOrUpdateBond bondToCheck = createOrUpdateBondBusinessEntityMap.get(attachment.getNicName());

            if (bondToCheck == null){
                VdsNetworkInterface existingNetworkInterfaceForAttachement = existingInterfacesMap.get(attachment.getNicName());
                if(existingNetworkInterfaceForAttachement == null || !existingNetworkInterfaceForAttachement.isBond()){
                    continue;
                }
                bondToCheck = CreateOrUpdateBond.fromBond((Bond) existingNetworkInterfaceForAttachement);
            }

            String networkLabel = networkBusinessEntityMap.get(attachment.getNetworkName()).getLabel();
            ValidationResult validationResult = checkBondMode(bondToCheck, networkLabel, attachment.getNetworkName());
            if (!validationResult.isValid()){
                return validationResult;
            }
        }
        return ValidationResult.VALID;
    }

    private ValidationResult checkBondMode(CreateOrUpdateBond createOrUpdateBond, String networkLabel, String networkName) {
        if (BondMode.isBondModeValidForVmNetwork(createOrUpdateBond.getBondOptions())){
            return ValidationResult.VALID;
        }

        if (networkLabel != null && isNicToConfigureContainTheLabel(createOrUpdateBond.getName(), networkLabel)){
            return new ValidationResult(EngineMessage.INVALID_BOND_MODE_FOR_BOND_WITH_LABELED_VM_NETWORK,
                    ReplacementUtils.createSetVariableString(VAR_BOND_NAME, createOrUpdateBond.getName()),
                    ReplacementUtils.createSetVariableString(VAR_LABEL, networkLabel),
                    ReplacementUtils.createSetVariableString(VAR_NETWORK_NAME, networkName));
        }

        return new ValidationResult(EngineMessage.INVALID_BOND_MODE_FOR_BOND_WITH_VM_NETWORK,
                ReplacementUtils.createSetVariableString(VAR_BOND_NAME, createOrUpdateBond.getName()),
                ReplacementUtils.createSetVariableString(VAR_NETWORK_NAME, networkName));
    }

    private boolean mustAttachementBeCheckedForBondMode(NetworkAttachment attachment,
            Map<String, VdsNetworkInterface> hostInterfacesByNetworkName){
        Network network = networkBusinessEntityMap.get(attachment.getNetworkName());
        String networkName = attachment.getNetworkName();
        if (!network.isVmNetwork()){
            return false;
        }
        VdsNetworkInterface nic = hostInterfacesByNetworkName.get(networkName);
        if (nic == null){
            return true;
        }
        NetworkImplementationDetails networkImplementationDetails = nic.getNetworkImplementationDetails();
        return networkImplementationDetails == null || networkImplementationDetails.isInSync()
                || attachment.isOverrideConfiguration();
    }

    private ValidationResult validateQos(Collection<NetworkAttachment> attachmentsToConfigure) {
        ValidationResult vr = ValidationResult.VALID;

        vr = skipValidation(vr) ? vr : validateQosOverriddenInterfaces();
        vr = skipValidation(vr) ? vr : validateQosNotPartiallyConfigured(attachmentsToConfigure);
        return vr;
    }

    private ValidationResult attachmentsDontReferenceSameNetworkDuplicately(Collection<NetworkAttachment> attachments) {
        return new NetworkAttachmentsValidator(attachments, networkBusinessEntityMap, networkExclusivenessValidator)
            .verifyUserAttachmentsDoesNotReferenceSameNetworkDuplicately();
    }

    /**
     * Validates that the feature is supported if any QoS configuration was specified, and that the values associated
     * with it are valid.
     */
    ValidationResult validateQosOverriddenInterfaces() {
        for (NetworkAttachment networkAttachment : params.getNetworkAttachments()) {
            if (networkAttachment.isQosOverridden()) {
                Network network = getNetworkRelatedToAttachment(networkAttachment);
                String networkName = network.getName();

                HostNetworkQos hostNetworkQos =
                        HostNetworkQos.fromAnonymousHostNetworkQos(networkAttachment.getHostNetworkQos());
                HostNetworkQosValidator qosValidator = createHostNetworkQosValidator(hostNetworkQos);

                ValidationResult requiredValuesPresent =
                    qosValidator.requiredQosValuesPresentForOverriding(networkName);
                if (!requiredValuesPresent.isValid()) {
                    return requiredValuesPresent;
                }

                ValidationResult valuesConsistent = qosValidator.valuesConsistent(networkName);
                if (!valuesConsistent.isValid()) {
                    return valuesConsistent;
                }
            }
        }

        return ValidationResult.VALID;
    }

    HostNetworkQosValidator createHostNetworkQosValidator(HostNetworkQos hostNetworkQos) {
        return new HostNetworkQosValidator(hostNetworkQos);
    }

    private Network getNetworkRelatedToAttachment(NetworkAttachment networkAttachment) {
        Guid networkId = networkAttachment.getNetworkId();
        return getNetworkByNetworkId(networkId);
    }

    private Network getNetworkByNetworkId(Guid networkId) {
        return networkBusinessEntityMap.get(networkId);
    }

    /**
     * Ensure that either none or all of the networks on a single interface have QoS configured on them.
     */
    ValidationResult validateQosNotPartiallyConfigured(Collection<NetworkAttachment> attachmentsToConfigure) {
        Set<String> someSubInterfacesHaveQos = new HashSet<>();
        Set<String> notAllSubInterfacesHaveQos = new HashSet<>();

        // first map which interfaces have some QoS configured on them, and which interfaces lack some QoS configuration
        for (NetworkAttachment networkAttachment : attachmentsToConfigure) {
            Network network = getNetworkRelatedToAttachment(networkAttachment);
            if (NetworkUtils.qosConfiguredOnInterface(networkAttachment, network)) {
                someSubInterfacesHaveQos.add(networkAttachment.getNicName());
            } else {
                notAllSubInterfacesHaveQos.add(networkAttachment.getNicName());
            }
        }

        // if any base interface has some sub-interfaces with QoS and some without - this is a partial configuration
        for (String ifaceName : someSubInterfacesHaveQos) {
            if (notAllSubInterfacesHaveQos.contains(ifaceName)) {
                return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_HOST_NETWORK_QOS_INTERFACES_WITHOUT_QOS,
                    ReplacementUtils.createSetVariableString(
                        "ACTION_TYPE_FAILED_HOST_NETWORK_QOS_INTERFACES_WITHOUT_QOS_LIST",
                        ifaceName));
            }
        }

        return ValidationResult.VALID;
    }

    private ValidationResult validateNetworkExclusiveOnNics(Collection<NetworkAttachment> attachmentsToConfigure) {
        NetworkAttachmentsValidator validator =
            new NetworkAttachmentsValidator(attachmentsToConfigure, networkBusinessEntityMap, networkExclusivenessValidator);
        return validator.validateNetworkExclusiveOnNics();
    }

    ValidationResult networksUniquelyConfiguredOnHost(Collection<NetworkAttachment> attachmentsToConfigure) {
        Set<Guid> usedNetworkIds = new HashSet<>(attachmentsToConfigure.size());
        for (NetworkAttachment attachment : attachmentsToConfigure) {
            boolean alreadyUsedNetworkId = usedNetworkIds.contains(attachment.getNetworkId());
            if (alreadyUsedNetworkId) {
                Network network = existingNetworkRelatedToAttachment(attachment);
                EngineMessage engineMessage = EngineMessage.NETWORKS_ALREADY_ATTACHED_TO_IFACES;
                return new ValidationResult(engineMessage,
                    ReplacementUtils.getVariableAssignmentStringWithMultipleValues(engineMessage, network.getName()));
            } else {
                usedNetworkIds.add(attachment.getNetworkId());
            }
        }

        return ValidationResult.VALID;
    }

    @SuppressWarnings("unchecked")
    ValidationResult validateNotRemovingUsedNetworkByVms(String removedNetworkName) {
        final List<String> removedNetworkNames = Collections.singletonList(removedNetworkName);
        final List<String> vmsNames =
                findActiveVmsUsingNetwork.findNamesOfActiveVmsUsingNetworks(host.getId(), removedNetworkNames);

        if (vmsNames.isEmpty()) {
            return ValidationResult.VALID;
        }

        EngineMessage engineMessage = EngineMessage.NETWORK_CANNOT_DETACH_NETWORK_USED_BY_VMS;
        return new ValidationResult(engineMessage,
                Stream.concat(
                        ReplacementUtils.replaceWith(VAR_NETWORK_NAME, removedNetworkNames, SEPARATOR).stream(),
                        ReplacementUtils.replaceWith(VAR_VM_NAMES, vmsNames, SEPARATOR).stream()
                ).collect(Collectors.toList()));
    }

    ValidationResult validRemovedBonds(Collection<NetworkAttachment> attachmentsToConfigure) {
        List<Guid> invalidBondIds = Entities.idsNotReferencingExistingRecords(params.getRemovedBonds(),
            existingInterfacesMap.unmodifiableEntitiesByIdMap());
        if (!invalidBondIds.isEmpty()) {
            EngineMessage engineMessage = EngineMessage.NETWORK_BOND_RECORDS_DOES_NOT_EXISTS;
            return new ValidationResult(engineMessage,
                ReplacementUtils.getListVariableAssignmentString(engineMessage, invalidBondIds));

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

                EngineMessage engineMessage = EngineMessage.BOND_USED_BY_NETWORK_ATTACHMENTS;

                List<String> replacements = new ArrayList<>();
                replacements.add(ReplacementUtils.getVariableAssignmentString(engineMessage, bondName));
                replacements.addAll(ReplacementUtils.replaceWith(VAR_ATTACHMENT_IDS, networkAttachmentsForNic));

                return new ValidationResult(engineMessage, replacements);

            }
        }

        return ValidationResult.VALID;
    }

    private Map<String, List<Guid>> getIdsOfNetworkAttachmentsRelatedToInterfaceNames(Collection<NetworkAttachment> networkAttachments) {
        return networkAttachments.stream().collect(Collectors.groupingBy(
                NetworkAttachment::getNicName, Collectors.mapping(NetworkAttachment::getId, Collectors.toList())));
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

        for (NetworkAttachment existingAttachment : existingAttachments) {
            Guid existingAttachmentId = existingAttachment.getId();
            if (!networkAttachmentsMap.containsKey(existingAttachmentId) &&
                !params.getRemovedNetworkAttachments().contains(existingAttachmentId)) {
                networkAttachmentsMap.put(existingAttachmentId, existingAttachment);
            }
        }

        List<NetworkAttachment> result = new ArrayList<>(networkAttachmentsMap.values());
        result.addAll(newAttachments);
        return result;
    }

    public ValidationResult bondNotUpdatedAndRemovedSimultaneously() {

        List<String> bondsUpdatedAndRemovedSimultaneously = params.getCreateOrUpdateBonds()
                .stream()
                .filter(bond -> params.getRemovedBonds().contains(bond.getId()))
                .map(CreateOrUpdateBond::getName)
                .collect(Collectors.toList());

        if (CollectionUtils.isNotEmpty(bondsUpdatedAndRemovedSimultaneously)) {
            EngineMessage engineMessage = EngineMessage.BONDS_UPDATED_AND_REMOVED_SIMULTANEOUSLY;
            return new ValidationResult(engineMessage,
                    ReplacementUtils.getListVariableAssignmentString(engineMessage,
                            bondsUpdatedAndRemovedSimultaneously));
        }

        return ValidationResult.VALID;
    }

    ValidationResult validNewOrModifiedBonds() {
        for (CreateOrUpdateBond modifiedOrNewBond : params.getCreateOrUpdateBonds()) {
            String bondName = modifiedOrNewBond.getName();
            Guid bondId = modifiedOrNewBond.getId();

            /*
             * following code relies on fact, that completors were ran and fixed the user input, so we need to consider
             * what original user input looked like, and how it was altered by completors.
             */

            /*
             * bondId is provided, but bondName not. This means that user attempted update, but bond of such ID does not
             * exit, thus completors did not complete name.
             */
            if (bondId != null && bondName == null) {
                return new ValidationResult(EngineMessage.HOST_NETWORK_INTERFACE_HAVING_ID_DOES_NOT_EXIST,
                        ReplacementUtils.createSetVariableString(VAR_NIC_ID, bondId));
            }

            /*
             * user did not provide neither bondId nor bondName. That means he probably attempted new bond creation
             * but forgot to provide bondName.
             */
            if (bondId == null && bondName == null) {
                return new ValidationResult(EngineMessage.BOND_DOES_NOT_HAVE_NEITHER_ID_NOR_NAME_SPECIFIED);
            }


            /*
             * if (bondId == null && bondName != null) …
             * User provided only bondName, and completors failed to find existing bonds id for that name.
             * We cannot tell, what user wanted to do(create/update). We have to assume, it's new record creation, which
             * is valid scenario.
             */

            ValidationResult validateCoherentNicIdentification = validateCoherentNicIdentification(modifiedOrNewBond);
            if (!validateCoherentNicIdentification.isValid()) {
                return validateCoherentNicIdentification;
            }

            boolean validBondName =
                    bondName.matches(FeatureSupported.isCustomBondNameSupported(host.getClusterCompatibilityVersion()) ?
                            BusinessEntitiesDefinitions.BOND_NAME_PATTERN :
                            BusinessEntitiesDefinitions.NUM_ONLY_BOND_NAME_PATTERN);

            if (!validBondName) {
                EngineMessage engineMessage = EngineMessage.NETWORK_BOND_NAME_BAD_FORMAT;
                return new ValidationResult(engineMessage,
                    ReplacementUtils.getVariableAssignmentString(engineMessage, bondName));

            }

            //either it's newly create bond, thus non existing, or given name must reference existing bond.
            ValidationResult interfaceIsBondOrNull = createHostInterfaceValidator(existingInterfacesMap.get(bondName)).interfaceIsBondOrNull();
            if (!interfaceIsBondOrNull.isValid()) {
                return interfaceIsBondOrNull;
            }

            //count of bond slaves must be at least two.
            if (modifiedOrNewBond.getSlaves().size() < 2) {
                return new ValidationResult(EngineMessage.NETWORK_BONDS_INVALID_SLAVE_COUNT,
                    ReplacementUtils.getVariableAssignmentString(EngineMessage.NETWORK_BONDS_INVALID_SLAVE_COUNT,
                        bondName));

            }

            ValidationResult validateModifiedBondSlaves = validateModifiedBondSlaves(modifiedOrNewBond);
            if (!validateModifiedBondSlaves.isValid()) {
                return validateModifiedBondSlaves;
            }
        }

        return ValidationResult.VALID;
    }

    ValidationResult validateModifiedBondSlaves(CreateOrUpdateBond modifiedOrNewBond) {

        for (String slaveName : modifiedOrNewBond.getSlaves()) {
            VdsNetworkInterface potentialSlave = existingInterfacesMap.get(slaveName);
            HostInterfaceValidator slaveHostInterfaceValidator = createHostInterfaceValidator(potentialSlave);

            ValidationResult interfaceExists = slaveHostInterfaceValidator.interfaceExists(slaveName);
            if (!interfaceExists.isValid()) {
                return interfaceExists;
            }

            ValidationResult interfaceIsValidSlave = slaveHostInterfaceValidator.interfaceIsValidSlave();
            if (!interfaceIsValidSlave.isValid()) {
                return interfaceIsValidSlave;
            }
            /*
             * definition of currently processed bond references this slave, but this slave already 'slaves' for
             * another bond. This is ok only when this bond will be removed as a part of this request
             * or the slave will be removed from its former bond, as a part of this request.
             */
            String currentSlavesBondName = potentialSlave.getBondName();
            if (potentialSlave.isPartOfBond() &&
                        /*
                         * we're creating new bond, and it's definition contains reference to slave already assigned
                         * to a different bond.
                         */
                (!potentialSlave.isPartOfBond(modifiedOrNewBond.getName())
                    //… but this bond is also removed in this request, so it's ok.
                    && !isBondRemoved(currentSlavesBondName)

                    //… or slave was removed from its former bond
                    && !bondIsUpdatedAndDoesNotContainCertainSlave(slaveName, currentSlavesBondName))) {


                EngineMessage engineMessage = EngineMessage.NETWORK_INTERFACE_ALREADY_IN_BOND;
                return new ValidationResult(engineMessage,
                    ReplacementUtils.getVariableAssignmentString(engineMessage, slaveName));

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
                    EngineMessage engineMessage = EngineMessage.NETWORK_INTERFACE_ADDED_TO_BOND_AND_NETWORK_IS_ATTACHED_TO_IT_AT_THE_SAME_TIME;
                    return new ValidationResult(engineMessage,
                            ReplacementUtils.getVariableAssignmentString(engineMessage, slaveName),
                        ReplacementUtils.createSetVariableString(VAR_NETWORK_NAME, attachment.getNetworkName()));
                } else {
                    EngineMessage engineMessage = EngineMessage.NETWORK_INTERFACE_ATTACHED_TO_NETWORK_CANNOT_BE_SLAVE;
                    return new ValidationResult(engineMessage,
                            ReplacementUtils.getVariableAssignmentString(engineMessage, slaveName),
                        ReplacementUtils.createSetVariableString(VAR_NETWORK_NAME, attachment.getNetworkName()));
                }
            }
        }

        for (VdsNetworkInterface iface : existingInterfaces) {
            if (slaveName.equals(NetworkCommonUtils.stripVlan(iface))) {
                if (iface.getNetworkImplementationDetails() != null &&
                    !iface.getNetworkImplementationDetails().isManaged()) {
                    return new ValidationResult(EngineMessage.NETWORK_INTERFACE_WITH_UNMANAGED_NETWORK_CANNOT_BE_SLAVE,
                            ReplacementUtils.createSetVariableString(VAR_NETWORK_NAME, iface.getNetworkName()),
                            ReplacementUtils.createSetVariableString(VAR_NIC_NAME, slaveName));
                }
            }
        }

        return ValidationResult.VALID;
    }

    private boolean slaveUsedMultipleTimesInDifferentBonds(String potentiallyDuplicateSlaveName) {
        int count = 0;
        for (CreateOrUpdateBond createOrUpdateBond : params.getCreateOrUpdateBonds()) {
            for (String slaveName : createOrUpdateBond.getSlaves()) {
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
        CreateOrUpdateBond createOrUpdateBond = this.createOrUpdateBondBusinessEntityMap.get(bondName);
        return createOrUpdateBond != null && !createOrUpdateBond.getSlaves().contains(slaveName);
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

    ValidationResult validNewOrModifiedNetworkAttachments() {
        ValidationResult vr = ValidationResult.VALID;

        Iterator<NetworkAttachment> iterator = params.getNetworkAttachments().iterator();
        while (iterator.hasNext() && vr.isValid()) {
            NetworkAttachment attachment = iterator.next();
            NetworkAttachmentValidator validator = createNetworkAttachmentValidator(attachment);

            vr = skipValidation(vr) ? vr : validator.networkAttachmentIsSet();
            vr = skipValidation(vr) ? vr : modifiedAttachmentExists(attachment.getId());

            vr = skipValidation(vr) ? vr : validator.networkExists();
            vr = skipValidation(vr) ? vr : validateCoherentNicIdentification(attachment);
            vr = skipValidation(vr) ? vr : validateCoherentNetworkIdentification(attachment);
            vr = skipValidation(vr) ? vr : modifiedAttachmentNotRemoved(attachment);
            vr = skipValidation(vr) ? vr : validateAttachmentNotReferenceVlanDevice(attachment);
            vr = skipValidation(vr) ? vr : validator.existingAttachmentIsReused(existingAttachmentsByNetworkId);
            vr = skipValidation(vr) ? vr : validateAttachmentAndNicReferenceSameLabelNotConflict(attachment);
            vr = skipValidation(vr) ? vr : validator.notExternalNetwork();
            vr = skipValidation(vr) ? vr : validator.networkAttachedToCluster();
            vr = skipValidation(vr) ? vr : validator.bootProtocolSetForRoleNetwork();

            vr = skipValidation(vr) ? vr : validator.nicNameIsSet();
            vr = skipValidation(vr) ? vr : nicActuallyExistsOrReferencesNewBond(attachment);

            vr = skipValidation(vr) ? vr : validator.networkNotChanged(existingAttachmentsById.get(attachment.getId()));
            vr = skipValidation(vr) ? vr : networkAttachmentIpConfigurationValidator.validateNetworkAttachmentIpConfiguration(
                params.getNetworkAttachments(), currentDefaultRouteNetworkAttachment, isEl8(host.getKernelVersion()));

            boolean attachmentUpdated = !isNewAttachment(attachment.getId());
            if (attachmentUpdated) {
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

    private ValidationResult validateCoherentNicIdentification(CreateOrUpdateBond createOrUpdateBond) {
        Guid nicId = createOrUpdateBond.getId();
        String nicName = createOrUpdateBond.getName();
        EngineMessage message = EngineMessage.BOND_REFERENCES_NICS_INCOHERENTLY;
        return hostSetupNetworksValidatorHelper.validateCoherentIdentification(nicName, nicId, nicName, message, existingInterfacesMap);
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

        EngineMessage engineMessage = EngineMessage.MODIFIED_NETWORK_ATTACHMENT_DOES_NOT_EXISTS;
        String replacement = ReplacementUtils.getVariableAssignmentString(engineMessage, networkAttachmentId.toString());
        return new ValidationResult(engineMessage, replacement);
    }

    ValidationResult modifiedAttachmentNotRemoved(NetworkAttachment networkAttachment) {
        Guid networkAttachmentId = networkAttachment.getId();
        if (isNewAttachment(networkAttachmentId)) {
            return ValidationResult.VALID;
        }

        boolean attachmentInRemoveList = params.getRemovedNetworkAttachments().contains(networkAttachmentId);

        EngineMessage engineMessage = EngineMessage.NETWORK_ATTACHMENT_IN_BOTH_LISTS;
        return ValidationResult.failWith(engineMessage,
                ReplacementUtils.getVariableAssignmentString(engineMessage, networkAttachmentId.toString()))
            .when(attachmentInRemoveList);

    }

    private boolean isNewAttachment(Guid networkAttachmentId) {
        return networkAttachmentId == null;
    }

    private ValidationResult nicActuallyExistsOrReferencesNewBond(NetworkAttachment attachment) {
        String nicName = attachment.getNicName();
        Guid nicId = attachment.getNicId();

        boolean nicActuallyExistsOrReferencesNewBond =
                isNicActuallyExistsOrReferencesNewBond(nicName, nicId);

        return ValidationResult.failWith(EngineMessage.HOST_NETWORK_INTERFACE_HAVING_ID_OR_NAME_DOES_NOT_EXIST,
            ReplacementUtils.createSetVariableString(VAR_NIC_ID, nicId),
            ReplacementUtils.createSetVariableString(VAR_NIC_NAME, nicName)
        ).when(!nicActuallyExistsOrReferencesNewBond);
    }

    private boolean isNicActuallyExistsOrReferencesNewBond(String nicName, Guid nicId) {
        return hostSetupNetworksValidatorHelper.isNicActuallyExistsOrReferencesNewBond(existingInterfacesMap,
                createOrUpdateBondBusinessEntityMap, nicName, nicId);
    }

    private ValidationResult validRemovedNetworkAttachments() {
        List<Guid> invalidIds = Entities.idsNotReferencingExistingRecords(params.getRemovedNetworkAttachments(),
            existingAttachments);
        if (!invalidIds.isEmpty()) {
            EngineMessage engineMessage = EngineMessage.NETWORK_ATTACHMENTS_TO_BE_REMOVED_DOES_NOT_EXISTS;
            return new ValidationResult(engineMessage,
                    ReplacementUtils.getListVariableAssignmentString(engineMessage, invalidIds));
        }

        ValidationResult vr = ValidationResult.VALID;

        Iterator<NetworkAttachment> iterator = removedNetworkAttachments.iterator();
        while (iterator.hasNext() && vr.isValid()) {
            NetworkAttachment attachment = iterator.next();
            NetworkAttachmentValidator validator = createNetworkAttachmentValidator(attachment);

            vr = skipValidation(vr) ? vr : validator.networkAttachmentIsSet();
            vr = skipValidation(vr) ? vr : validator.notExternalNetwork();

            vr = skipValidation(vr) ? vr : validator.notRemovingManagementNetwork();
            vr = skipValidation(vr) ? vr : notRemovingLabeledNetworks(attachment);
            vr = skipValidation(vr) ? vr : validateNotRemovingUsedNetworkByVms(attachment.getNetworkName());
            vr = skipValidation(vr) ?
                    vr :
                    validateNotRemovingPhysicalNetworksLinkedToExternalUsedByVMs(attachment.getNetworkId(),
                            attachment.getNetworkName());
        }

        return vr;
    }

    private NetworkAttachmentValidator createNetworkAttachmentValidator(NetworkAttachment attachmentToValidate) {
        return new NetworkAttachmentValidator(attachmentToValidate, host, networkClusterDao, networkDao, vdsDao);
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

        EngineMessage engineMessage = EngineMessage.ACTION_TYPE_FAILED_CANNOT_REMOVE_LABELED_NETWORK_FROM_NIC;
        return ValidationResult.failWith(engineMessage,
                ReplacementUtils.getVariableAssignmentStringWithMultipleValues(engineMessage, removedNetwork.getName()))
                .when(isNicToConfigureContainTheLabel(attachment.getNicName(), removedNetwork.getLabel()));

    }

    ValidationResult notMovingLabeledNetworkToDifferentNic(NetworkAttachment newOrModifedAttachmet) {
        Network movedNetwork = existingNetworkRelatedToAttachment(newOrModifedAttachmet);

        if (!NetworkUtils.isLabeled(movedNetwork)) {
            return ValidationResult.VALID;
        }

        NetworkAttachment existingAttachment = existingAttachmentsById.get(newOrModifedAttachmet.getId());
        boolean movedToDifferentNic = !existingAttachment.getNicId().equals(newOrModifedAttachmet.getNicId());

        EngineMessage engineMessage = EngineMessage.ACTION_TYPE_FAILED_CANNOT_MOVE_LABELED_NETWORK_TO_ANOTHER_NIC;
        return ValidationResult.failWith(engineMessage,
                ReplacementUtils.createSetVariableString(VAR_NETWORK_NAME, movedNetwork.getName()),
                ReplacementUtils.getVariableAssignmentString(engineMessage, movedNetwork.getLabel()))
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

        EngineMessage engineMessage = EngineMessage.NETWORK_SHOULD_BE_ATTACHED_VIA_LABEL_TO_ANOTHER_NIC;
        return ValidationResult.failWith(engineMessage,
                ReplacementUtils.getVariableAssignmentString(engineMessage, network.getName()),
                ReplacementUtils.createSetVariableString(VAR_NIC_NAME, attachment.getNicName()),
                ReplacementUtils.createSetVariableString(VAR_LABELED_NIC_NAME, nicThatShouldHaveTheLabel))
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
        String version = host.getClusterCompatibilityVersion().getValue();
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
                List<ValidationError> errors =
                    util.validateProperties(network.isVmNetwork() ? validPropertiesForVm : validPropertiesForNonVm,
                        attachment.getProperties());
                if (!errors.isEmpty()) {
                    handleCustomPropertiesError(util, errors);
                    EngineMessage engineMessage = EngineMessage.ACTION_TYPE_FAILED_NETWORK_CUSTOM_PROPERTIES_BAD_INPUT;
                    return new ValidationResult(engineMessage,
                        ReplacementUtils.getVariableAssignmentStringWithMultipleValues(engineMessage, network.getName()));
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
        EngineMessage engineMessage = EngineMessage.ATTACHMENT_REFERENCE_VLAN_DEVICE;
        return ValidationResult.failWith(engineMessage,
                ReplacementUtils.getVariableAssignmentString(engineMessage, attachment.getNetworkName()),
                ReplacementUtils.createSetVariableString(VAR_NIC_NAME, attachment.getNicName()))
            .when(nic != null && NetworkCommonUtils.isVlan(nic));
    }

    private Network existingNetworkRelatedToAttachment(NetworkAttachment attachment) {
        return networkBusinessEntityMap.get(attachment.getNetworkId());
    }

    private boolean skipValidation(ValidationResult validationResult) {
        return !validationResult.isValid();
    }

    protected ValidationResult validateNotRemovingPhysicalNetworksLinkedToExternalUsedByVMs(Guid physicalNetworkId,
            String physicalNetworkName) {
        List<String> physicalNetworks = Collections.singletonList(physicalNetworkName);
        List<String> externalNetworks = networkDao.getAllExternalNetworksLinkedToPhysicalNetwork(physicalNetworkId)
                .stream()
                .map(Network::getName)
                .collect(Collectors.toList());

        List<String> vmsNames =
                findActiveVmsUsingNetwork.findNamesOfActiveVmsUsingNetworks(host.getId(), externalNetworks);

        if (vmsNames.isEmpty()) {
            return ValidationResult.VALID;
        }

        EngineMessage engineMessage =
                EngineMessage.NETWORK_CANNOT_DETACH_PHYSICAL_NETWORK_LINKED_TO_EXTERNAL_USED_BY_VM;
        return new ValidationResult(engineMessage,
                Stream.concat(
                        ReplacementUtils.replaceWith(VAR_NETWORK_NAME, physicalNetworks, SEPARATOR).stream(),
                        ReplacementUtils.replaceWith(VAR_VM_NAMES, vmsNames, SEPARATOR).stream()
                ).collect(Collectors.toList()));
    }
}

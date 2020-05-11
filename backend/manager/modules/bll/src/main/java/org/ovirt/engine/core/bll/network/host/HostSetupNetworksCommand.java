package org.ovirt.engine.core.bll.network.host;

import static org.ovirt.engine.core.common.AuditLogType.NETWORK_IPV4_GATEWAY_OUT_OF_RANGE;
import static org.ovirt.engine.core.common.vdscommands.TimeBoundPollVDSCommandParameters.PollTechnique.CONFIRM_CONNECTIVITY;
import static org.ovirt.engine.core.utils.NetworkUtils.areDifferentId;
import static org.ovirt.engine.core.utils.NetworkUtils.hasIpv6StaticBootProto;
import static org.ovirt.engine.core.utils.network.predicate.IsDefaultRouteOnInterfacePredicate.isDefaultRouteOnInterfacePredicate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.validation.groups.Default;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.ovirt.engine.core.bll.HostLocking;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.VdsCommand;
import org.ovirt.engine.core.bll.VdsHandler;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.network.FindActiveVmsUsingNetwork;
import org.ovirt.engine.core.bll.network.cluster.ManageNetworkClustersCommand;
import org.ovirt.engine.core.bll.network.cluster.ManagementNetworkUtil;
import org.ovirt.engine.core.bll.network.cluster.NetworkClusterHelper;
import org.ovirt.engine.core.bll.validator.network.NetworkAttachmentIpConfigurationValidator;
import org.ovirt.engine.core.bll.validator.network.NetworkExclusivenessValidatorResolver;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.CreateOrUpdateBond;
import org.ovirt.engine.core.common.action.HostSetupNetworksParameters;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.LockProperties.Scope;
import org.ovirt.engine.core.common.businessentities.BusinessEntity;
import org.ovirt.engine.core.common.businessentities.BusinessEntityMap;
import org.ovirt.engine.core.common.businessentities.Entities;
import org.ovirt.engine.core.common.businessentities.SeparateNewAndModifiedInstances;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.network.Bond;
import org.ovirt.engine.core.common.businessentities.network.DnsResolverConfiguration;
import org.ovirt.engine.core.common.businessentities.network.HostNetworkQos;
import org.ovirt.engine.core.common.businessentities.network.IPv4Address;
import org.ovirt.engine.core.common.businessentities.network.Ipv4BootProtocol;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkAttachment;
import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;
import org.ovirt.engine.core.common.businessentities.network.NicLabel;
import org.ovirt.engine.core.common.businessentities.network.ReportedConfiguration;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface.NetworkImplementationDetails;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.interfaces.FutureVDSCall;
import org.ovirt.engine.core.common.network.SwitchType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.common.utils.MapNetworkAttachments;
import org.ovirt.engine.core.common.utils.NetworkCommonUtils;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.utils.ValidationUtils;
import org.ovirt.engine.core.common.validation.CidrValidator;
import org.ovirt.engine.core.common.validation.group.CreateEntity;
import org.ovirt.engine.core.common.validation.group.UpdateEntity;
import org.ovirt.engine.core.common.vdscommands.FutureVDSCommandType;
import org.ovirt.engine.core.common.vdscommands.HostNetwork;
import org.ovirt.engine.core.common.vdscommands.HostSetupNetworksVdsCommandParameters;
import org.ovirt.engine.core.common.vdscommands.TimeBoundPollVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.UserConfiguredNetworkData;
import org.ovirt.engine.core.common.vdscommands.UserOverriddenNicValues;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.VdsIdAndVdsVDSCommandParametersBase;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogable;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableImpl;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.dao.VdsDynamicDao;
import org.ovirt.engine.core.dao.network.InterfaceDao;
import org.ovirt.engine.core.dao.network.NetworkAttachmentDao;
import org.ovirt.engine.core.dao.network.NetworkClusterDao;
import org.ovirt.engine.core.dao.network.NetworkDao;
import org.ovirt.engine.core.utils.NetworkUtils;
import org.ovirt.engine.core.utils.ReplacementUtils;
import org.ovirt.engine.core.utils.lock.EngineLock;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;
import org.ovirt.engine.core.vdsbroker.EffectiveHostNetworkQos;
import org.ovirt.engine.core.vdsbroker.NetworkImplementationDetailsUtils;
import org.ovirt.engine.core.vdsbroker.vdsbroker.HostNetworkTopologyPersister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@NonTransactiveCommandAttribute
public class HostSetupNetworksCommand<T extends HostSetupNetworksParameters> extends VdsCommand<T> {

    private static final Logger log = LoggerFactory.getLogger(HostSetupNetworksCommand.class);

    private static final String DEFAULT_BOND_OPTIONS = "mode=4 miimon=100 xmit_hash_policy=2";

    private BusinessEntityMap<Network> networkBusinessEntityMap;

    private Set<String> removedNetworks;
    private Set<String> removedBondNames;
    private List<VdsNetworkInterface> removedBonds;
    private Set<String> removedUnmanagedNetworks;
    private List<VdsNetworkInterface> existingNics;
    private List<NetworkAttachment> existingAttachments;
    private BusinessEntityMap<VdsNetworkInterface> existingNicsBusinessEntityMap;
    private List<Network> clusterNetworks;

    @Inject
    private HostNetworkTopologyPersister hostNetworkTopologyPersister;

    @Inject
    private FindActiveVmsUsingNetwork findActiveVmsUsingNetwork;

    private List<Network> modifiedNetworks;

    @Inject
    private NetworkAttachmentDao networkAttachmentDao;

    @Inject
    private IpConfigurationCompleter ipConfigurationCompleter;

    @Inject
    private NetworkIdNetworkNameCompleter networkIdNetworkNameCompleter;

    @Inject
    HostSetupNetworksValidatorHelper hostSetupNetworksValidatorHelper;

    @Inject
    private EffectiveHostNetworkQos effectiveHostNetworkQos;

    @Inject
    private NetworkImplementationDetailsUtils networkImplementationDetailsUtils;

    @Inject
    private NetworkExclusivenessValidatorResolver networkExclusivenessValidatorResolver;

    @Inject
    private NetworkAttachmentIpConfigurationValidator networkAttachmentIpConfigurationValidator;

    @Inject
    private UnmanagedNetworkValidator unmanagedNetworkValidator;

    @Inject
    private ManagementNetworkUtil managementNetworkUtil;

    @Inject
    private HostLocking hostLocking;

    @Inject
    private NetworkClusterHelper networkClusterHelper;
    @Inject
    private NetworkClusterDao networkClusterDao;
    @Inject
    private NetworkDao networkDao;
    @Inject
    private VdsDao vdsDao;
    @Inject
    private InterfaceDao interfaceDao;
    @Inject
    private VdsDynamicDao vdsDynamicDao;

    @Inject
    private ReportedConfigurationsFiller reportedConfigurationsFiller;

    public HostSetupNetworksCommand(T parameters) {
        this(parameters, null);
    }

    public HostSetupNetworksCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
        setVdsId(parameters.getVdsId());
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__ACTION__SETUP);
        addValidationMessage(EngineMessage.VAR__TYPE__NETWORKS);
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        int timeout = Config.<Integer> getValue(ConfigValues.SetupNetworksWaitTimeoutSeconds);
        return lockProperties.withScope(Scope.Execution).withWaitTimeout(TimeUnit.SECONDS.toMillis(timeout));
    }

    @Override
    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        return hostLocking.getSetupNetworksLock(getParameters().getVdsId());
    }

    @Override
    protected boolean validate() {
        VDS host = getVds();

        final ValidationResult hostValidatorResult = new HostValidator(host, isInternalExecution()).validate();
        if (!hostValidatorResult.isValid()) {
            return validate(hostValidatorResult);
        }

        completeMissingDataInParameters();
        boolean requestValid = validateEntitiesFromRequest(getParameters().getNetworkAttachments()) &&
                validateEntitiesFromRequest(getParameters().getCreateOrUpdateBonds());

        if (!requestValid) {
            return requestValid;
        }

        fillInUnsetBondingOptions();

        IdQueryParameters idParameters = new IdQueryParameters(getVdsId());
        QueryReturnValue existingBondsResponse = runInternalQuery(QueryType.GetHostBondsByHostId, idParameters);
        if (!existingBondsResponse.getSucceeded()) {
            return false;
        }
        List<VdsNetworkInterface> existingBonds = existingBondsResponse.getReturnValue();

        removeUnchangedAttachments();
        removeUnchangedBonds(existingBonds);

        ValidationResult hostSetupNetworkValidatorResult = validateWithHostSetupNetworksValidator(host);
        if (!hostSetupNetworkValidatorResult.isValid()) {
            return validate(hostSetupNetworkValidatorResult);
        }

        return validate(checkForOutOfSyncNetworks());
    }

    private void completeMissingDataInParameters() {
        NicNameNicIdCompleter nicNameNicIdCompleter = new NicNameNicIdCompleter(getExistingNics());
        nicNameNicIdCompleter.completeNetworkAttachments(getParameters().getNetworkAttachments());
        nicNameNicIdCompleter.completeBonds(getParameters().getCreateOrUpdateBonds());
        nicNameNicIdCompleter.completeNetworkAttachments(getExistingAttachments());
        nicNameNicIdCompleter.completeLabels(getParameters().getLabels());

        ipConfigurationCompleter.fillInUnsetIpConfigs(getParameters().getNetworkAttachments());

        networkIdNetworkNameCompleter.completeNetworkAttachments(
                getParameters().getNetworkAttachments(),
                getNetworkBusinessEntityMap(),
                getStoragePoolId());

        networkIdNetworkNameCompleter.completeNetworkAttachments(
                getExistingAttachments(),
                getNetworkBusinessEntityMap(),
                getStoragePoolId());

        NicLabelsCompleter labelsCompleter = new NicLabelsCompleter(getParameters(),
                getExistingAttachments(),
                getClusterNetworks(),
                getExistingNicsBusinessEntityMap());
        labelsCompleter.completeNetworkAttachments();
    }

    private boolean validateEntitiesFromRequest(List<? extends BusinessEntity<?>> newOrUpdateBusinessEntities) {
        SeparateNewAndModifiedInstances instances = new SeparateNewAndModifiedInstances(newOrUpdateBusinessEntities);

        List<String> validationMessages = new ArrayList<>();
        validationMessages.addAll(callValidationOnAllItems(instances.getNewEntities(), CreateEntity.class, Default.class));
        validationMessages.addAll(callValidationOnAllItems(instances.getUpdatedEntities(), UpdateEntity.class, Default.class));

        return !getReturnValue().getValidationMessages().addAll(validationMessages);
    }

    private List<String> callValidationOnAllItems(List<BusinessEntity<?>> items, Class<?>... groups) {

        List<String> validationMessages = new ArrayList<>();
        for (BusinessEntity<?> businessEntity : items) {
            validationMessages.addAll(ValidationUtils.validateInputs(Arrays.asList(groups), businessEntity));
        }

        return validationMessages;
    }

    private ValidationResult validateWithHostSetupNetworksValidator(VDS host) {
        HostSetupNetworksValidator validator = new HostSetupNetworksValidator(host,
                getParameters(),
                getExistingNics(),
                getExistingAttachments(),
                selectNetworkAttachmentForValidatation(),
                getNetworkBusinessEntityMap(),
                networkClusterDao,
                networkDao,
                vdsDao,
                findActiveVmsUsingNetwork,
                hostSetupNetworksValidatorHelper,
                networkExclusivenessValidatorResolver,
                networkAttachmentIpConfigurationValidator,
                unmanagedNetworkValidator,
                backend);

        return validator.validate();
    }

    /**
     * if default route role was changed, the networkCluster has already been updated in the db by {@link ManageNetworkClustersCommand}
     * so in both flows the current default route role attachment can be found via the networkCluster
     * the {@link VdsNetworkInterface}s have not been updated yet, so previous default route role can be found from them
     *
     * if prev and curr are different - pass null to skip validation because default route role has been changed and need to allow setup networks to happen
     * if same - this is an update\addition of attachment and need to validate ipv6 gateway setting is legal
     *
     * @return network attachment to use in ipv6 gateway validation
     */
    private NetworkAttachment selectNetworkAttachmentForValidatation() {
        NetworkAttachment currDefaultRouteNetworkAttachment = findAttachmentByNetworkClusterId(
                findCurrentDefaultRouteNetworkForCluster());
        NetworkAttachment prevDefaultRouteNetworkAttachment = findNetworkAttachmentByNetworkName(
                findPreviousDefaultRouteNic(), getExistingAttachments());
        return areDifferentId(prevDefaultRouteNetworkAttachment, currDefaultRouteNetworkAttachment) ? null : prevDefaultRouteNetworkAttachment;
    }

    @Override
    protected void executeCommand() {
        if (noChangesDetected()) {
            log.info("No changes were detected in setup networks for host '{}' (ID: '{}')", getVdsName(), getVdsId());
            setSucceeded(true);
            return;
        }

        try (EngineLock monitoringLock = acquireMonitorLock("Host setup networks")) {
            int timeout = getSetupNetworksTimeout();
            FutureVDSCall<VDSReturnValue> setupNetworksTask = invokeSetupNetworksCommand(timeout);

            try {
                VDSReturnValue retVal = setupNetworksTask.get(timeout, TimeUnit.SECONDS);
                if (retVal != null) {
                    if (!retVal.getSucceeded() && retVal.getVdsError() == null && getParameters().rollbackOnFailure()) {
                        throw new EngineException(EngineError.SETUP_NETWORKS_ROLLBACK, retVal.getExceptionString());
                    }

                    VdsHandler.handleVdsResult(retVal);

                    if (retVal.getSucceeded()) {

                        VDSReturnValue returnValue =
                                runVdsCommand(VDSCommandType.GetCapabilities,
                                        new VdsIdAndVdsVDSCommandParametersBase(getVds()));
                        VDS updatedHost = (VDS) returnValue.getReturnValue();
                        persistNetworkChanges(updatedHost);
                    }

                    setSucceeded(true);
                }
            } catch (TimeoutException e) {
                log.debug("Host Setup networks command timed out for {} seconds", timeout);
            }
        }
    }

    private void removeUnchangedBonds(List<VdsNetworkInterface> existingNics) {
        Map<Guid, VdsNetworkInterface> nicsById = Entities.businessEntitiesById(existingNics);

        List<CreateOrUpdateBond> createOrUpdateBonds = getParameters().getCreateOrUpdateBonds();
        for (Iterator<CreateOrUpdateBond> iterator = createOrUpdateBonds.iterator(); iterator.hasNext();) {
            CreateOrUpdateBond bondFromRequest =  iterator.next();
            Guid idOfBondFromRequest = bondFromRequest.getId();

            boolean bondFromRequestIsNewBond = idOfBondFromRequest == null;
            if (!bondFromRequestIsNewBond) {
                if (bondFromRequest.equalToBond((Bond) nicsById.get(idOfBondFromRequest))) {
                    iterator.remove();
                }
            }
        }
    }

    /**
     * @param existingNetworkAttachment {@link NetworkAttachment} to compare.
     * @return true if passed in {@link NetworkAttachment networkAttachment} is deeply equal to {@code this}
     * {@link NetworkAttachment}
     */
    private boolean attachmentFromRequestIsEqualToAlreadyExistingOne(NetworkAttachment networkAttachmentFromRequest, NetworkAttachment existingNetworkAttachment) {
        return existingNetworkAttachment != null
                && Objects.equals(networkAttachmentFromRequest.getId(), existingNetworkAttachment.getId())
                && Objects.equals(networkAttachmentFromRequest.getNetworkId(), existingNetworkAttachment.getNetworkId())
                && Objects.equals(networkAttachmentFromRequest.getNetworkName(), existingNetworkAttachment.getNetworkName())
                && Objects.equals(networkAttachmentFromRequest.getNicId(), existingNetworkAttachment.getNicId())
                && Objects.equals(networkAttachmentFromRequest.getHostNetworkQos(), existingNetworkAttachment.getHostNetworkQos())
                && Objects.equals(networkAttachmentFromRequest.getNicName(), existingNetworkAttachment.getNicName())
                && Objects.equals(networkAttachmentFromRequest.getIpConfiguration(), existingNetworkAttachment.getIpConfiguration())
                && Objects.equals(networkAttachmentFromRequest.getProperties(), existingNetworkAttachment.getProperties())
                && Objects.equals(networkAttachmentFromRequest.getDnsResolverConfiguration(),
                    existingNetworkAttachment.getDnsResolverConfiguration());
    }

    private void removeUnchangedAttachments() {
        Map<Guid, NetworkAttachment> existingAttachmentsById = Entities.businessEntitiesById(getExistingAttachments());

        for (Iterator<NetworkAttachment> iterator = getParameters().getNetworkAttachments().iterator(); iterator.hasNext();) {
            NetworkAttachment attachmentFromRequest =  iterator.next();
            Guid idOfAttachmentFromRequest = attachmentFromRequest.getId();

            boolean attachmentFromRequestIsNewAttachment = idOfAttachmentFromRequest == null;
            // when flag 'isOverrideConfiguration' is set, NetworkAttachment from request cannot be ignored.
            boolean overridingConfiguration = attachmentFromRequest.isOverrideConfiguration();

            if (!attachmentFromRequestIsNewAttachment && !overridingConfiguration) {
                NetworkAttachment existingAttachment = existingAttachmentsById.get(idOfAttachmentFromRequest);
                if (attachmentFromRequestIsEqualToAlreadyExistingOne(attachmentFromRequest, existingAttachment)) {
                    iterator.remove();
                }
            }
        }
    }

    private void fillInUnsetBondingOptions() {
        for (CreateOrUpdateBond createOrUpdateBond : getParameters().getCreateOrUpdateBonds()) {
            if (StringUtils.isEmpty(createOrUpdateBond.getBondOptions())) {
                createOrUpdateBond.setBondOptions(DEFAULT_BOND_OPTIONS);
            }
        }
    }

    private ValidationResult checkForOutOfSyncNetworks() {
        for (NetworkAttachment networkAttachment : getParameters().getNetworkAttachments()) {
            boolean newNetworkAttachment = networkAttachment.getId() == null;
            if (newNetworkAttachment) {
                //attachment to be yet created cannot be out of sync.
                continue;
            }

            boolean doNotCheckForOutOfSync = networkAttachment.isOverrideConfiguration();
            if (doNotCheckForOutOfSync) {
                continue;
            }

            Map<Guid, NetworkAttachment> existingNetworkAttachmentMap =
                Entities.businessEntitiesById(getExistingAttachments());
            NetworkAttachment existingNetworkAttachment = existingNetworkAttachmentMap.get(networkAttachment.getId());

            VdsNetworkInterface nic =
                    NetworkUtils.hostInterfacesByNetworkName(getExistingNics())
                            .get(existingNetworkAttachment.getNetworkName());

            NetworkImplementationDetails networkImplementationDetails = nic.getNetworkImplementationDetails();
            boolean networkIsNotInSync = networkImplementationDetails != null && !networkImplementationDetails.isInSync();
            reportedConfigurationsFiller.fillReportedConfiguration(existingNetworkAttachment, getVdsId());

            if (networkIsNotInSync) {
                List<String> errors = new ArrayList<>();
                List<ReportedConfiguration> reportedConfigurations =
                        existingNetworkAttachment.getReportedConfigurations().getReportedConfigurationList();
                List<ReportedConfiguration> outOfSyncReportedConfigurations =
                        reportedConfigurations.stream().filter(config -> !config.isInSync()).collect(
                                Collectors.toList());

                Collections.sort(outOfSyncReportedConfigurations, Comparator.comparing(r -> r.getType().getName()));

                outOfSyncReportedConfigurations.stream().forEach(reportedConfiguration -> {
                    // The format of the message is for example - "${MTU} ${HOST_OT_OF_SYNC} - 5, ${DC_OUT_OF_SYNC} - 4"
                    errors.add(String.format("${%s} ${HOST_OUT_OF_SYNC} - %s, ${DC_OUT_OF_SYNC} - %s", reportedConfiguration.getType().getName().toUpperCase(), reportedConfiguration.getActualValue(), reportedConfiguration.getExpectedValue()));
                    addValidationMessage(reportedConfiguration.getType().getName().toUpperCase());
                });

                addValidationMessage(EngineMessage.HOST_OUT_OF_SYNC);
                addValidationMessage(EngineMessage.DC_OUT_OF_SYNC);
                addValidationMessageVariable("OUT_OF_SYNC_VALUES",
                        StringUtils.join(errors, "; "));
                return new ValidationResult(EngineMessage.NETWORK_NOT_IN_SYNC,
                        ReplacementUtils.createSetVariableString("NETWORK_NOT_IN_SYNC",
                                existingNetworkAttachment.getNetworkName()));
            }
        }

        return ValidationResult.VALID;
    }

    private FutureVDSCall<VDSReturnValue> invokeSetupNetworksCommand(int timeout) {
        final HostSetupNetworksVdsCommandParameters parameters = createSetupNetworksParameters(timeout);
        FutureVDSCall<VDSReturnValue> setupNetworksTask =
                vdsBroker.runFutureVdsCommand(FutureVDSCommandType.HostSetupNetworks, parameters);

        if (parameters.isRollbackOnFailure()) {
            HostPoller poller = new HostPoller(new TimeBoundPollVDSCommandParameters(getVdsId(),
                    CONFIRM_CONNECTIVITY));
            while (!setupNetworksTask.isDone()) {
                poller.poll();
            }
        }

        return setupNetworksTask;
    }

    private HostSetupNetworksVdsCommandParameters createSetupNetworksParameters(int timeout) {
        SwitchType clusterSwitchType = getCluster().getRequiredSwitchTypeForCluster();

        List<HostNetwork> networksToConfigure = calculateNetworksToConfigure();

        final HostSetupNetworksVdsCommandParameters hostCmdParams = new HostSetupNetworksVdsCommandParameters(
            getVds(),
            networksToConfigure,
            getAllNetworksToRemove(),
            getParameters().getCreateOrUpdateBonds(),
            getRemovedBondNames(),
            clusterSwitchType);
        hostCmdParams.setRollbackOnFailure(getParameters().rollbackOnFailure());
        hostCmdParams.setConnectivityTimeout(timeout);
        hostCmdParams.setCommitOnSuccess(getParameters().isCommitOnSuccess());
        hostCmdParams.setManagementNetworkChanged(isManagementNetworkChanged(networksToConfigure));
        return hostCmdParams;
    }

    private Set<String> getAllNetworksToRemove() {
        Set<String> result = new HashSet<>(getRemovedNetworks().size() + getRemovedUnmanagedNetworks().size());
        result.addAll(getRemovedNetworks());
        result.addAll(getRemovedUnmanagedNetworks());
        return result;
    }

    private Integer getSetupNetworksTimeout() {
        return getParameters().getConectivityTimeout() != null ? getParameters().getConectivityTimeout()
            : Config.<Integer> getValue(ConfigValues.NetworkConnectivityCheckTimeoutInSeconds);
    }

    private boolean noChangesDetected() {
        return getParameters().isEmptyRequest();
    }

    private List<VdsNetworkInterface> getRemovedBonds() {
        if (removedBonds == null) {
            Set<Guid> removedBondIds = getParameters().getRemovedBonds();
            removedBonds = Entities.filterEntitiesByRequiredIds(removedBondIds, getExistingNics());
        }

        return removedBonds;
    }

    private Set<String> getRemovedBondNames() {
        if (removedBondNames == null) {


            List<VdsNetworkInterface> existingVdsInterfaceToBeRemoved = getRemovedBonds();
            Set<String> removedBondNames = new HashSet<>(existingVdsInterfaceToBeRemoved.size());
            for (VdsNetworkInterface removedBondInterface : existingVdsInterfaceToBeRemoved) {
                removedBondNames.add(removedBondInterface.getName());
            }
            this.removedBondNames = removedBondNames;
        }

        return removedBondNames;
    }

    private List<VdsNetworkInterface> getExistingNics() {
        if (existingNics == null) {
            existingNics = interfaceDao.getAllInterfacesForVds(getVdsId());

            NetworkCommonUtils.fillBondSlaves(existingNics);

            for (VdsNetworkInterface iface : existingNics) {
                Network network = getNetworkBusinessEntityMap().get(iface.getNetworkName());
                NetworkImplementationDetails networkImplementationDetails =
                    networkImplementationDetailsUtils.calculateNetworkImplementationDetails(iface, network);
                iface.setNetworkImplementationDetails(networkImplementationDetails);
            }
        }

        return existingNics;
    }

    private List<NetworkAttachment> getExistingAttachments() {
        if (existingAttachments == null) {
            existingAttachments = networkAttachmentDao.getAllForHost(getVdsId());
        }

        return existingAttachments;
    }

    private Set<String> getRemovedNetworks() {
        if (removedNetworks == null) {
            List<NetworkAttachment> removedNetworkAttachments =
                    Entities.filterEntitiesByRequiredIds(getParameters().getRemovedNetworkAttachments(),
                            existingAttachments);
            removedNetworks = new HashSet<>(removedNetworkAttachments.size());

            Map<Guid, NetworkAttachment> networkIdToAttachment =
                    new MapNetworkAttachments(getParameters().getNetworkAttachments()).byNetworkId();

            for (NetworkAttachment removedAttachment : removedNetworkAttachments) {
                if (!networkIdToAttachment.containsKey(removedAttachment.getNetworkId())) {
                    removedNetworks.add(existingNetworkRelatedToAttachment(removedAttachment).getVdsmName());
                }
            }
        }

        return removedNetworks;
    }

    private Set<String> getRemovedUnmanagedNetworks() {
        if (removedUnmanagedNetworks == null) {
            this.removedUnmanagedNetworks = new HashSet<>(getParameters().getRemovedUnmanagedNetworks());
        }

        return removedUnmanagedNetworks;
    }

    private List<HostNetwork> calculateNetworksToConfigure() {
        List<HostNetwork> networksToConfigure = new ArrayList<>(getParameters().getNetworkAttachments().size());
        BusinessEntityMap<VdsNetworkInterface> nics = getExistingNicsBusinessEntityMap();

        List<NetworkAttachment> attachments = getAttachmentsWithMissingUpdatedDefaultRoute();
        removeIpv6GatewayFromPreviousDefaultRouteAttachment(attachments);
        for (NetworkAttachment attachment : attachments) {
            Network network = existingNetworkRelatedToAttachment(attachment);
            NetworkCluster networkCluster = network.getCluster();
            HostNetwork networkToConfigure = new HostNetwork(network, attachment);
            networkToConfigure.setBonding(isBonding(attachment, nics));

            boolean isDefaultRoute = defaultRouteSupported() && networkCluster.isDefaultRoute();
            if (isDefaultRoute) {
                DnsResolverConfiguration dnsResolverConfiguration =
                        getDnsConfigurationFromNetworkOrItsAttachment(attachment, network);

                if (dnsResolverConfiguration != null) {
                    networkToConfigure.setNameServers(dnsResolverConfiguration.getNameServers());
                }

            }

            // TODO: YZ - should default route be set separately for IPv4 and IPv6
            networkToConfigure.setDefaultRoute(isDefaultRoute);

            if (NetworkUtils.qosConfiguredOnInterface(attachment, network)) {
                networkToConfigure.setQosConfiguredOnInterface(true);

                HostNetworkQos hostNetworkQos = effectiveHostNetworkQos.getQos(attachment, network);
                networkToConfigure.setQos(hostNetworkQos);
            }
            auditIpConfiguration(attachment);
            networksToConfigure.add(networkToConfigure);
        }

        return networksToConfigure;
    }

    private List<NetworkAttachment> getAttachmentsWithMissingUpdatedDefaultRoute() {
        NetworkCluster currentDefaultRouteNetworkCluster = findCurrentDefaultRouteNetworkForCluster();
        NetworkAttachment currentDefaultRouteNetworkAttachment =
            findAttachmentByNetworkClusterId(currentDefaultRouteNetworkCluster);

        VdsNetworkInterface previousDefaultRouteNic = findPreviousDefaultRouteNic();
        NetworkAttachment previousDefaultRouteNetworkAttachment = findNetworkAttachmentByNetworkName(previousDefaultRouteNic, getExistingAttachments());

        if (sameNetworkAttachment(currentDefaultRouteNetworkAttachment, previousDefaultRouteNetworkAttachment)) {
            return getParameters().getNetworkAttachments();
        }

        List<NetworkAttachment> extendedAttachments = getParameters().getNetworkAttachments();
        NetworkAttachment potentialyMissingAttachment = getPotentialyMissingAttachment(
                currentDefaultRouteNetworkCluster == null ? null : currentDefaultRouteNetworkCluster.getNetworkId(),
                currentDefaultRouteNetworkAttachment,
                previousDefaultRouteNetworkAttachment);
        addAttachmentIfMissing(potentialyMissingAttachment, extendedAttachments);
        return extendedAttachments;
    }

    private boolean sameNetworkAttachment(NetworkAttachment first, NetworkAttachment second) {
        return first != null && second != null && first.getId().equals(second.getId());
    }

    private void removeIpv6GatewayFromPreviousDefaultRouteAttachment(List<NetworkAttachment> extendedAttachments) {
        NetworkAttachment previousDefaultRouteAttachment = findNetworkAttachmentByNetworkName(
                findPreviousDefaultRouteNic(), extendedAttachments
        );

        if (!sameNetworkAttachment(findAttachmentByNetworkClusterId(findCurrentDefaultRouteNetworkForCluster()),
                previousDefaultRouteAttachment)) {
            if (hasIpv6StaticBootProto(previousDefaultRouteAttachment)) {
                previousDefaultRouteAttachment.getIpConfiguration().getIpv6PrimaryAddress().setGateway(null);
                auditLog(auditEventRemoveIpv6Gateway(previousDefaultRouteAttachment),
                        AuditLogType.NETWORK_REMOVING_IPV6_GATEWAY_FROM_OLD_DEFAULT_ROUTE_ROLE_ATTACHMENT);
            }
        }
    }

    private NetworkAttachment findNetworkAttachmentByNetworkName(VdsNetworkInterface iface, List<NetworkAttachment> attachments) {
        return iface == null ? null : findNetworkAttachmentBy(networkNameAttachmentPredicate(iface.getNetworkName()), attachments);
    }

    private NetworkAttachment findAttachmentByNetworkClusterId(NetworkCluster networkCluster) {
        return networkCluster == null ? null : findNetworkAttachmentBy(
            networkIdAttachmentPredicate(networkCluster.getNetworkId()), getExistingAttachments());
    }

    private Predicate<NetworkAttachment> networkIdAttachmentPredicate(Guid networkId) {
        return attachment -> Objects.equals(networkId, attachment.getNetworkId());
    }

    private Predicate<NetworkAttachment> networkNameAttachmentPredicate(String networkName) {
        return attachment -> Objects.equals(networkName, attachment.getNetworkName());
    }

    private Predicate<NetworkAttachment> idAttachmentPredicate(Guid id) {
        return attachment -> Objects.equals(id, attachment.getId());
    }

    private NetworkCluster findCurrentDefaultRouteNetworkForCluster() {
        List<NetworkCluster> allForCluster = networkClusterDao.getAllForCluster(getClusterId());
        return allForCluster.stream()
                .filter(NetworkCluster::isDefaultRoute)
                .findFirst()
                .orElse(null);
    }

    private NetworkAttachment findNetworkAttachmentBy(Predicate<NetworkAttachment> predicate,
            List<NetworkAttachment> attachments) {
        return attachments.stream()
                .filter(attachment -> predicate.test(attachment))
                .findFirst()
                .orElse(null);
    }

    private VdsNetworkInterface findPreviousDefaultRouteNic() {
        List<VdsNetworkInterface> existingNics = getExistingNics();
        return existingNics.stream()
                .filter(isDefaultRouteOnInterfacePredicate())
                .findFirst()
                .orElse(null);
    }

    private NetworkAttachment getPotentialyMissingAttachment(Guid currentDefaultRouteNetworkId,
            NetworkAttachment currentDefaultRouteNetworkAttachment,
            NetworkAttachment previousDefaultRouteNetworkAttachment) {
        if (isPreviousDefaultRouteInParameters(previousDefaultRouteNetworkAttachment)) {
            return currentDefaultRouteNetworkAttachment;
        }
        if (isCurrentDefaultRouteInParameters(currentDefaultRouteNetworkId)) {
            return previousDefaultRouteNetworkAttachment;
        }
        return null;
    }

    private void addAttachmentIfMissing(NetworkAttachment attachmentToAdd,
            List<NetworkAttachment> extendedAttachments) {
        if (attachmentToAdd == null) {
            return;
        }

        if (findNetworkAttachmentBy(idAttachmentPredicate(attachmentToAdd.getId()),
                getParameters().getNetworkAttachments()) == null
                && !getParameters().getRemovedNetworkAttachments().contains(attachmentToAdd.getId())) {
            extendedAttachments.add(attachmentToAdd);
        }
    }

    private boolean isCurrentDefaultRouteInParameters(Guid currentDefaultRouteNetworkId) {
        return currentDefaultRouteNetworkId != null
                && findNetworkAttachmentBy(networkIdAttachmentPredicate(currentDefaultRouteNetworkId),
                        getParameters().getNetworkAttachments()) != null;
    }

    private boolean isPreviousDefaultRouteInParameters(NetworkAttachment previousDefaultRouteNetworkAttachment) {
        return previousDefaultRouteNetworkAttachment != null
                && findNetworkAttachmentBy(idAttachmentPredicate(previousDefaultRouteNetworkAttachment.getId()),
                        getParameters().getNetworkAttachments()) != null
                && !getParameters().getRemovedNetworkAttachments()
                        .contains(previousDefaultRouteNetworkAttachment.getId());
    }

    private DnsResolverConfiguration getDnsConfigurationFromNetworkOrItsAttachment(NetworkAttachment attachment,
            Network network) {
        DnsResolverConfiguration networkAttachmentDnsResolverConfiguration = attachment.getDnsResolverConfiguration();
        if (networkAttachmentDnsResolverConfiguration != null) {
            return networkAttachmentDnsResolverConfiguration;
        }

        DnsResolverConfiguration networkDnsResolverConfiguration = network.getDnsResolverConfiguration();
        if (networkDnsResolverConfiguration != null) {
            return networkDnsResolverConfiguration;
        }
        return null;
    }

    private BusinessEntityMap<VdsNetworkInterface> getExistingNicsBusinessEntityMap() {
        if (existingNicsBusinessEntityMap == null) {
            existingNicsBusinessEntityMap = new BusinessEntityMap<>(getExistingNics());
        }

        return existingNicsBusinessEntityMap;
    }

    private boolean defaultRouteSupported() {
        Set<Version> supportedClusterVersionsSet = getVds().getSupportedClusterVersionsSet();
        if (supportedClusterVersionsSet == null || supportedClusterVersionsSet.isEmpty()) {
            log.warn("Host '{}' ('{}') doesn't contain Supported Cluster Versions, "
                    + "therefore 'defaultRoute' will not be sent via the SetupNetworks",
                getVdsName(),
                getVdsId());
            return false;
        }

        return true;
    }

    private boolean isBonding(NetworkAttachment attachment, BusinessEntityMap<VdsNetworkInterface> nics) {
        for (CreateOrUpdateBond bond : getParameters().getCreateOrUpdateBonds()) {
            if (bond.getName() != null && bond.getName().equals(attachment.getNicName())) {
                return true;
            }
        }

        VdsNetworkInterface attachedNic = nics.get(attachment.getNicId(), attachment.getNicName());
        Validate.notNull(attachedNic, "NicId/NicName must refer to a resolvable interface");
        return Boolean.TRUE.equals(attachedNic.getBonded());
    }

    private Map<String, UserOverriddenNicValues> applyUserConfiguredNics() {
        List<VdsNetworkInterface> nicsToConfigure = getExistingInterfacesAndNewlyCreatedBonds();

        updateLabelsOnNicsToConfigure(nicsToConfigure);

        return nicsToConfigure.stream()
                .collect(Collectors.toMap(VdsNetworkInterface::getName,
                        nic -> new UserOverriddenNicValues(nic.getLabels())));
    }

    private void updateLabelsOnNicsToConfigure(List<VdsNetworkInterface> nicsToConfigure) {
        Map<String, VdsNetworkInterface> nicsToConfigureByName = Entities.entitiesByName(nicsToConfigure);

        clearLabelsFromRemovedBonds(nicsToConfigureByName);

        updateAddedModifiedLabelsOnNics(nicsToConfigureByName);

        updateRemovedLabelOnNics(nicsToConfigureByName);
    }

    private void updateRemovedLabelOnNics(Map<String, VdsNetworkInterface> nicsToConfigureByName) {
        Map<String, VdsNetworkInterface> labelToNic = getLabelToNic(nicsToConfigureByName.values());
        for (String removedLabel : getParameters().getRemovedLabels()) {
            VdsNetworkInterface nicWithLabel = labelToNic.get(removedLabel);

            if (nicWithLabel != null) {
                nicWithLabel.getLabels().remove(removedLabel);
            }
        }
    }

    private void updateAddedModifiedLabelsOnNics(Map<String, VdsNetworkInterface> nicsToConfigureByName) {
        Map<String, VdsNetworkInterface> labelToExistingNic = getLabelToNic(nicsToConfigureByName.values());
        for (NicLabel nicLabel : getParameters().getLabels()) {
            VdsNetworkInterface currentLabelNic = labelToExistingNic.get(nicLabel.getLabel());
            VdsNetworkInterface newLabelNic = nicsToConfigureByName.get(nicLabel.getNicName());

            moveLabel(nicLabel.getLabel(), currentLabelNic, newLabelNic);
        }
    }

    private void moveLabel(String label,
            VdsNetworkInterface currentLabelNic,
            VdsNetworkInterface newLabelNic) {
        if (currentLabelNic != null && currentLabelNic.getName().equals(newLabelNic.getName())) {
            return;
        }

        // Add label to new nic
        Set<String> labelsOnNic = newLabelNic.getLabels();

        if (labelsOnNic == null) {
            labelsOnNic = new HashSet<>();
            newLabelNic.setLabels(labelsOnNic);
        }

        labelsOnNic.add(label);

        // Remove labels from current nic
        if (currentLabelNic != null) {
            currentLabelNic.getLabels().remove(label);
        }
    }

    private void clearLabelsFromRemovedBonds(Map<String, VdsNetworkInterface> nicsToConfigureByName) {
        for (VdsNetworkInterface existingBondToRemove : getRemovedBonds()) {
            nicsToConfigureByName.get(existingBondToRemove.getName()).setLabels(null);
        }
    }

    private List<VdsNetworkInterface> getExistingInterfacesAndNewlyCreatedBonds() {
        List<VdsNetworkInterface> nicsToConfigure = new ArrayList<>();

        nicsToConfigure.addAll(interfaceDao.getAllInterfacesForVds(getVdsId()));

        for (CreateOrUpdateBond createOrUpdateBond : getParameters().getCreateOrUpdateBonds()) {
            if (createOrUpdateBond.getId() == null) {
                Bond newBond = new Bond(createOrUpdateBond.getName());
                nicsToConfigure.add(newBond);
            }
        }

        return nicsToConfigure;
    }

    private Map<String, VdsNetworkInterface> getLabelToNic(Collection<VdsNetworkInterface> nics) {
        Map<String, VdsNetworkInterface> labelToNic = new HashMap<>();
        for (VdsNetworkInterface nic : nics) {
            if (NetworkUtils.isLabeled(nic)) {
                for (String label : nic.getLabels()) {
                    labelToNic.put(label, nic);
                }
            }
        }

        return labelToNic;
    }

    private List<Network> getModifiedNetworks() {
        if (modifiedNetworks == null) {
            List<NetworkAttachment> networkAttachments = getParameters().getNetworkAttachments();
            modifiedNetworks = new ArrayList<>(networkAttachments.size());

            for (NetworkAttachment attachment : networkAttachments) {
                modifiedNetworks.add(existingNetworkRelatedToAttachment(attachment));
            }
        }

        return modifiedNetworks;
    }

    private Network existingNetworkRelatedToAttachment(NetworkAttachment attachment) {
        return getNetworkBusinessEntityMap().get(attachment.getNetworkId());
    }

    private void persistNetworkChanges(final VDS updatedHost) {
        TransactionSupport.executeInNewTransaction(() -> {
            UserConfiguredNetworkData userConfiguredNetworkData =
                    new UserConfiguredNetworkData(getAttachmentsWithMissingUpdatedDefaultRoute(),
                            getParameters().getRemovedNetworkAttachments(),
                            applyUserConfiguredNics());

            // save the new network topology to DB
            hostNetworkTopologyPersister.persistAndEnforceNetworkCompliance(updatedHost,
                false,
                userConfiguredNetworkData);

            vdsDynamicDao.updateNetConfigDirty(updatedHost.getId(), updatedHost.getNetConfigDirty());

            // Update cluster networks (i.e. check if need to activate each new network)
            networkClusterHelper.setStatus(getClusterId(), getModifiedNetworks());

            return null;
        });
    }

    private BusinessEntityMap<Network> getNetworkBusinessEntityMap() {
        if (networkBusinessEntityMap == null) {
            networkBusinessEntityMap = new BusinessEntityMap<>(getClusterNetworks());
        }

        return networkBusinessEntityMap;
    }

    private List<Network> getClusterNetworks() {
        if (clusterNetworks == null) {
            clusterNetworks = networkDao.getAllForCluster(getClusterId());
        }
        return clusterNetworks;
    }

    private boolean isManagementNetworkChanged(List<HostNetwork> networksToConfigure){
        Network managementNetwork = managementNetworkUtil.getManagementNetwork(getClusterNetworks(), getClusterId());

        String managementNetworkName = managementNetwork.getName();
        for (HostNetwork network : networksToConfigure) {
            if (managementNetworkName.equals(network.getNetworkName())){
                return true;
            }
        }

        for (CreateOrUpdateBond createOrUpdateBond : getParameters().getCreateOrUpdateBonds()) {
            // We are only interested in existing bonds, whose bonding options/slave have changed, so it
            // enough to check existing bonds. New bonds which have the management network
            // are covered by network attachments
            VdsNetworkInterface bondNic = getExistingNicsBusinessEntityMap().get(createOrUpdateBond.getId());
            if (bondNic != null && managementNetworkName.equals(bondNic.getNetworkName())) {
                return true;
            }
        }
        return false;
    }

    private AuditLogable auditEventRemoveIpv6Gateway(NetworkAttachment attachment) {
        AuditLogable event = new AuditLogableImpl();
        event.setClusterId(getCluster().getId());
        event.setClusterName(getCluster().getName());
        event.addCustomValue("NetworkName", attachment.getNetworkName());
        return event;
    }

    private void auditIpConfiguration(NetworkAttachment attachment) {
        List<IPv4Address> iPv4Addresses = attachment.getIpConfiguration().getIPv4Addresses();
        iPv4Addresses.stream()
                .filter(a -> a.getBootProtocol() == Ipv4BootProtocol.STATIC_IP)
                .forEach(a -> {
                    if (!CidrValidator.getInstance().isIpv4InSubnet(a.getAddress(), a.getNetmask(), a.getGateway())) {
                        auditLog(auditEventIpv4GatewayOutOfRange(attachment, a), NETWORK_IPV4_GATEWAY_OUT_OF_RANGE);
                    }
                });
    }

    private AuditLogable auditEventIpv4GatewayOutOfRange(NetworkAttachment attachment, IPv4Address address) {
        AuditLogable event = new AuditLogableImpl();
        event.setVdsId(getVdsId());
        event.setClusterName(getCluster().getName());
        event.setVdsName(getVdsName());
        event.addCustomValue("Interface", attachment.getNicName());
        event.addCustomValue("Gateway", address.getGateway());
        return event;

    }
}

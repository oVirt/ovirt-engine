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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.ovirt.engine.core.bll.LockMessagesMatchUtil;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.VdsCommand;
import org.ovirt.engine.core.bll.VdsHandler;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.network.cluster.ManagementNetworkUtil;
import org.ovirt.engine.core.bll.network.cluster.NetworkClusterHelper;
import org.ovirt.engine.core.bll.validator.network.NetworkExclusivenessValidatorResolver;
import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.action.HostSetupNetworksParameters;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.LockProperties.Scope;
import org.ovirt.engine.core.common.businessentities.BusinessEntityMap;
import org.ovirt.engine.core.common.businessentities.Entities;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.network.Bond;
import org.ovirt.engine.core.common.businessentities.network.HostNetworkQos;
import org.ovirt.engine.core.common.businessentities.network.IpConfiguration;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkAttachment;
import org.ovirt.engine.core.common.businessentities.network.NetworkBootProtocol;
import org.ovirt.engine.core.common.businessentities.network.NicLabel;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface.NetworkImplementationDetails;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.interfaces.FutureVDSCall;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.utils.MapNetworkAttachments;
import org.ovirt.engine.core.common.utils.NetworkCommonUtils;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.FutureVDSCommandType;
import org.ovirt.engine.core.common.vdscommands.HostNetwork;
import org.ovirt.engine.core.common.vdscommands.HostSetupNetworksVdsCommandParameters;
import org.ovirt.engine.core.common.vdscommands.UserConfiguredNetworkData;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.VdsIdAndVdsVDSCommandParametersBase;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.dao.network.InterfaceDao;
import org.ovirt.engine.core.dao.network.NetworkAttachmentDao;
import org.ovirt.engine.core.dao.network.NetworkClusterDao;
import org.ovirt.engine.core.dao.network.NetworkDao;
import org.ovirt.engine.core.utils.NetworkUtils;
import org.ovirt.engine.core.utils.ReplacementUtils;
import org.ovirt.engine.core.utils.lock.EngineLock;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;
import org.ovirt.engine.core.vdsbroker.EffectiveHostNetworkQos;
import org.ovirt.engine.core.vdsbroker.NetworkImplementationDetailsUtils;
import org.ovirt.engine.core.vdsbroker.vdsbroker.HostNetworkTopologyPersister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@NonTransactiveCommandAttribute
public class HostSetupNetworksCommand<T extends HostSetupNetworksParameters> extends VdsCommand<T> {


    private static final Logger log = LoggerFactory.getLogger(HostSetupNetworksCommand.class);
    private static final String DEFAULT_BOND_OPTIONS = "mode=4 miimon=100";
    private BusinessEntityMap<Network> networkBusinessEntityMap;

    private Set<String> removedNetworks;
    private Set<String> removedBondNames;
    private List<VdsNetworkInterface> removedBonds;
    private Set<String> removedUnmanagedNetworks;
    private List<VdsNetworkInterface> existingNics;
    private List<NetworkAttachment> existingAttachments;
    private List<HostNetwork> networksToConfigure;
    private BusinessEntityMap<VdsNetworkInterface> existingNicsBusinessEntityMap;
    private List<Network> clusterNetworks;

    @Inject
    private HostNetworkTopologyPersister hostNetworkTopologyPersister;

    @Inject
    private ManagementNetworkUtil managementNetworkUtil;

    private List<Network> modifiedNetworks;

    @Inject
    private NetworkClusterDao networkClusterDao;

    @Inject
    private NetworkDao networkDao;

    @Inject
    private NetworkAttachmentDao networkAttachmentDao;

    @Inject
    private VdsDao vdsDao;

    @Inject
    private VmDao vmDao;

    @Inject
    private InterfaceDao interfaceDao;

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

    public HostSetupNetworksCommand(T parameters) {
        this(parameters, null);
    }

    public HostSetupNetworksCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
        setVdsId(parameters.getVdsId());
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(EngineMessage.VAR__ACTION__SETUP);
        addCanDoActionMessage(EngineMessage.VAR__TYPE__NETWORKS);
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(Scope.Execution);
    }

    @Override
    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        return Collections.singletonMap(getParameters().getVdsId().toString(),
                LockMessagesMatchUtil.makeLockingPair(LockingGroup.HOST_NETWORK,
                        EngineMessage.ACTION_TYPE_FAILED_SETUP_NETWORKS_IN_PROGRESS));
    }

    @Override
    protected boolean canDoAction() {
        VDS host = getVds();

        fillInUnsetBondingOptions();

        final ValidationResult hostValidatorResult = new HostValidator(host, isInternalExecution()).validate();
        if (!hostValidatorResult.isValid()) {
            return validate(hostValidatorResult);
        }

        completeMissingDataInParameters();

        IdQueryParameters idParameters = new IdQueryParameters(getVdsId());
        VdcQueryReturnValue existingBondsResponse = runInternalQuery(VdcQueryType.GetHostBondsByHostId, idParameters);
        if (!existingBondsResponse.getSucceeded()) {
            return false;
        }
        List<VdsNetworkInterface> existingBonds = existingBondsResponse.getReturnValue();

        removeUnchangedAttachments(networkAttachmentDao.getAllForHost(getVdsId()));
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
        nicNameNicIdCompleter.completeBonds(getParameters().getBonds());
        nicNameNicIdCompleter.completeNetworkAttachments(getExistingAttachments());
        nicNameNicIdCompleter.completeLabels(getParameters().getLabels());

        networkIdNetworkNameCompleter.completeNetworkAttachments(
                getParameters().getNetworkAttachments(),
                getNetworkBusinessEntityMap());

        networkIdNetworkNameCompleter.completeNetworkAttachments(
                getExistingAttachments(),
                getNetworkBusinessEntityMap());

        NicLabelsCompleter labelsCompleter = new NicLabelsCompleter(getParameters(),
                getExistingAttachments(),
                getClusterNetworks(),
                getExistingNicsBusinessEntityMap());
        labelsCompleter.completeNetworkAttachments();
    }

    private ValidationResult validateWithHostSetupNetworksValidator(VDS host) {
        HostSetupNetworksValidator validator = new HostSetupNetworksValidator(host,
                getParameters(),
                getExistingNics(),
                getExistingAttachments(),
                getNetworkBusinessEntityMap(),
                managementNetworkUtil,
                networkClusterDao,
                networkDao,
                vdsDao,
                hostSetupNetworksValidatorHelper,
                vmDao,
                networkExclusivenessValidatorResolver);

        return validator.validate();
    }

    @Override
    protected void executeCommand() {
        if (noChangesDetected()) {
            log.info("No changes were detected in setup networks for host '{}' (ID: '{}')", getVdsName(), getVdsId());
            setSucceeded(true);
            return;
        }

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
                    try (EngineLock monitoringLock = acquireMonitorLock()) {
                        VDSReturnValue returnValue =
                            runVdsCommand(VDSCommandType.GetCapabilities,
                                new VdsIdAndVdsVDSCommandParametersBase(getVds()));
                        VDS updatedHost = (VDS) returnValue.getReturnValue();
                        persistNetworkChanges(updatedHost);
                    }
                    logMonitorLockReleased("Host setup networks");

                    setSucceeded(true);
                }
            }
        } catch (TimeoutException e) {
            log.debug("Host Setup networks command timed out for {} seconds", timeout);
        }
    }

    private void removeUnchangedBonds(List<VdsNetworkInterface> existingNics) {
        Map<Guid, VdsNetworkInterface> nicsById = Entities.businessEntitiesById(existingNics);

        for (Iterator<Bond> iterator = getParameters().getBonds().iterator(); iterator.hasNext();) {
            Bond bondFromRequest =  iterator.next();
            Guid idOfBondFromRequest = bondFromRequest.getId();

            boolean bondFromRequestIsNewBond = idOfBondFromRequest == null;
            if (!bondFromRequestIsNewBond) {
                if (bondFromRequestIsEqualToAlreadyExistingNic(bondFromRequest, nicsById.get(idOfBondFromRequest))) {
                    iterator.remove();
                }
            }
        }
    }

    // TODO MM: The bonds in the parameters shouldn't contain the whole VdsNetworkInterface. 0nly the id, name, bondOptions and slaves.
    private boolean bondFromRequestIsEqualToAlreadyExistingNic(Bond bondFromRequest, VdsNetworkInterface existingNic) {
        return existingNic != null
                && existingNic instanceof Bond
                && Objects.equals(bondFromRequest.getId(), existingNic.getId())
                && Objects.equals(bondFromRequest.getName(), existingNic.getName())
                && Objects.equals(bondFromRequest.getBondOptions(), existingNic.getBondOptions())
                && bondsHasSameSlaves(bondFromRequest, (Bond) existingNic);

    }

    /**
     * @param existingNetworkAttachment {@link NetworkAttachment} to compare.
     * @return true if passed in {@link NetworkAttachment networkAttachment} is deeply equal to {@code this}
     * {@link NetworkAttachment}
     */
    public boolean attachmentFromRequestIsEqualToAlreadyExistingOne(NetworkAttachment networkAttachmentFromRequest, NetworkAttachment existingNetworkAttachment) {
        return existingNetworkAttachment != null
                && Objects.equals(networkAttachmentFromRequest.getId(), existingNetworkAttachment.getId())
                && Objects.equals(networkAttachmentFromRequest.getNetworkId(), existingNetworkAttachment.getNetworkId())
                && Objects.equals(networkAttachmentFromRequest.getNetworkName(), existingNetworkAttachment.getNetworkName())
                && Objects.equals(networkAttachmentFromRequest.getNicId(), existingNetworkAttachment.getNicId())
                && Objects.equals(networkAttachmentFromRequest.getHostNetworkQos(), existingNetworkAttachment.getHostNetworkQos())
                && Objects.equals(networkAttachmentFromRequest.getNicName(), existingNetworkAttachment.getNicName())
                && Objects.equals(networkAttachmentFromRequest.getIpConfiguration(), existingNetworkAttachment.getIpConfiguration())
                && Objects.equals(networkAttachmentFromRequest.getProperties(), existingNetworkAttachment.getProperties());
    }

    private boolean bondsHasSameSlaves(Bond bondFromRequest, Bond existingNic) {
        List<String> slavesOfBondFromRequest = replaceNullWithEmptyList(bondFromRequest.getSlaves());
        List<String> slavesOfExistingBond = replaceNullWithEmptyList(existingNic.getSlaves());

        //bonds can be in any order, and I don't want to change this order during this check.
        return slavesOfBondFromRequest.size() == slavesOfExistingBond.size()
                && slavesOfBondFromRequest.containsAll(slavesOfExistingBond);

    }

    private List<String> replaceNullWithEmptyList(List<String> list) {
        return list == null ? Collections.emptyList() : list;
    }

    private void removeUnchangedAttachments(List<NetworkAttachment> existingAttachments) {
        Map<Guid, NetworkAttachment> existingAttachmentsById = Entities.businessEntitiesById(existingAttachments);

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
        for (Bond bond : getParameters().getBonds()) {
            if (StringUtils.isEmpty(bond.getBondOptions())) {
                bond.setBondOptions(DEFAULT_BOND_OPTIONS);
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

            if (networkIsNotInSync) {
                return new ValidationResult(EngineMessage.NETWORKS_NOT_IN_SYNC,
                    ReplacementUtils.createSetVariableString("NETWORK_NOT_IN_SYNC",
                        existingNetworkAttachment.getNetworkName()));
            }
        }

        return ValidationResult.VALID;
    }

    private FutureVDSCall<VDSReturnValue> invokeSetupNetworksCommand(int timeout) {
        final HostSetupNetworksVdsCommandParameters parameters = createSetupNetworksParameters(timeout);
        FutureVDSCall<VDSReturnValue> setupNetworksTask =
            getVdsBroker().runFutureVdsCommand(FutureVDSCommandType.HostSetupNetworks, parameters);

        if (parameters.isRollbackOnFailure()) {
            HostSetupNetworkPoller poller = new HostSetupNetworkPoller();
            while (!setupNetworksTask.isDone()) {
                poller.poll(getVdsId());
            }
        }

        return setupNetworksTask;
    }

    private HostSetupNetworksVdsCommandParameters createSetupNetworksParameters(int timeout) {
        final HostSetupNetworksVdsCommandParameters hostCmdParams = new HostSetupNetworksVdsCommandParameters(
            getVds(),
            getNetworksToConfigure(),
            getAllNetworksToRemove(),
            getParameters().getBonds(),
            getRemovedBondNames());
        hostCmdParams.setRollbackOnFailure(getParameters().rollbackOnFailure());
        hostCmdParams.setConectivityTimeout(timeout);
        boolean hostNetworkQosSupported = FeatureSupported.hostNetworkQos(getVds().getVdsGroupCompatibilityVersion());
        hostCmdParams.setHostNetworkQosSupported(hostNetworkQosSupported);
        hostCmdParams.setManagementNetworkChanged(isManagementNetworkChanged());
        return hostCmdParams;
    }

    private Set<String> getAllNetworksToRemove() {
        Set<String> result = new HashSet<>(getRemovedNetworks().size() + getRemovedUnmanagedNetworks().size());
        result.addAll(getRemovedNetworks());
        result.addAll(getRemovedUnmanagedNetworks());
        return result;
    }

    protected Integer getSetupNetworksTimeout() {
        return getParameters().getConectivityTimeout() != null ? getParameters().getConectivityTimeout()
            : Config.<Integer> getValue(ConfigValues.NetworkConnectivityCheckTimeoutInSeconds);
    }

    private boolean defaultRouteRequired(Network network, IpConfiguration ipConfiguration) {
        return managementNetworkUtil.isManagementNetwork(network.getId(), getVds().getVdsGroupId())
                && ipConfiguration != null
                && ipConfiguration.hasPrimaryAddressSet()
                && (ipConfiguration.getPrimaryAddress().getBootProtocol() == NetworkBootProtocol.DHCP
                      || ipConfiguration.getPrimaryAddress().getBootProtocol() == NetworkBootProtocol.STATIC_IP
                            && StringUtils.isNotEmpty(ipConfiguration.getPrimaryAddress().getGateway()));
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
            existingNics = getDbFacade().getInterfaceDao().getAllInterfacesForVds(getVdsId());

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
            existingAttachments = getDbFacade().getNetworkAttachmentDao().getAllForHost(getVdsId());
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
                    removedNetworks.add(existingNetworkRelatedToAttachment(removedAttachment).getName());
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

    private List<HostNetwork> getNetworksToConfigure() {
        if (networksToConfigure == null) {
            networksToConfigure = new ArrayList<>(getParameters().getNetworkAttachments().size());
            BusinessEntityMap<VdsNetworkInterface> nics = getExistingNicsBusinessEntityMap();

            for (NetworkAttachment attachment : getParameters().getNetworkAttachments()) {
                Network network = existingNetworkRelatedToAttachment(attachment);
                HostNetwork networkToConfigure = new HostNetwork(network, attachment);
                networkToConfigure.setBonding(isBonding(attachment, nics));

                if (defaultRouteSupported() && defaultRouteRequired(network, attachment.getIpConfiguration())) {
                    networkToConfigure.setDefaultRoute(true);
                }

                if (NetworkUtils.qosConfiguredOnInterface(attachment, network)) {
                    networkToConfigure.setQosConfiguredOnInterface(true);

                    HostNetworkQos hostNetworkQos = effectiveHostNetworkQos.getQos(attachment, network);
                    networkToConfigure.setQos(hostNetworkQos);
                }

                networksToConfigure.add(networkToConfigure);
            }
        }

        return networksToConfigure;
    }

    private BusinessEntityMap<VdsNetworkInterface> getExistingNicsBusinessEntityMap() {
        if (existingNicsBusinessEntityMap == null) {
            existingNicsBusinessEntityMap = new BusinessEntityMap<>(getExistingNics());
        }

        return existingNicsBusinessEntityMap;
    }

    private boolean defaultRouteSupported() {
        boolean defaultRouteSupported = false;
        Set<Version> supportedClusterVersionsSet = getVds().getSupportedClusterVersionsSet();
        if (supportedClusterVersionsSet == null || supportedClusterVersionsSet.isEmpty()) {
            log.warn("Host '{}' ('{}') doesn't contain Supported Cluster Versions, "
                    + "therefore 'defaultRoute' will not be sent via the SetupNetworks",
                getVdsName(),
                getVdsId());
        } else if (FeatureSupported.defaultRoute(Collections.max(supportedClusterVersionsSet))) {
            defaultRouteSupported = true;
        }

        return defaultRouteSupported;
    }

    private boolean isBonding(NetworkAttachment attachment, BusinessEntityMap<VdsNetworkInterface> nics) {
        for (Bond bond : getParameters().getBonds()) {
            if (bond.getName() != null && bond.getName().equals(attachment.getNicName())) {
                return true;
            }
        }

        VdsNetworkInterface attachedNic = nics.get(attachment.getNicId(), attachment.getNicName());
        Validate.notNull(attachedNic, "NicId/NicName must refer to a resolvable interface");
        return Boolean.TRUE.equals(attachedNic.getBonded());
    }

    private List<VdsNetworkInterface> applyUserConfiguredNics() {
        List<VdsNetworkInterface> nicsToConfigure = getNicsToConfigureWithoutLabelsUpdates();

        updateLabelsOnNicsToConfigure(nicsToConfigure);

        return nicsToConfigure;
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
        Map<String, VdsNetworkInterface> labelToExistingNic = getLabelToNic(nicsToConfigureByName);
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

    private Map<String, VdsNetworkInterface> getLabelToNic(Map<String, VdsNetworkInterface> nicsToConfigureByName) {
        Map<String, VdsNetworkInterface> labelToExistingNic = new HashMap<>();
        for (VdsNetworkInterface nic : nicsToConfigureByName.values()) {
            if (NetworkUtils.isLabeled(nic)) {
                for (String label : nic.getLabels()) {
                    labelToExistingNic.put(label, nic);
                }
            }
        }

        return labelToExistingNic;
    }

    private List<VdsNetworkInterface> getNicsToConfigureWithoutLabelsUpdates() {
        List<VdsNetworkInterface> nicsToConfigure = new ArrayList<>();

        nicsToConfigure.addAll(interfaceDao.getAllInterfacesForVds(getVdsId()));

        // TODO MM: The bonds in the parameters shouldn't contain the whole VdsNetworkInterface. 0nly the id, name, bondOptions and slaves.
        for (Bond bond : getParameters().getBonds()) {
            if (bond.getId() == null) {
                Bond newBond = new Bond(bond.getName());
                nicsToConfigure.add(newBond);
            }
        }

        return nicsToConfigure;
    }

    private Map<String, VdsNetworkInterface> getLabelToNic(Collection<VdsNetworkInterface> nics) {
        Map<String, VdsNetworkInterface> labelToNic = new HashMap<>();
        for (VdsNetworkInterface nic : nics) {
            if (nic.getLabels() != null) {
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
        TransactionSupport.executeInNewTransaction(new TransactionMethod<Void>() {
            @Override
            public Void runInTransaction() {
                UserConfiguredNetworkData userConfiguredNetworkData =
                    new UserConfiguredNetworkData(getParameters().getNetworkAttachments(), applyUserConfiguredNics());

                // save the new network topology to DB
                hostNetworkTopologyPersister.persistAndEnforceNetworkCompliance(updatedHost,
                    false,
                    userConfiguredNetworkData);

                getVdsDynamicDao().updateNetConfigDirty(updatedHost.getId(), updatedHost.getNetConfigDirty());

                // Update cluster networks (i.e. check if need to activate each new network)
                for (Network net : getModifiedNetworks()) {
                    NetworkClusterHelper.setStatus(getVdsGroupId(), net);
                }

                return null;
            }


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
            clusterNetworks = getNetworkDao().getAllForCluster(getVdsGroupId());
        }
        return clusterNetworks;
    }

    private boolean isManagementNetworkChanged(){
        String mgmtNetworkName = managementNetworkUtil.getManagementNetwork(getVds().getVdsGroupId()).getName();
        for (HostNetwork network : getNetworksToConfigure()) {
            if (mgmtNetworkName.equals(network.getNetworkName())){
                return true;
            }
        }
        for (Bond bond : getParameters().getBonds()) {
            // We are only interested in existing bonds, whose bonding options/slave have changed, so it
            // enough to check existing bonds. New bonds which have the management network
            // are covered by network attachments
            VdsNetworkInterface bondNic = getExistingNicsBusinessEntityMap().get(bond.getId());
            if (bondNic != null && mgmtNetworkName.equals(bondNic.getNetworkName())) {
                return true;
            }
        }
        return false;
    }

}

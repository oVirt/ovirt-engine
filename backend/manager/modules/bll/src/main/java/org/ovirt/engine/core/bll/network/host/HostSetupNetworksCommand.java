package org.ovirt.engine.core.bll.network.host;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.VdsCommand;
import org.ovirt.engine.core.bll.VdsHandler;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.network.cluster.ManagementNetworkUtil;
import org.ovirt.engine.core.bll.network.cluster.NetworkClusterHelper;
import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.action.HostSetupNetworksParameters;
import org.ovirt.engine.core.common.businessentities.BusinessEntityMap;
import org.ovirt.engine.core.common.businessentities.Entities;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.network.Bond;
import org.ovirt.engine.core.common.businessentities.network.IpConfiguration;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkAttachment;
import org.ovirt.engine.core.common.businessentities.network.NetworkBootProtocol;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.VdcBLLException;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.interfaces.FutureVDSCall;
import org.ovirt.engine.core.common.vdscommands.FutureVDSCommandType;
import org.ovirt.engine.core.common.vdscommands.HostNetwork;
import org.ovirt.engine.core.common.vdscommands.HostSetupNetworksVdsCommandParameters;
import org.ovirt.engine.core.common.vdscommands.UserConfiguredNetworkData;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.VdsIdAndVdsVDSCommandParametersBase;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dao.network.HostNetworkQosDao;
import org.ovirt.engine.core.utils.NetworkUtils;
import org.ovirt.engine.core.utils.lock.EngineLock;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;
import org.ovirt.engine.core.vdsbroker.vdsbroker.HostNetworkTopologyPersister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@NonTransactiveCommandAttribute
public class HostSetupNetworksCommand<T extends HostSetupNetworksParameters> extends VdsCommand<T> {


    private static final Logger log = LoggerFactory.getLogger(HostSetupNetworksCommand.class);
    private Map<Guid, Network> clusterNetworks;
    private Set<String> removedNetworks;
    private Set<String> removedBondNames;
    private List<VdsNetworkInterface> removedBonds;
    private List<VdsNetworkInterface> existingNics;
    private List<NetworkAttachment> existingAttachments;
    private List<HostNetwork> networksToConfigure;

    @Inject
    private HostNetworkTopologyPersister hostNetworkTopologyPersister;

    @Inject
    private ManagementNetworkUtil managementNetworkUtil;

    @Inject
    private HostNetworkQosDao qosDao;
    private List<Network> modifiedNetworks;

    public HostSetupNetworksCommand(T parameters) {
        this(parameters, null);
    }

    public HostSetupNetworksCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
        setVdsId(parameters.getVdsId());
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__SETUP);
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__NETWORKS);
    }

    @Override
    protected boolean canDoAction() {
        VDS host = getVds();

        final ValidationResult hostValidatorResult = new HostValidator(host, isInternalExecution()).validate();
        if (!hostValidatorResult.isValid()) {
            return validate(hostValidatorResult);
        }

        NicNameNicIdCompleter completer = new NicNameNicIdCompleter(getExistingNics());
        completer.completeNetworkAttachments(getParameters().getNetworkAttachments());
        completer.completeBonds(getParameters().getBonds());
        completer.completeNetworkAttachments(getExistingAttachments());

        return validate(validateWithHostSetupNetworksValidator(host));
    }

    private ValidationResult validateWithHostSetupNetworksValidator(VDS host) {
        HostSetupNetworksValidator validator = new HostSetupNetworksValidator(host,
                getParameters(),
                getExistingNics(),
                getExistingAttachments(),
                getClusterNetworks(),
            managementNetworkUtil
        );

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
                    throw new VdcBLLException(VdcBllErrors.SETUP_NETWORKS_ROLLBACK, retVal.getExceptionString());
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

    private FutureVDSCall<VDSReturnValue> invokeSetupNetworksCommand(int timeout) {
        final HostSetupNetworksVdsCommandParameters parameters = createSetupNetworksParameters(timeout);
        FutureVDSCall<VDSReturnValue> setupNetworksTask =
                getBackend().getResourceManager().runFutureVdsCommand(FutureVDSCommandType.HostSetupNetworks,
                    parameters);

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
                getRemovedNetworks(),
                getParameters().getBonds(),
                getRemovedBondNames());
        hostCmdParams.setRollbackOnFailure(getParameters().rollbackOnFailure());
        hostCmdParams.setConectivityTimeout(timeout);
        boolean hostNetworkQosSupported = FeatureSupported.hostNetworkQos(getVds().getVdsGroupCompatibilityVersion());
        hostCmdParams.setHostNetworkQosSupported(hostNetworkQosSupported);
        return hostCmdParams;
    }

    protected Integer getSetupNetworksTimeout() {
        return getParameters().getConectivityTimeout() != null ? getParameters().getConectivityTimeout()
                : Config.<Integer> getValue(ConfigValues.NetworkConnectivityCheckTimeoutInSeconds);
    }

    private boolean defaultRouteRequired(Network network, IpConfiguration ipConfiguration) {
        return managementNetworkUtil.isManagementNetwork(network.getId(), getVds().getVdsGroupId())
                && ipConfiguration != null
                && (ipConfiguration.getBootProtocol() == NetworkBootProtocol.DHCP
                || ipConfiguration.getBootProtocol() == NetworkBootProtocol.STATIC_IP
                && ipConfiguration.hasPrimaryAddressSet() && StringUtils.isNotEmpty(ipConfiguration.getPrimaryAddress().getGateway()));
    }

    private boolean noChangesDetected() {
        return getNetworksToConfigure().isEmpty()
                && getRemovedNetworks().isEmpty()
                && getParameters().getBonds().isEmpty()
                && getRemovedBondNames().isEmpty();
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
            Map<String, Network> networksByName = Entities.entitiesByName(getClusterNetworks().values());

            for (VdsNetworkInterface iface : existingNics) {
                Network network = networksByName.get(iface.getNetworkName());
                iface.setNetworkImplementationDetails(NetworkUtils.calculateNetworkImplementationDetails(network,
                        network == null ? null : qosDao.get(network.getQosId()),
                        iface));
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

            for (NetworkAttachment attachment : removedNetworkAttachments) {
                removedNetworks.add(existingNetworkRelatedToAttachment(attachment).getName());
            }
        }

        return removedNetworks;
    }

    private Map<Guid, Network> getClusterNetworks() {
        if (clusterNetworks == null) {
            clusterNetworks = Entities.businessEntitiesById(getNetworkDAO().getAllForCluster(getVdsGroupId()));
        }

        return clusterNetworks;
    }

    private List<HostNetwork> getNetworksToConfigure() {
        if (networksToConfigure == null) {
            networksToConfigure = new ArrayList<>(getParameters().getNetworkAttachments().size());
            BusinessEntityMap<VdsNetworkInterface> nics = new BusinessEntityMap<>(getExistingNics());

            for (NetworkAttachment attachment : getParameters().getNetworkAttachments()) {
                Network network = existingNetworkRelatedToAttachment(attachment);
                HostNetwork networkToConfigure = new HostNetwork(network, attachment);
                networkToConfigure.setBonding(isBonding(attachment, nics));

                if (defaultRouteSupported() && defaultRouteRequired(network, attachment.getIpConfiguration())) {
                    networkToConfigure.setDefaultRoute(true);
                }

                //TODO MM:  if it's newly created interface, it won't be discovered and Qos cannot be evaluated.
                VdsNetworkInterface iface = nics.get(attachment.getNicId(), attachment.getNicName());
                boolean qosConfiguredOnInterface =
                    iface == null ? false : NetworkUtils.qosConfiguredOnInterface(iface, network);
                networkToConfigure.setQosConfiguredOnInterface(qosConfiguredOnInterface);
                if (qosConfiguredOnInterface) {
                    networkToConfigure.setQos(
                        iface.isQosOverridden() ? iface.getQos() : qosDao.get(network.getQosId()));
                }

                networksToConfigure.add(networkToConfigure);
            }
        }

        return networksToConfigure;
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
        Validate.notNull(attachedNic, "Bond must refer to a resolvable interface");
        return Boolean.TRUE.equals(attachedNic.getBonded());
    }

    private List<VdsNetworkInterface> applyUserConfiguredNics() {
        List<VdsNetworkInterface> userConfiguredNics = new ArrayList<>();
        userConfiguredNics.addAll(getParameters().getBonds());
        for (VdsNetworkInterface existingBondToRemove : getRemovedBonds()) {
            existingBondToRemove.setLabels(null);
            userConfiguredNics.add(existingBondToRemove);
        }

        return userConfiguredNics;
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
        return getClusterNetworks().get(attachment.getNetworkId());
    }

    private void persistNetworkChanges(final VDS updatedHost) {
        TransactionSupport.executeInNewTransaction(new TransactionMethod<Void>() {
            @Override
            public Void runInTransaction() {
                UserConfiguredNetworkData userConfiguredNetworkData =
                        new UserConfiguredNetworkData(getParameters().getNetworkAttachments(),
                                applyUserConfiguredNics());

                // save the new network topology to DB
                hostNetworkTopologyPersister.persistAndEnforceNetworkCompliance(updatedHost,
                        false,
                        userConfiguredNetworkData);

                getVdsDynamicDao().updateNetConfigDirty(getVds().getId(), getVds().getNetConfigDirty());

                // Update cluster networks (i.e. check if need to activate each new network)
                for (Network net : getModifiedNetworks()) {
                    NetworkClusterHelper.setStatus(getVdsGroupId(), net);
                }

                return null;
            }
        });
    }
}

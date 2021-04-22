package org.ovirt.engine.core.bll.network.dc;

import static org.ovirt.engine.core.common.AuditLogType.NETWORK_UPDATE_NETWORK;
import static org.ovirt.engine.core.common.AuditLogType.NETWORK_UPDATE_NETWORK_FAILED;
import static org.ovirt.engine.core.common.AuditLogType.NETWORK_UPDATE_NETWORK_STARTED;
import static org.ovirt.engine.core.common.AuditLogType.NETWORK_UPDATE_NETWORK_START_ERROR;
import static org.ovirt.engine.core.common.AuditLogType.NETWORK_UPDATE_NOTHING_TO_DO;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Typed;
import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.ConcurrentChildCommandsExecutionCallback;
import org.ovirt.engine.core.bll.NetworkLocking;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.RenamedEntityInfoProvider;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.network.AddNetworkParametersBuilder;
import org.ovirt.engine.core.bll.network.HostSetupNetworksParametersBuilder;
import org.ovirt.engine.core.bll.network.RemoveNetworkParametersBuilder;
import org.ovirt.engine.core.bll.network.cluster.NetworkClusterHelper;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.bll.validator.HasStoragePoolValidator;
import org.ovirt.engine.core.bll.validator.NetworkValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.AddNetworkStoragePoolParameters;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.PersistentHostSetupNetworksParameters;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.network.DnsResolverConfiguration;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkAttachment;
import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;
import org.ovirt.engine.core.common.businessentities.network.ProviderNetwork;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface.NetworkImplementationDetails;
import org.ovirt.engine.core.common.businessentities.network.VmNic;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.utils.NetworkCommonUtils;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.validation.group.UpdateEntity;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogable;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableImpl;
import org.ovirt.engine.core.dao.StoragePoolDao;
import org.ovirt.engine.core.dao.VdsStaticDao;
import org.ovirt.engine.core.dao.network.InterfaceDao;
import org.ovirt.engine.core.dao.network.NetworkAttachmentDao;
import org.ovirt.engine.core.dao.network.NetworkClusterDao;
import org.ovirt.engine.core.dao.network.NetworkDao;
import org.ovirt.engine.core.dao.network.VmNicDao;
import org.ovirt.engine.core.di.Injector;
import org.ovirt.engine.core.utils.NetworkUtils;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;
import org.ovirt.engine.core.vdsbroker.NetworkImplementationDetailsUtils;

@NonTransactiveCommandAttribute
public class UpdateNetworkCommand<T extends AddNetworkStoragePoolParameters> extends NetworkModification<T> implements RenamedEntityInfoProvider {

    @Inject
    private SyncNetworkParametersBuilder syncNetworkParametersBuilder;
    @Inject
    private NetworkClusterHelper networkClusterHelper;
    @Inject
    private NetworkDao networkDao;
    @Inject
    private NetworkClusterDao networkClusterDao;
    @Inject
    private NetworkLocking networkLocking;
    @Inject
    private StoragePoolDao storagePoolDao;
    @Inject
    private VmNicDao vmNicDao;
    @Inject
    @Typed(ConcurrentChildCommandsExecutionCallback.class)
    private Instance<ConcurrentChildCommandsExecutionCallback> callbackProvider;
    @Inject
    private AuditLogDirector auditLogDirector;

    private Network oldNetwork;

    public UpdateNetworkCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }

    @Override
    protected void executeCommand() {
        NetworkUtils.setNetworkVdsmName(getNetwork());
        TransactionSupport.executeInNewTransaction(() -> {
            networkDao.update(getNetwork());

            for (NetworkCluster clusterAttachment : networkClusterDao.getAllForNetwork(getNetwork().getId())) {
                networkClusterHelper.setStatus(clusterAttachment.getClusterId(), getNetwork());
            }

            if (networkChangedToNonVmNetwork()) {
                removeVnicProfiles();
            } else if (isMtuUpdated() || isVlanUpdated()) {
                var vnics = vmNicDao.getActiveForNetwork(getNetwork().getId());
                vnics.forEach(vnic -> vnic.setSynced(false));
                vmNicDao.updateAllInBatch(vnics);
                logOutOfSync(vnics);
            }

            if (networkNameChanged()) {
                updateDefaultVnicProfileName(getOldNetwork().getName());
            }

            return null;
        });

        if (!getNetwork().isExternal()) {
            applyNetworkChangesToHosts();
        }

        setSucceeded(true);
    }

    private boolean networkNameChanged() {
        return !getOldNetwork().getName().equals(getNetworkName());
    }

    private boolean isMtuUpdated() {
        return NetworkUtils.getVmMtuActualValue(getOldNetwork()) != NetworkUtils.getVmMtuActualValue(getNetwork());
    }

    private boolean isVlanUpdated() {
        return !Objects.equals(getOldNetwork().getVlanId(), getNetwork().getVlanId());
    }

    private void applyNetworkChangesToHosts() {
        List<ActionParametersBase> parameters = syncNetworkParametersBuilder.buildParameters(getNetwork(), getOldNetwork());

        if (!parameters.isEmpty()) {
            HostSetupNetworksParametersBuilder.updateParametersSequencing(parameters);
            parameters.forEach(this::withRootCommandInfo);
            runInternalMultipleActions(ActionType.PersistentHostSetupNetworks, parameters);
        }
    }

    private boolean networkChangedToNonVmNetwork() {
        return getOldNetwork().isVmNetwork() && !getNetwork().isVmNetwork();
    }

    @Override
    protected void setActionMessageParameters() {
        super.setActionMessageParameters();
        addValidationMessage(EngineMessage.VAR__ACTION__UPDATE);
    }

    @Override
    protected boolean validate() {
        if (onlyPermittedFieldsChanged() && allowedNetworkLabelManipulation()) {
            return true;
        }

        final HasStoragePoolValidator hasStoragePoolValidator = new HasStoragePoolValidator(getNetwork());
        final NetworkValidator validatorNew = new NetworkValidator(getNetwork());
        final UpdateNetworkValidator validatorOld = new UpdateNetworkValidator(getOldNetwork());
        return validate(hasStoragePoolValidator.storagePoolExists())
                && validate(validatorNew.stpForVmNetworkOnly())
                && validate(validatorNew.networkPrefixValid())
                && validate(validatorNew.qosExistsInDc())
                && validate(validatorOld.networkIsSet(getNetwork().getId()))
                && validate(validatorOld.notChangingDataCenterId(getNetwork().getDataCenterId()))
                && validate(validatorNew.networkNameNotUsed())
                && validate(validatorNew.networkNameNotUsedAsVdsmName())
                && validate(validatorOld.nonVmNetworkNotUsedByVms(getNetwork()))
                && validate(validatorOld.nonVmNetworkNotUsedByTemplates(getNetwork()))
                && validate(validatorNew.portIsolationForVmNetworkOnly())
                && validate(validatorOld.portIsolationUnchanged(getNetwork()))
                && validate(validatorOld.notRenamingUsedNetwork(getNetworkName()))
                && validate(validatorOld.notRenamingLabel(getNetwork().getLabel()))
                && (oldAndNewNetworkIsNotExternal()
                || validate(validatorOld.externalNetworkDetailsUnchanged(getNetwork())));
    }

    private boolean allowedNetworkLabelManipulation() {
        boolean labelNotChanged = !labelChanged(getNetwork(), getOldNetwork());

        return !getNetwork().isExternal() && (labelNotChanged || labelAdded(getNetwork(), getOldNetwork()));
    }

    /**
     * @return <code>true</code> iff only the description or comment field were changed, otherwise <code>false</code>.
     */
    private boolean onlyPermittedFieldsChanged() {
        Network oldNetwork = getOldNetwork();
        Network newNetwork = getNetwork();

        if (oldNetwork == null || newNetwork == null) {
            return false;
        }

        return Objects.equals(oldNetwork.getName(), newNetwork.getName()) &&
                Objects.equals(oldNetwork.getDataCenterId(), newNetwork.getDataCenterId()) &&
                Objects.equals(oldNetwork.getId(), newNetwork.getId()) &&
                Objects.equals(oldNetwork.getMtu(), newNetwork.getMtu()) &&
                Objects.equals(oldNetwork.getName(), newNetwork.getName()) &&
                Objects.equals(oldNetwork.getProvidedBy(), newNetwork.getProvidedBy()) &&
                Objects.equals(oldNetwork.getStp(), newNetwork.getStp()) &&
                Objects.equals(oldNetwork.getVlanId(), newNetwork.getVlanId()) &&
                Objects.equals(oldNetwork.isVmNetwork(), newNetwork.isVmNetwork()) &&
                Objects.equals(oldNetwork.isPortIsolation(), newNetwork.isPortIsolation());
    }

    private boolean oldAndNewNetworkIsNotExternal() {
        return !getOldNetwork().isExternal() && !getNetwork().isExternal();
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        switch (getActionState()) {
        case EXECUTE:
            if (!getSucceeded()) {
                return NETWORK_UPDATE_NETWORK_START_ERROR;
            } else if (skipHostSetupNetworks()) {
                return NETWORK_UPDATE_NOTHING_TO_DO;
            } else {
                return NETWORK_UPDATE_NETWORK_STARTED;
            }
        case END_SUCCESS:
            return NETWORK_UPDATE_NETWORK;
        }
        return NETWORK_UPDATE_NETWORK_FAILED;
    }

    private void logOutOfSync(List<VmNic> vnics) {
        var logable = new AuditLogableImpl();
        logable.addCustomValue("NetworkName", getNetworkName());
        logable.addCustomValue("VnicNames", vnics.stream().map(VmNic::getName).collect(Collectors.joining(",")));
        auditLogDirector.log(logable, AuditLogType.VNICS_OUT_OF_SYNC_ON_NETWORK_UPDATE);
    }

    private boolean skipHostSetupNetworks() {
        return getNetwork().isExternal() || syncNetworkParametersBuilder.buildParameters(getNetwork(), getOldNetwork()).isEmpty();
    }

    @Override
    protected List<Class<?>> getValidationGroups() {
        addValidationGroup(UpdateEntity.class);
        return super.getValidationGroups();
    }

    private Network getOldNetwork() {
        if (oldNetwork == null) {
            oldNetwork = networkDao.get(getNetwork().getId());
        }
        return oldNetwork;
    }

    protected static class UpdateNetworkValidator extends NetworkValidator {

        public UpdateNetworkValidator(Network network) {
            super(network);
        }

        public ValidationResult notRenamingLabel(String newLabel) {
            String oldLabel = network.getLabel();
            if (oldLabel == null || newLabel == null || oldLabel.equals(newLabel)) {
                return ValidationResult.VALID;
            }

            List<VdsNetworkInterface> nics = Injector.get(InterfaceDao.class).getVdsInterfacesByNetworkId(network.getId());
            for (VdsNetworkInterface nic : nics) {
                VdsNetworkInterface labeledNic = nic;
                if (NetworkCommonUtils.isVlan(nic)) {
                    labeledNic = getBaseInterface(nic);
                }

                if (NetworkUtils.isLabeled(labeledNic) && labeledNic.getLabels().contains(oldLabel)) {
                    return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_NETWORK_LABEL_RENAMING_NOT_SUPPORTED);
                }
            }

            return ValidationResult.VALID;
        }

        private VdsNetworkInterface getBaseInterface(VdsNetworkInterface vlan) {
            List<VdsNetworkInterface> hostNics = Injector.get(InterfaceDao.class).getAllInterfacesForVds(vlan.getVdsId());

            for (VdsNetworkInterface hostNic : hostNics) {
                if (NetworkUtils.interfaceBasedOn(vlan, hostNic.getName())) {
                    return hostNic;
                }
            }

            throw new EngineException(EngineError.LABELED_NETWORK_INTERFACE_NOT_FOUND);
        }

        public ValidationResult notRenamingUsedNetwork(String networkName) {
            if (StringUtils.equals(network.getName(), networkName)) {
                return ValidationResult.VALID;
            }

            ValidationResult result = networkNotUsedByHosts();
            if (!result.isValid()) {
                return result;
            }

            return networkNotUsedByTemplates();
        }

        public ValidationResult nonVmNetworkNotUsedByVms(Network updatedNetwork) {
            if (networkChangedToNonVmNetwork(updatedNetwork)) {
                return networkNotUsedByVms();
            }

            return ValidationResult.VALID;
        }

        private boolean networkChangedToNonVmNetwork(Network updatedNetwork) {
            return network.isVmNetwork() && !updatedNetwork.isVmNetwork();
        }

        public ValidationResult nonVmNetworkNotUsedByTemplates(Network updatedNetwork) {
            if (networkChangedToNonVmNetwork(updatedNetwork)) {
                return networkNotUsedByTemplates();
            }

            return ValidationResult.VALID;
        }

        /**
         * Check that the external network details that can't be updated were not updated.<br>
         * The check is undefined if both the validator's network and the new network are internal networks.
         *
         * @param newNetwork
         *            The new network definition to check.
         * @return A valid result iff the details that shouldn't be changed remained unchanged, An error otherwise.
         */
        public ValidationResult externalNetworkDetailsUnchanged(Network newNetwork) {
            ProviderNetwork providerNetwork = network.getProvidedBy();
            ProviderNetwork newProviderNetwork = newNetwork.getProvidedBy();
            return Objects.equals(network.getVlanId(), newNetwork.getVlanId())
                    && network.getStp() == newNetwork.getStp()
                    && network.isVmNetwork() == newNetwork.isVmNetwork()
                    && Objects.equals(providerNetwork.getProviderId(), newProviderNetwork.getProviderId())
                    && Objects.equals(providerNetwork.getExternalId(), newProviderNetwork.getExternalId())
                    ? ValidationResult.VALID
                    : new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_EXTERNAL_NETWORK_DETAILS_CANNOT_BE_EDITED);
        }

        public ValidationResult notChangingDataCenterId(Guid dataCenterId) {
            final Guid oldDataCenterId = network.getDataCenterId();
            return ValidationResult.failWith(EngineMessage.ACTION_TYPE_FAILED_DATA_CENTER_ID_CANNOT_BE_CHANGED)
                    .when(!oldDataCenterId.equals(dataCenterId));
        }

        public ValidationResult portIsolationUnchanged(Network newNetwork) {
            return ValidationResult.failWith(EngineMessage.ACTION_TYPE_FAILED_NETWORK_PORT_ISOLATION_CANNOT_BE_CHANGED)
                    .when(network.isPortIsolation() != newNetwork.isPortIsolation());
        }
    }

    @Override
    public String getEntityType() {
        return VdcObjectType.Network.getVdcObjectTranslation();
    }

    @Override
    public String getEntityOldName() {
        return getOldNetwork().getName();
    }

    @Override
    public String getEntityNewName() {
        return getParameters().getNetwork().getName();
    }

    @Override
    public void setEntityId(AuditLogable logable) {

    }

    private static class SyncNetworkParametersBuilder extends HostSetupNetworksParametersBuilder {

        @Inject
        private NetworkImplementationDetailsUtils networkImplementationDetailsUtils;

        @Inject
        private AddNetworkParametersBuilder addNetworkParametersBuilder;

        @Inject
        private RemoveNetworkParametersBuilder removeNetworkParametersBuilder;

        @Inject
        public SyncNetworkParametersBuilder(InterfaceDao interfaceDao,
                VdsStaticDao vdsStaticDao,
                NetworkClusterDao networkClusterDao,
                NetworkAttachmentDao networkAttachmentDao) {
            super(interfaceDao, vdsStaticDao, networkClusterDao, networkAttachmentDao);
        }

        private ArrayList<ActionParametersBase> buildParameters(Network network, Network oldNetwork) {
            ArrayList<ActionParametersBase> parameters = new ArrayList<>();
            List<VdsNetworkInterface> nics =
                    interfaceDao.getVdsInterfacesByNetworkId(network.getId());

            // sync network on nics if the label wasn't changed
            if (!labelChanged(network, oldNetwork)) {
                createSyncNetworkParameters(network, oldNetwork, parameters, nics);
                return parameters;
            }

            // add network to labeled interfaces and sync network on the rest
            if (labelAdded(network, oldNetwork) || labelRenamed(network, oldNetwork)) {
                List<VdsNetworkInterface> labeledNics = getLabeledNics(network);
                Map<Guid, VdsNetworkInterface> hostToNic = mapHostToNic(nics);
                List<VdsNetworkInterface> nicsForAdd = new ArrayList<>();
                Set<VdsNetworkInterface> nicsForSync = new HashSet<>();

                // nics to add network
                for (VdsNetworkInterface labeledNic : labeledNics) {
                    VdsNetworkInterface nic = hostToNic.get(labeledNic.getVdsId());

                    // add network to labeled nic if network not configured on host
                    if (nic == null) {
                        nicsForAdd.add(labeledNic);
                    } else {
                        // sync the network
                        nicsForSync.add(nic);
                    }
                }

                // add the unlabeled nics to be synced
                for (VdsNetworkInterface nic : nics) {
                    if (!nicsForSync.contains(nic)) {
                        nicsForSync.add(nic);
                    }
                }

                parameters.addAll(createAddNetworkParameters(network, nicsForAdd));
                createSyncNetworkParameters(network, oldNetwork, parameters, nicsForSync);
                return parameters;
            }

            // remove network from labeled interfaces
            if (labelRemoved(network, oldNetwork)) {
                List<VdsNetworkInterface> labeledNics = getLabeledNics(oldNetwork);
                Map<Guid, VdsNetworkInterface> hostToNic = mapHostToNic(nics);
                List<VdsNetworkInterface> nicsForRemove = new ArrayList<>();
                Set<VdsNetworkInterface> nicsForSync = new HashSet<>();

                // nics to remove the network from
                for (VdsNetworkInterface labeledNic : labeledNics) {
                    VdsNetworkInterface nic = hostToNic.get(labeledNic.getVdsId());

                    // remove the network from labeled nic
                    if (nic != null) {
                        nicsForRemove.add(labeledNic);
                    }
                }

                // add the unlabeled nics to be synced
                for (VdsNetworkInterface nic : nics) {
                    if (!nicsForSync.contains(nic)) {
                        nicsForSync.add(nic);
                    }
                }

                parameters.addAll(createRemoveNetworkParameters(network, nicsForRemove));
                createSyncNetworkParameters(network, oldNetwork, parameters, nicsForSync);
                return parameters;
            }

            return parameters;
        }

        private ArrayList<ActionParametersBase> createAddNetworkParameters(Network network, List<VdsNetworkInterface> nicsForAdd) {
            return addNetworkParametersBuilder.buildParameters(network, nicsForAdd);
        }

        private ArrayList<ActionParametersBase> createRemoveNetworkParameters(Network oldNetwork, List<VdsNetworkInterface> nicsForRemove) {
            return removeNetworkParametersBuilder.buildParameters(oldNetwork, nicsForRemove);
        }

        private Map<Guid, VdsNetworkInterface> mapHostToNic(List<VdsNetworkInterface> nics) {
            Map<Guid, VdsNetworkInterface> hostToNic = new HashMap<>(nics.size());
            for (VdsNetworkInterface nic : nics) {
                hostToNic.put(nic.getVdsId(), nic);
            }
            return hostToNic;
        }

        private List<VdsNetworkInterface> getLabeledNics(Network network) {
            List<NetworkCluster> clusters = networkClusterDao.getAllForNetwork(network.getId());
            List<VdsNetworkInterface> labeledNics = new ArrayList<>();
            for (NetworkCluster networkCluster : clusters) {
                labeledNics.addAll(interfaceDao.getAllInterfacesByLabelForCluster(networkCluster.getClusterId(),
                        network.getLabel()));
            }
            return labeledNics;
        }

        private void createSyncNetworkParameters(Network network, Network oldNetwork, ArrayList<ActionParametersBase> parameters,
                Collection<VdsNetworkInterface> nics) {

            Set<Guid> hostIdsToSync = new HashSet<>();
            for (VdsNetworkInterface nic : nics) {
                NetworkImplementationDetails networkImplementationDetails =
                        networkImplementationDetailsUtils.calculateNetworkImplementationDetails(nic, network);
                boolean networkShouldBeSynced =
                        networkImplementationDetails != null && !networkImplementationDetails.isInSync()
                                || shouldDnsNameServerBeUpdatedOnHost(network, oldNetwork, nic);

                if (networkShouldBeSynced) {
                    hostIdsToSync.add(nic.getVdsId());
                }
            }

            for (Guid hostId : hostIdsToSync) {
                PersistentHostSetupNetworksParameters setupNetworkParams = createHostSetupNetworksParameters(hostId);
                setupNetworkParams.setNetworkNames(network.getName());

                NetworkAttachment attachment = getNetworkIdToAttachmentMap(hostId).get(network.getId());
                attachment.setOverrideConfiguration(true);
                setupNetworkParams.getNetworkAttachments().add(attachment);
                parameters.add(setupNetworkParams);
            }
        }

        private boolean shouldDnsNameServerBeUpdatedOnHost(Network network,
                Network oldNetwork,
                VdsNetworkInterface nic) {
            return nic.isIpv4DefaultRoute() && wasDnsNameServerRemoved(network, oldNetwork);
        }

        private boolean wasDnsNameServerRemoved(Network network, Network oldNetwork) {
            DnsResolverConfiguration dnsResolverConfiguration = network.getDnsResolverConfiguration();
            DnsResolverConfiguration oldDnsResolverConfiguration = oldNetwork.getDnsResolverConfiguration();
            boolean dnsEmpty = isDnsEmpty(dnsResolverConfiguration);
            boolean oldDnsEmpty = isDnsEmpty(oldDnsResolverConfiguration);
            return (dnsEmpty && !oldDnsEmpty) || (!dnsEmpty && !oldDnsEmpty
                    && !dnsResolverConfiguration.getNameServers()
                            .containsAll(oldDnsResolverConfiguration.getNameServers()));
        }

        private boolean isDnsEmpty(DnsResolverConfiguration dnsResolverConfiguration) {
            return dnsResolverConfiguration == null || dnsResolverConfiguration.getNameServers() == null || dnsResolverConfiguration.getNameServers().isEmpty();
        }
    }

    private static boolean labelChanged(Network network, Network oldNetwork) {
        return !Objects.equals(network.getLabel(), oldNetwork.getLabel());
    }

    private static boolean labelAdded(Network network, Network oldNetwork) {
        return !NetworkUtils.isLabeled(oldNetwork) && NetworkUtils.isLabeled(network);
    }

    private static boolean labelRemoved(Network network, Network oldNetwork) {
        return NetworkUtils.isLabeled(oldNetwork) && !NetworkUtils.isLabeled(network);
    }

    private static boolean labelRenamed(Network network, Network oldNetwork) {
        return NetworkUtils.isLabeled(oldNetwork) && NetworkUtils.isLabeled(network) && labelChanged(network, oldNetwork);
    }

    private Guid getProviderId() {
        return (getNetwork()==null || !getNetwork().isExternal()) ?
                null : getNetwork().getProvidedBy().getProviderId();
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(LockProperties.Scope.Execution).withWaitForever();
    }

    @Override
    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        if (getNetwork().isExternal() && !isInternalExecution()) {
            return networkLocking.getNetworkProviderLock(getProviderId());
        } else {
            return null;
        }
    }

    @Override
    public Map<String, String> getJobMessageProperties() {
        if (jobProperties == null) {
            jobProperties = super.getJobMessageProperties();
        }
        StoragePool pool = storagePoolDao.get(getParameters().getNetwork().getDataCenterId());
        jobProperties.put(VdcObjectType.StoragePool.name().toLowerCase(), pool.getName());
        return jobProperties;
    }

    @Override
    public CommandCallback getCallback() {
        return callbackProvider.get();
    }
}

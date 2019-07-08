package org.ovirt.engine.core.bll.network.dc;

import static org.ovirt.engine.core.common.AuditLogType.NETWORK_REMOVE_NETWORK;
import static org.ovirt.engine.core.common.AuditLogType.NETWORK_REMOVE_NETWORK_FAILED;
import static org.ovirt.engine.core.common.AuditLogType.NETWORK_REMOVE_NETWORK_STARTED;
import static org.ovirt.engine.core.common.AuditLogType.NETWORK_REMOVE_NETWORK_START_ERROR;
import static org.ovirt.engine.core.common.AuditLogType.NETWORK_REMOVE_NOTHING_TO_DO;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Typed;
import javax.inject.Inject;

import org.ovirt.engine.core.bll.ConcurrentChildCommandsExecutionCallback;
import org.ovirt.engine.core.bll.NetworkLocking;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.network.HostSetupNetworksParametersBuilder;
import org.ovirt.engine.core.bll.network.RemoveNetworkParametersBuilder;
import org.ovirt.engine.core.bll.network.cluster.NetworkClusterHelper;
import org.ovirt.engine.core.bll.network.cluster.NetworkHelper;
import org.ovirt.engine.core.bll.provider.ProviderProxyFactory;
import org.ovirt.engine.core.bll.provider.ProviderValidator;
import org.ovirt.engine.core.bll.provider.network.NetworkProviderProxy;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.bll.validator.NetworkValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.RemoveNetworkParameters;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;
import org.ovirt.engine.core.common.businessentities.network.ProviderNetwork;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.StoragePoolDao;
import org.ovirt.engine.core.dao.network.InterfaceDao;
import org.ovirt.engine.core.dao.network.NetworkClusterDao;
import org.ovirt.engine.core.dao.network.NetworkDao;
import org.ovirt.engine.core.dao.provider.ProviderDao;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@NonTransactiveCommandAttribute(forceCompensation = true)
public class RemoveNetworkCommand<T extends RemoveNetworkParameters> extends NetworkCommon<T> {

    @Inject
    private InterfaceDao interfaceDao;
    @Inject
    private NetworkClusterHelper networkClusterHelper;
    @Inject
    private NetworkDao networkDao;
    @Inject
    private ProviderDao providerDao;
    @Inject
    private NetworkClusterDao networkClusterDao;
    @Inject
    private ProviderProxyFactory providerProxyFactory;
    @Inject
    private NetworkLocking networkLocking;
    @Inject
    private RemoveNetworkParametersBuilder removeNetworkParametersBuilder;
    @Inject
    private StoragePoolDao storagePoolDao;
    @Inject
    @Typed(ConcurrentChildCommandsExecutionCallback.class)
    private Instance<ConcurrentChildCommandsExecutionCallback> callbackProvider;

    private Network network;
    private Provider<?> provider;
    private ArrayList<ActionParametersBase> setupNetworksParameters;

    public RemoveNetworkCommand(Guid id) {
        super(id);
    }

    public RemoveNetworkCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected Network getNetwork() {
        if (network == null) {
            network = networkDao.get(getNetworkId());
        }

        return network;
    }

    private Guid getNetworkId() {
        return getParameters().getId();
    }

    private Provider<?> getProvider() {
        if (provider == null) {
            provider = providerDao.get(getProviderId());
        }

        return provider;
    }

    private Guid getProviderId() {
        return (getNetwork()==null || !getNetwork().isExternal()) ?
                null : getNetwork().getProvidedBy().getProviderId();
    }

    @Override
    public ActionReturnValue executeAction() {
        setStoragePoolId(getNetwork().getDataCenterId());
        getParameters().setStoragePoolId(getStoragePoolId());
        getParameters().setNetworkName(getNetworkName());
        return super.executeAction();
    }

    @Override
    protected void executeCommand() {
        TransactionSupport.executeInNewTransaction(() -> {
            removeVnicProfiles();
            removeFromClusters();
            networkHelper.setVdsmNamesInVdsInterfaces(getNetwork());
            getCompensationContext().snapshotEntity(getNetwork());
            networkDao.remove(getNetwork().getId());
            getCompensationContext().stateChanged();
            return null;
        });

        if (getNetwork().isExternal()) {
            if (getParameters().isRemoveFromNetworkProvider()) {
                removeExternalNetwork();
            }
        }

        if (NetworkHelper.shouldRemoveNetworkFromHostUponNetworkRemoval(getNetwork())) {
            removeNetworkFromHosts();
        }

        setSucceeded(true);
    }

    private void removeFromClusters() {
        for (NetworkCluster networkCluster : networkClusterDao.getAllForNetwork(getNetwork().getId())) {
            networkClusterHelper.removeNetworkAndReassignRoles(networkCluster);
        }
    }

    private void removeNetworkFromHosts() {
        List<VdsNetworkInterface> nics = interfaceDao.getAllInterfacesByLabelForDataCenter(getStoragePoolId(), network.getLabel());
        setupNetworksParameters = removeNetworkParametersBuilder.buildParameters(network, nics);

        if (!setupNetworksParameters.isEmpty()) {
            HostSetupNetworksParametersBuilder.updateParametersSequencing(setupNetworksParameters);
            setupNetworksParameters.forEach(this::withRootCommandInfo);
            backend.runInternalMultipleActions(ActionType.PersistentHostSetupNetworks, setupNetworksParameters, cloneContextAndDetachFromParent());
        }
    }

    private void removeExternalNetwork() {
        NetworkProviderProxy proxy = providerProxyFactory.create(getProvider());
        proxy.remove(getNetwork().getProvidedBy().getExternalId());
    }

    @Override
    protected void setActionMessageParameters() {
        super.setActionMessageParameters();
        addValidationMessage(EngineMessage.VAR__ACTION__REMOVE);
    }

    @Override
    protected boolean validate() {
        NetworkValidator validator = new NetworkValidator(getNetwork());

        return validate(validator.networkIsSet(getNetworkId()))
                && validate(validator.notRemovingManagementNetwork())
                && validate(validator.notIscsiBondNetwork())
                && validate(validator.networkNotUsedByVms())
                && validate(validator.networkNotUsedByTemplates())
                && validate(getRemoveExternalNetworkValidationResult())
                && validate(validator.notLinkedToExternalNetwork());
    }

    private ValidationResult getRemoveExternalNetworkValidationResult() {
        ProviderNetwork providerNetwork = getNetwork().getProvidedBy();
        if (providerNetwork == null || !getParameters().isRemoveFromNetworkProvider()){
            return ValidationResult.VALID;
        }
        ProviderValidator providerValidator = new ProviderValidator(getProvider());
        return providerValidator.validateReadOnlyActions();
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        switch (getActionState()) {
        case EXECUTE:
            if (!getSucceeded()) {
                return NETWORK_REMOVE_NETWORK_START_ERROR;
            } else if (skipHostSetupNetworks()) {
                return NETWORK_REMOVE_NOTHING_TO_DO;
            } else {
                return NETWORK_REMOVE_NETWORK_STARTED;
            }
        case END_SUCCESS:
            addCustomValues();
            return NETWORK_REMOVE_NETWORK;
        }
        addCustomValues();
        return NETWORK_REMOVE_NETWORK_FAILED;
    }

    private boolean skipHostSetupNetworks() {
        return !NetworkHelper.shouldRemoveNetworkFromHostUponNetworkRemoval(getNetwork()) || setupNetworksParameters.isEmpty();
    }

    private void addCustomValues() {
        addCustomValue("NetworkName", getParameters().getNetworkName());
        setStoragePoolId(getParameters().getStoragePoolId());
        addCustomValue("StoragePoolName", getStoragePoolName());
    }

    @Override
    public Map<String, String> getJobMessageProperties() {
        if (jobProperties == null) {
            jobProperties = super.getJobMessageProperties();
        }
        StoragePool pool = storagePoolDao.get(getNetwork().getDataCenterId());
        jobProperties.put(VdcObjectType.Network.name().toLowerCase(), getNetwork().getName());
        jobProperties.put(VdcObjectType.StoragePool.name().toLowerCase(), pool.getName());
        return jobProperties;
    }

    @Override
    public CommandCallback getCallback() {
        return callbackProvider.get();
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
}

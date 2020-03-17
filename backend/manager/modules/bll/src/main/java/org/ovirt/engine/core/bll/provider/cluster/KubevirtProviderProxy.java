package org.ovirt.engine.core.bll.provider.cluster;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.context.ChildCompensationWrapper;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.bll.kubevirt.ClusterMonitoring;
import org.ovirt.engine.core.bll.kubevirt.KubevirtMonitoring;
import org.ovirt.engine.core.bll.network.cluster.ManagementNetworkUtil;
import org.ovirt.engine.core.bll.provider.ProviderProxy;
import org.ovirt.engine.core.bll.provider.ProviderValidator;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.AttachStorageDomainToPoolParameters;
import org.ovirt.engine.core.common.action.ClusterOperationParameters;
import org.ovirt.engine.core.common.action.ClusterParametersBase;
import org.ovirt.engine.core.common.action.RemoveNetworkParameters;
import org.ovirt.engine.core.common.action.StoragePoolManagementParameter;
import org.ovirt.engine.core.common.action.StoragePoolParametersBase;
import org.ovirt.engine.core.common.action.UnmanagedStorageDomainManagementParameter;
import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.FencingPolicy;
import org.ovirt.engine.core.common.businessentities.KubevirtProviderProperties;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.vdscommands.AddVdsVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dao.ClusterDao;
import org.ovirt.engine.core.dao.StoragePoolDao;
import org.ovirt.engine.core.dao.VdsStaticDao;
import org.ovirt.engine.core.dao.network.NetworkClusterDao;
import org.ovirt.engine.core.dao.network.NetworkDao;
import org.ovirt.engine.core.dao.provider.ProviderDao;
import org.ovirt.engine.core.utils.ReplacementUtils;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;
import org.ovirt.engine.core.vdsbroker.ResourceManager;

import io.kubernetes.client.ApiException;

public class KubevirtProviderProxy implements ProviderProxy<ProviderValidator<KubevirtProviderProperties>> {
    private static final String KUBEVIRT_DC_NAME = "kubevirt";
    private Provider<KubevirtProviderProperties> provider;
    static final String VAR_PROM_URL = "PromUrl";

    @Inject
    private BackendInternal backend;

    @Inject
    private VdsStaticDao vdsStaticDao;

    @Inject
    private Instance<KubevirtMonitoring> monitoring;

    @Inject
    private StoragePoolDao storagePoolDao;

    @Inject
    private ResourceManager resourceManager;

    @Inject
    private ProviderDao providerDao;

    @Inject
    private NetworkDao networkDao;

    @Inject
    private NetworkClusterDao networkClusterDao;

    @Inject
    private ManagementNetworkUtil managementNetworkUtil;

    @Inject
    private AuditLogDirector auditLogDirector;

    @Inject
    private ClusterDao clusterDao;

    /**
     * command's context is required for internal backend actions that requires a user, i.e. creating data-center with
     * networks (for permissions assignments)
     */
    private CommandContext context;

    public KubevirtProviderProxy(Provider<KubevirtProviderProperties> provider) {
        this.provider = provider;
    }

    @Override
    public void testConnection() {
        try {
            monitoring.get().test(provider);
        } catch (Exception e) {
            throw new EngineException(EngineError.PROVIDER_FAILURE, e, true);
        }
    }

    @Override
    public ProviderValidator<KubevirtProviderProperties> getProviderValidator() {
        return new ProviderValidator<KubevirtProviderProperties>(provider) {
            @Override
            public ValidationResult validateAddProvider() {
                ValidationResult deploymentResult = validateKubevirtDeployed();
                if (!deploymentResult.isValid()) {
                    return deploymentResult;
                }

                ValidationResult promValidation = validatePrometheusUrl();
                if (!promValidation.isValid()) {
                    return promValidation;
                }
                ValidationResult templatesResult = validateTemplates();
                if (!templatesResult.isValid()) {
                    return templatesResult;
                }

                return super.validateAddProvider();
            }

            @Override
            public ValidationResult validateUpdateProvider() {
                ValidationResult deploymentResult = validateKubevirtDeployed();
                if (!deploymentResult.isValid()) {
                    return deploymentResult;
                }

                ValidationResult templatesResult = validateTemplates();
                if (!templatesResult.isValid()) {
                    return templatesResult;
                }

                return super.validateUpdateProvider();
            }

            @Override
            public ValidationResult validatePassword() {
                return ValidationResult.VALID;
            }
        };
    }

    @Override
    public void onAddition() {
        updateConsoleUrl();

        // Failure in cluster creation will result in reverting both cluster and provider from the DB.
        Cluster cluster = createCluster();
        createStorage(cluster);

        monitoring.get().register(provider);
    }

    private Cluster createCluster() {
        Cluster cluster = new Cluster();
        cluster.setName(provider.getName());
        cluster.setStoragePoolId(findOrCreateKubevirtDataCenterId());
        cluster.setDescription(provider.getDescription());
        cluster.setCompatibilityVersion(Version.getLast());
        cluster.setArchitecture(ArchitectureType.x86_64);
        cluster.setCpuName("Intel Nehalem Family");
        cluster.setId(provider.getId());
        cluster.setManaged(false);
        FencingPolicy fencingPolicy = new FencingPolicy();
        fencingPolicy.setFencingEnabled(false);
        cluster.setFencingPolicy(fencingPolicy);

        ActionReturnValue returnValue = TransactionSupport.executeInNewTransaction(() -> {
            ClusterOperationParameters parameters = new ClusterOperationParameters(cluster);
            // in order to avoid changing AddClusterCommand to a non-transactional command, we'll add explicitly
            // compensation of a new entity
            parameters.setCompensationEnabled(true);
            return backend.runInternalAction(
                    ActionType.AddCluster,
                    parameters,
                    getContext());
        });
        if (!returnValue.getSucceeded()) {
            throw new EngineException(EngineError.GeneralException, "Failed to create a cluster");
        }
        return cluster;
    }

    private void createStorage(Cluster cluster) {
        StorageDomainStatic sd = new StorageDomainStatic();
        sd.setStorageName(provider.getName());
        sd.setId(provider.getId());
        ActionReturnValue returnValue = backend.runInternalAction(ActionType.AddUnmanagedStorageDomain,
                new UnmanagedStorageDomainManagementParameter(sd),
                getContext());
        if (!returnValue.getSucceeded()) {
            throw new EngineException(EngineError.GeneralException, "Failed to create a storage domain");
        }

        returnValue = backend.runInternalAction(ActionType.AttachStorageDomainToPool,
                new AttachStorageDomainToPoolParameters(sd.getId(), cluster.getStoragePoolId()),
                getContext());
        if (!returnValue.getSucceeded()) {
            throw new EngineException(EngineError.GeneralException, "Failed to attach the storage domain");
        }
    }

    private void updateConsoleUrl() {
        try {
            String consoleUrl = ClusterMonitoring.fetchConsoleUrl(provider, auditLogDirector);
            if (!Objects.equals(provider.getAdditionalProperties().getConsoleUrl(), consoleUrl)) {
                TransactionSupport.executeInNewTransaction(() -> {
                    getContext().getCompensationContext().snapshotEntityUpdated(provider);
                    provider.getAdditionalProperties().setConsoleUrl(consoleUrl);
                    providerDao.update(provider);
                    getContext().getCompensationContext().stateChanged();
                    return null;
                });
            }
        } catch (Exception e) {
            // console url will not be resolved and kept for the provider.
            // we'd might attempt to resolve it once user asked to open a console if missed
        }
    }

    @Override
    public void onModification() {
        // TODO: invoke the logic below only if a meaningful field was changed (not name / description)
        updateConsoleUrl();

        // first unregister current kubevirt cluster resource from engine
        monitoring.get().unregister(provider.getId());

        // remove all hosts that represents kubevirt nodes and cancel their monitoring resources
        vdsStaticDao.getAllForCluster(provider.getId()).forEach(h -> resourceManager.removeVds(h.getId()));

        // register the cluster based on the updated provider's details
        monitoring.get().register(provider);

        // after the sync, registered hosts that were existed before need to reschedule
        vdsStaticDao.getAllForCluster(provider.getId())
                .stream()
                .filter(h -> resourceManager.getVdsManager(h.getId()) == null)
                .forEach(h -> resourceManager.runVdsCommand(VDSCommandType.AddVds,
                        new AddVdsVDSCommandParameters(h.getId())));
    }

    private Optional<StoragePool> findKubevirtDataCenter() {
        return storagePoolDao.getAll().stream().filter(dc -> dc.getName().equals(KUBEVIRT_DC_NAME)).findFirst();
    }

    private Guid findOrCreateKubevirtDataCenterId() {
        Optional<StoragePool> kubevirtDc = findKubevirtDataCenter();

        if (kubevirtDc.isPresent()) {
            return kubevirtDc.get().getId();
        }

        // create an empty data center for kubevirt clusters
        StoragePool dc = new StoragePool();
        dc.setName(KUBEVIRT_DC_NAME);
        dc.setManaged(false);
        StoragePoolManagementParameter params = new StoragePoolManagementParameter(dc);
        params.setCompensationEnabled(true);
        return TransactionSupport.executeInNewTransaction(() ->
                backend.runInternalAction(ActionType.AddEmptyStoragePool,
                        params,
                        getContext())
                        .getActionReturnValue());
    }

    public ValidationResult validatePrometheusUrl() {
        String prometheusUrl = getPrometheusUrl(provider);
        if (prometheusUrl != null) {
            Matcher matcher = ProviderValidator.URL_PATTERN.matcher(prometheusUrl);
            return ValidationResult.failWith(EngineMessage.ACTION_TYPE_FAILED_PROVIDER_INVALID_PROMETHEUS_URL,
                    ReplacementUtils.createSetVariableString(VAR_PROM_URL, prometheusUrl))
                    .when(!matcher.matches());
        }
        return ValidationResult.VALID;
    }

    public ValidationResult validateTemplates() {
        try {
            return ValidationResult.failWith(EngineMessage.ACTION_TYPE_FAILED_PROVIDER_TEMPLATES_NOT_FOUND)
                    .unless(monitoring.get().checkTemplates(provider));
        } catch (IOException | ApiException e) {
            return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_PROVIDER_FAILED_FETCH_TEMPLATES);
        }
    }

    public ValidationResult validateKubevirtDeployed() {
        try {
            return ValidationResult.failWith(EngineMessage.ACTION_TYPE_FAILED_PROVIDER_KUBEVIRT_NOT_FOUND)
                    .unless(monitoring.get().checkDeployment(provider));
        } catch (IOException | ApiException e) {
            return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_PROVIDER_FAILED_FETCH_VERSIONS);
        }
    }

    private static String getPrometheusUrl(Provider<KubevirtProviderProperties> provider) {
        return provider.getAdditionalProperties().getPrometheusUrl();
    }

    @Override
    public void onRemoval() {
        monitoring.get().unregister(provider.getId());
        List<Network> networks = networkDao.getAllForCluster(provider.getId());
        networks.stream()
                .filter(n -> !managementNetworkUtil.getDefaultManagementNetworkName().equals(n.getName()))
                .forEach(n -> deleteNetwork(n, provider.getId()));

        ClusterParametersBase parameters = new ClusterParametersBase(provider.getId());
        parameters.setForce(true);
        backend.runInternalAction(ActionType.RemoveCluster, parameters);

        Optional<StoragePool> kubevirtDc = findKubevirtDataCenter();
        if (kubevirtDc.isPresent()) {
            Guid storagePoolId = kubevirtDc.get().getId();
            List<Cluster> clusters = clusterDao.getAllForStoragePool(storagePoolId);
            if (clusters.isEmpty()) {
                StoragePoolParametersBase sp = new StoragePoolParametersBase(storagePoolId);
                backend.runInternalAction(ActionType.RemoveStoragePool, sp);
            }
        }
    }

    private void deleteNetwork(Network network, Guid clusterId) {
        if (networkClusterDao.getAllForNetwork(network.getId())
                .stream()
                .filter(nc -> !nc.getClusterId().equals(clusterId))
                .count() == 0) {
            backend.runInternalAction(ActionType.RemoveNetwork, new RemoveNetworkParameters(network.getId()));
        }
    }

    @Override
    public void setCommandContext(CommandContext context) {
        this.context = context.clone()
                .withoutExecutionContext()
                .withoutLock()
                .withCompensationContext(new ChildCompensationWrapper(context.getCompensationContext()));
    }

    /**
     * Returns the context of the parent command with default compensation context so all of the entities will be
     * submitted together or deleted on failure.
     *
     * @return the command context to be shared by all of the internal commands
     */
    private CommandContext getContext() {
        return context;
    }
}

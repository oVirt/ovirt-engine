package org.ovirt.engine.core.bll.provider.cluster;

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
import org.ovirt.engine.core.bll.provider.ProviderProxy;
import org.ovirt.engine.core.bll.provider.ProviderValidator;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.AttachStorageDomainToPoolParameters;
import org.ovirt.engine.core.common.action.ClusterOperationParameters;
import org.ovirt.engine.core.common.action.ClusterParametersBase;
import org.ovirt.engine.core.common.action.StoragePoolManagementParameter;
import org.ovirt.engine.core.common.action.UnmanagedStorageDomainManagementParameter;
import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.FencingPolicy;
import org.ovirt.engine.core.common.businessentities.KubevirtProviderProperties;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dao.StoragePoolDao;
import org.ovirt.engine.core.dao.VdsStaticDao;
import org.ovirt.engine.core.dao.provider.ProviderDao;
import org.ovirt.engine.core.utils.ReplacementUtils;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;
import org.ovirt.engine.core.vdsbroker.ResourceManager;

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
                ValidationResult promValidation = validatePrometheusUrl();
                if (!promValidation.isValid()) {
                    return promValidation;
                }

                return super.validateAddProvider();
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
            String consoleUrl = ClusterMonitoring.fetchConsoleUrl(provider);
            if (!Objects.equals(provider.getAdditionalProperties().getConsoleUrl(), consoleUrl)) {
                provider.getAdditionalProperties().setConsoleUrl(consoleUrl);
                providerDao.update(provider);
            }
        } catch (Exception e) {
            // console url will not be resolved and kept for the provider.
            // we'd might attempt to resolve it once user asked to open a console if missed
        }
    }

    @Override
    public void onModification() {
        updateConsoleUrl();
        monitoring.get().unregister(provider.getId());
        vdsStaticDao.getAllForCluster(provider.getId()).forEach(h -> resourceManager.removeVds(h.getId()));
        monitoring.get().register(provider);
    }

    private Guid findOrCreateKubevirtDataCenterId() {
        Optional<StoragePool> kubevirtDc =
                storagePoolDao.getAll().stream().filter(dc -> dc.getName().equals(KUBEVIRT_DC_NAME)).findFirst();

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

    private static String getPrometheusUrl(Provider<KubevirtProviderProperties> provider) {
        return provider.getAdditionalProperties().getPrometheusUrl();
    }

    @Override
    public void onRemoval() {
        monitoring.get().unregister(provider.getId());
        ClusterParametersBase parameters = new ClusterParametersBase(provider.getId());
        parameters.setForce(true);
        backend.runInternalAction(ActionType.RemoveCluster, parameters);
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

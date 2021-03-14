package org.ovirt.engine.core.bll.kubevirt;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.ovirt.engine.core.common.businessentities.KubevirtProviderProperties;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dao.VdsStaticDao;
import org.ovirt.engine.core.vdsbroker.kubevirt.KubevirtAuditUtils;
import org.ovirt.engine.core.vdsbroker.kubevirt.KubevirtUtils;
import org.ovirt.engine.core.vdsbroker.monitoring.kubevirt.KubevirtClusterMigrationMonitoring;
import org.ovirt.engine.core.vdsbroker.monitoring.kubevirt.KubevirtMigrationMonitoring;
import org.ovirt.engine.core.vdsbroker.monitoring.kubevirt.SharedInformerFactoryProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.kubernetes.client.ApiClient;
import io.kubernetes.client.ApiException;
import io.kubernetes.client.informer.SharedInformerFactory;
import kubevirt.io.KubevirtApi;
import kubevirt.io.V1DeleteOptions;
import kubevirt.io.V1Status;
import kubevirt.io.V1VirtualMachine;
import openshift.io.OpenshiftApi;
import openshift.io.V1Route;
import openshift.io.V1RouteList;

public class ClusterMonitoring {
    private static final Logger log = LoggerFactory.getLogger(ClusterMonitoring.class);

    private final Provider<KubevirtProviderProperties> provider;
    private Guid clusterId;

    private VMsMonitoring vmsMonitoring;
    private NodesMonitoring nodesMonitoring;
    private KubevirtClusterMigrationMonitoring migrationMonitoring;
    private DisksMonitoring disksMonitoring;
    private TemplatesMonitoring templatesMonitoring;
    private NetworksMonitoring networksMonitoring;

    @Inject
    private SharedInformerFactoryProducer sharedInformerFactoryProducer;

    @Inject
    private VdsStaticDao vdsStaticDao;

    @Inject
    private HostUpdater hostUpdater;

    @Inject
    private ClusterSyncer clusterSyncer;

    @Inject
    private VmUpdater vmUpdater;

    @Inject DiskUpdater diskUpdater;

    @Inject
    private KubevirtMigrationMonitoring kubevirtMigrationMonitoring;

    @Inject
    private AuditLogDirector auditLogDirector;

    @Inject
    private TemplateUpdater templateUpdater;

    @Inject
    private NetworkUpdater networkUpdater;

    private KubevirtApi api;
    private ApiClient client;
    private SharedInformerFactory sharedInformerFactory;

    public static void testConnectivity(Provider<KubevirtProviderProperties> provider) throws IOException {
        KubevirtApi api = KubevirtUtils.getKubevirtApi(provider);
        try {
            api.checkHealth();
        } catch (ApiException e) {
            log.error("failed to check health of kubevirt provider (url = {}): {}",
                    provider.getUrl(),
                    ExceptionUtils.getRootCauseMessage(e));
            log.debug("Exception", e);
            throw new IOException(e);
        }
        OpenshiftApi ocpApi = KubevirtUtils.getOpenshiftApi(provider);
        try {
            // just a sample call
            // don't care about the results - there only must not be any exceptions thrown
            ocpApi.listNamespacedRoute("openshift-console",
                    null,
                    "metadata.name=console",
                    null,
                    null,
                    null,
                    null,
                    null,
                    Boolean.FALSE);
        } catch (ApiException e) {
            log.error("failed to check OpenShift health of kubevirt provider (url = {}): {}",
                    provider.getUrl(),
                    ExceptionUtils.getRootCauseMessage(e));
            log.debug("Exception", e);
            throw new IOException(e);
        }
    }

    public static String fetchConsoleUrl(Provider<KubevirtProviderProperties> provider,
            AuditLogDirector auditLogDirector) throws IOException, ApiException, URISyntaxException {
        OpenshiftApi api = KubevirtUtils.getOpenshiftApi(provider);
        Optional<V1Route> route;
        try {
            V1RouteList routes = api.listNamespacedRoute("openshift-console",
                    null,
                    "metadata.name=console",
                    null,
                    null,
                    null,
                    null,
                    null,
                    Boolean.FALSE);
            route = routes.getItems().stream().findAny();
        } catch (ApiException e) {
            KubevirtAuditUtils.auditAuthorizationIssues(e, auditLogDirector, provider);
            log.error("failed to retrieve console url for kubevirt provider (url = {}): {}",
                    provider.getUrl(),
                    ExceptionUtils.getRootCauseMessage(e));
            log.debug("Exception", e);
            throw e;
        }

        if (route.isPresent()) {
            String host = route.get().getSpec().getHost();
            String scheme = route.get().getSpec().getPort().getTargetPort();
            try {
                return new URI(scheme, host, null, null).toString();
            } catch (URISyntaxException e) {
                log.error("failed to retrieve console url for kubevirt provider (url = {}): {}",
                        provider.getUrl(),
                        ExceptionUtils.getRootCauseMessage(e));
                log.debug("Exception", e);
                throw e;
            }
        }

        return null;
    }

    public ClusterMonitoring(Provider<KubevirtProviderProperties> provider) {
        this.provider = provider;
        this.clusterId = provider.getId();
    }

    @PostConstruct
    private void init() {
        client = getApiClient();
        sharedInformerFactory = sharedInformerFactoryProducer.newInstance(client);
    }

    public ClusterMonitoring start() {
        client.getHttpClient().setReadTimeout(0, TimeUnit.SECONDS);
        clusterSyncer.sync(client, clusterId);
        nodesMonitoring = new NodesMonitoring(client, clusterId, vdsStaticDao, hostUpdater);
        nodesMonitoring.monitor(sharedInformerFactory);

        networksMonitoring = new NetworksMonitoring(client, clusterId, networkUpdater);
        networksMonitoring.monitor(sharedInformerFactory);

        vmsMonitoring = new VMsMonitoring(client, clusterId, vmUpdater);
        vmsMonitoring.monitor(sharedInformerFactory);

        disksMonitoring = new DisksMonitoring(client, clusterId, diskUpdater);
        disksMonitoring.monitor(sharedInformerFactory);

        migrationMonitoring = new KubevirtClusterMigrationMonitoring(client, auditLogDirector, provider);
        migrationMonitoring.monitor(sharedInformerFactory);
        kubevirtMigrationMonitoring.register(clusterId, migrationMonitoring);

        templatesMonitoring = new TemplatesMonitoring(client, clusterId, templateUpdater);
        templatesMonitoring.monitor(sharedInformerFactory);

        sharedInformerFactory.startAllRegisteredInformers();

        return this;
    }

    private ApiClient getApiClient() {
        try {
            return KubevirtUtils.createApiClient(provider);
        } catch (IOException e) {
            throw new RuntimeException("Failed to connect to kubevirt of provider: " + provider.getId(), e);
        }
    }

    private KubevirtApi getKubevirtApi() {
        if (api == null) {
            try {
                api = KubevirtUtils.getKubevirtApi(provider);
            } catch (IOException e) {
                throw new RuntimeException("Failed to connect to kubevirt of provider: " + provider.getId(), e);
            }
        }
        return api;
    }

    public void stop() {
        sharedInformerFactory.stopAllRegisteredInformers();
        kubevirtMigrationMonitoring.unregister(clusterId);
    }

    public void addVm(V1VirtualMachine vm) {
        try {
            getKubevirtApi().createNamespacedVirtualMachine(vm, vm.getMetadata().getNamespace());
        } catch (ApiException e) {
            handleException(e, "failed to interact with kubevirt create namespaced virtual machine  endpoint");
        }
    }

    public void runVm(VM vm) {
        try {
            getKubevirtApi().start(vm.getNamespace(), vm.getName());
        } catch (ApiException e) {
            handleException(e, "failed to interact with kubevirt start endpoint");
        }
    }

    public void stopVm(VM vm) {
        try {
            getKubevirtApi().stop(vm.getNamespace(), vm.getName());
        } catch (ApiException e) {
            handleException(e, "failed to interact with kubevirt stop endpoint");
        }
    }

    public void migrateVm(VM vm) {
        try {
            getKubevirtApi().migrate(vm.getNamespace(), vm.getName());
        } catch (ApiException ae) {
            ObjectMapper objectMapper = new ObjectMapper();

            String msg = "";
            try {
                V1Status status = objectMapper.readValue(ae.getResponseBody(), V1Status.class);
                msg = status.getMessage();
            } catch (IOException e) {
                // Not useful but in case of parsing failure
                msg = ae.getMessage();
            }

            throw new EngineException(EngineError.migrateErr, msg);
        }
    }

    public void deleteVm(VM vm) {
        try {
            V1DeleteOptions options = new V1DeleteOptions();
            getKubevirtApi().deleteNamespacedVirtualMachine(options, vm.getNamespace(), vm.getName(), null, null, null);
        } catch (ApiException e) {
            handleException(e, "failed to interact with kubevirt delete endpoint");
        }
    }

    public void restartVm(VM vm) {
        try {
            getKubevirtApi().restart(vm.getNamespace(), vm.getName());
        } catch (ApiException e) {
            handleException(e, "failed to interact with kubevirt restart endpoint");
        }
    }

    private void handleException(ApiException e, String message) throws EngineException {
        KubevirtAuditUtils.auditAuthorizationIssues(e, auditLogDirector, provider);
        throw new EngineException(EngineError.unexpected, message);
    }
}

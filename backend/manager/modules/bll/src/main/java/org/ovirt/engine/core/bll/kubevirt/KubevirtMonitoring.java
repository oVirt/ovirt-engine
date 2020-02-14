package org.ovirt.engine.core.bll.kubevirt;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.KubevirtProviderProperties;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.di.Injector;
import org.ovirt.engine.core.vdsbroker.kubevirt.KubevirtUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.kubernetes.client.ApiClient;
import io.kubernetes.client.ApiException;
import kubevirt.io.KubevirtApi;
import kubevirt.io.V1APIGroupList;
import kubevirt.io.V1VirtualMachine;
import openshift.io.OpenshiftApi;
import openshift.io.V1TemplateList;

@Singleton
public class KubevirtMonitoring {
    private static final Logger log = LoggerFactory.getLogger(KubevirtMonitoring.class);

    private Map<Guid, ClusterMonitoring> clusterToJob;

    public KubevirtMonitoring() {
        clusterToJob = new ConcurrentHashMap<>();
    }

    public void test(Provider<KubevirtProviderProperties> provider) {
        log.info("at KubevirtMonitoring#test");
        try {
            ClusterMonitoring.testConnectivity(provider);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
   }

   public void register(Provider<KubevirtProviderProperties> provider) {
       clusterToJob.computeIfAbsent(provider.getId(),
               id -> Injector.injectMembers(new ClusterMonitoring(provider)).start());
    }

    public void unregister(Guid clusterId) {
        ClusterMonitoring monitoring = clusterToJob.remove(clusterId);
        if (monitoring != null) {
            monitoring.stop();
        }
    }

    public boolean checkTemplates(Provider<KubevirtProviderProperties> provider) throws IOException, ApiException {
        ApiClient client = KubevirtUtils.createApiClient(provider);
        OpenshiftApi api = new OpenshiftApi(client);
        String labelSelector = "template.kubevirt.io/type=base";
        V1TemplateList list = api.listKubevirtTemplateForAllNamespaces(null, null, null, labelSelector, null, null, null, null);
        return list.getItems().size() > 0;
    }

    public boolean checkDeployment(Provider<KubevirtProviderProperties> provider) throws IOException, ApiException {
        KubevirtApi client = KubevirtUtils.getKubevirtApi(provider);
        V1APIGroupList apigroup = client.getAPIGroupList();
        return apigroup.getGroups().stream().anyMatch(group -> group.getVersions().stream()
                .anyMatch(version -> version.getGroupVersion().startsWith("kubevirt.io")));
    }

    public void create(Guid clusterId, V1VirtualMachine kvm) {
        clusterToJob.get(clusterId).addVm(kvm);
    }

    public void start(VM vm) {
        clusterToJob.get(vm.getClusterId()).runVm(vm);
    }

    public void stop(VM vm) {
        clusterToJob.get(vm.getClusterId()).stopVm(vm);
    }

    public void migrate(VM vm) {
        clusterToJob.get(vm.getClusterId()).migrateVm(vm);
    }

    public void delete(VM vm) {
        clusterToJob.get(vm.getClusterId()).deleteVm(vm);
    }

    public void restart(VM vm) {
        clusterToJob.get(vm.getClusterId()).restartVm(vm);
    }
}

package org.ovirt.engine.core.vdsbroker.monitoring.kubevirt;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.KubevirtProviderProperties;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.dao.provider.ProviderDao;
import org.ovirt.engine.core.vdsbroker.VdsManager;
import org.ovirt.engine.core.vdsbroker.kubevirt.KubevirtUtils;
import org.ovirt.engine.core.vdsbroker.monitoring.HostConnectionRefresherInterface;

import io.kubernetes.client.ApiClient;
import io.kubernetes.client.informer.SharedInformerFactory;

public class KubevirtHostConnectionRefresher implements HostConnectionRefresherInterface {

    @Inject
    private ProviderDao providerDao;

    @Inject
    private SharedInformerFactoryProducer sharedInformerFactoryProducer;

    private VdsManager vdsManager;
    private ApiClient client;
    private SharedInformerFactory sharedInformerFactory;

    public KubevirtHostConnectionRefresher(VdsManager vdsManager) {
        this.vdsManager = vdsManager;
    }

    @PostConstruct
    public void init() {
        client = getApiClient();
        client.getHttpClient().setReadTimeout(0, TimeUnit.SECONDS);
        sharedInformerFactory = sharedInformerFactoryProducer.newInstance(client);
    }

    @Override public void start() {
        KubevirtNodeMonitoring kubevirtNodeMonitoring = new KubevirtNodeMonitoring(vdsManager, client, getProvider());
        kubevirtNodeMonitoring.monitorNodeUpdates(sharedInformerFactory);
        kubevirtNodeMonitoring.monitorNodeVmsUpdates(sharedInformerFactory);
        sharedInformerFactory.startAllRegisteredInformers();
    }

    @Override public void stop() {
        sharedInformerFactory.stopAllRegisteredInformers();
    }

    private ApiClient getApiClient() {
        try {
            return KubevirtUtils.createApiClient(getProvider());
        } catch (IOException e) {
            throw new RuntimeException("Failed to connect to kubevirt of provider: " + vdsManager.getClusterId(), e);
        }
    }

    private Provider<KubevirtProviderProperties> getProvider() {
        return (Provider<KubevirtProviderProperties>) providerDao.get(vdsManager.getClusterId());
    }
}

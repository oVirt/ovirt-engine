package org.ovirt.engine.core.bll.kubevirt;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.ovirt.engine.core.common.BackendService;
import org.ovirt.engine.core.common.businessentities.KubevirtProviderProperties;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.ProviderType;
import org.ovirt.engine.core.dao.provider.ProviderDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class KubevirtService implements BackendService {
    private static final Logger log = LoggerFactory.getLogger(KubevirtService.class);

    @Inject
    private ProviderDao providerDao;

    @Inject
    private KubevirtMonitoring kubevirtMonitoring;

    @PostConstruct
    public void init() {
        List<Provider<?>> providers = providerDao.getAllByTypes(ProviderType.KUBEVIRT);
        providers.forEach(p -> registerCluster((Provider<KubevirtProviderProperties>) p));
    }

    private void registerCluster(Provider<KubevirtProviderProperties> provider) {
        try {
            kubevirtMonitoring.register(provider);
        } catch (Exception e) {
            log.error("Failed to register KubeVirt cluster for provider {} with URL {}: {}",
                    provider.getName(),
                    provider.getUrl(),
                    e.getMessage());
            log.debug("Exception", e);
        }
    }
}

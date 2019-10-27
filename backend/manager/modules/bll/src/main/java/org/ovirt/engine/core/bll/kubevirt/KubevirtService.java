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

@ApplicationScoped
public class KubevirtService implements BackendService {
    @Inject
    private ProviderDao providerDao;

    @Inject
    private KubevirtMonitoring kubevirtMonitoring;

    @PostConstruct
    public void init() {
        List<Provider<?>> providers = providerDao.getAllByTypes(ProviderType.KUBEVIRT);
        providers.forEach(p -> kubevirtMonitoring.register((Provider<KubevirtProviderProperties>) p));
    }
}

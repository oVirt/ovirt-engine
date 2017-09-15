package org.ovirt.engine.core.bll.network.cluster;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.enterprise.concurrent.ManagedScheduledExecutorService;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.common.BackendService;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.IdParameters;
import org.ovirt.engine.core.common.businessentities.OpenstackNetworkProviderProperties;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.ProviderType;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.dao.provider.ProviderDao;
import org.ovirt.engine.core.utils.threadpool.ThreadPools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class ExternalNetworkSyncService implements BackendService {

    private static final Logger log = LoggerFactory.getLogger(ExternalNetworkSyncService.class);

    @Inject
    @ThreadPools(ThreadPools.ThreadPoolType.EngineScheduledThreadPool)
    private ManagedScheduledExecutorService executor;

    @Inject
    private BackendInternal backendInternal;

    @Inject
    private ProviderDao providerDao;

    @PostConstruct
    private void init() {
        int refreshRate = Config.getValue(ConfigValues.ExternalNetworkProviderSynchronizationRate);
        executor.scheduleWithFixedDelay(this::synchronize, refreshRate, refreshRate, TimeUnit.SECONDS);
    }

    private void synchronize() {

        List<Provider<?>> networkProviders = providerDao.getAllByTypes(ProviderType.EXTERNAL_NETWORK,
                ProviderType.OPENSTACK_NETWORK);

        networkProviders.stream()
                .filter(provider -> {
                    OpenstackNetworkProviderProperties networkProperties =
                            (OpenstackNetworkProviderProperties) provider.getAdditionalProperties();
                    return networkProperties.getAutoSync();
                })
                .map(Provider::getId)
                .distinct()
                .forEach(providerId -> {
                    backendInternal.runInternalAction(ActionType.SyncNetworkProvider, new IdParameters(providerId));
                });
        }
}

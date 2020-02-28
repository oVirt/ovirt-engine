package org.ovirt.engine.core.bll.host.provider.foreman;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.host.provider.HostProviderProxy;
import org.ovirt.engine.core.bll.provider.ProviderProxyFactory;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.ProviderType;
import org.ovirt.engine.core.dao.provider.ProviderDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EngineForemanProviderFinder {

    private static Logger log = LoggerFactory.getLogger(EngineForemanProviderFinder.class);
    private String engineHostName;

    @Inject
    private ProviderDao providerDao;

    @Inject
    private ProviderProxyFactory providerProxyFactory;

    public HostProviderProxy findEngineProvider() {
        engineHostName = resolveEngineHostName();
        if (engineHostName == null) {
            return null;
        }

        List<Provider<?>> hostProviders = providerDao.getAllByTypes(ProviderType.FOREMAN);
        HostProviderProxy proxy;
        for (Provider<?> provider : hostProviders) {
            proxy = providerProxyFactory.create(provider);
            if (proxy.isContentHostExist(ContentHostIdentifier.builder().withName(engineHostName).build())) {
                return proxy;
            }
        }

        log.error("Failed to find host on any provider by host name '{}' ", engineHostName);
        return null;
    }

    public String getEngineHostName() {
        return engineHostName;
    }

    private String resolveEngineHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            log.error("Failed to resolve hostname for the engine server", e);
            return null;
        }
    }
}

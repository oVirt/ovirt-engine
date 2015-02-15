package org.ovirt.engine.core.bll.host.provider.foreman;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.host.provider.HostProviderProxy;
import org.ovirt.engine.core.bll.provider.ProviderProxyFactory;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.ProviderType;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SystemProviderFinder {

    private static Logger log = LoggerFactory.getLogger(SystemProviderFinder.class);
    private String systemHostName;

    @Inject
    private DbFacade dbFacade;

    public HostProviderProxy findSystemProvider() {
        systemHostName = resolveSystemHostName();
        if (systemHostName == null) {
            return null;
        }

        List<Provider<?>> hostProviders = dbFacade.getProviderDao().getAllByType(ProviderType.FOREMAN);
        HostProviderProxy proxy = null;
        for (Provider<?> provider : hostProviders) {
            proxy = (HostProviderProxy) ProviderProxyFactory.getInstance().create(provider);
            if (proxy.findContentHost(systemHostName) != null) {
                return proxy;
            }
        }

        return null;
    }

    public String getSystemHostName() {
        return systemHostName;
    }

    private String resolveSystemHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            log.error("Failed to resolve hostname for the ovir-engine server", e);
            return null;
        }
    }
}

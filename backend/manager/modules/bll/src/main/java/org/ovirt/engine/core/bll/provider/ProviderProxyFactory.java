package org.ovirt.engine.core.bll.provider;

import org.ovirt.engine.core.bll.host.provider.foreman.ForemanHostProviderProxy;
import org.ovirt.engine.core.common.businessentities.Provider;

/**
 * The provider proxy factory can create a provider proxy according to the provider definition.
 */
public class ProviderProxyFactory {

    private static final ProviderProxyFactory INSTANCE = new ProviderProxyFactory();

    private ProviderProxyFactory() {
        // Singleton private c'tor
    }

    /**
     * Create the proxy used to communicate with the given provider.
     *
     * @param provider
     *            The provider to create the proxy for.
     * @return The proxy for communicating with the provider
     */
    public <P extends ProviderProxy> P create(Provider provider) {
        return (P) new ForemanHostProviderProxy(provider);
    }

    /**
     * Return the {@link ProviderProxyFactory} for using as a dependency.
     *
     * @return The {@link ProviderProxyFactory}
     */
    public static ProviderProxyFactory getInstance() {
        return INSTANCE;
    }
}

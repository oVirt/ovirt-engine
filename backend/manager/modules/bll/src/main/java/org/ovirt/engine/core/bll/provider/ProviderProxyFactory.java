package org.ovirt.engine.core.bll.provider;

import org.ovirt.engine.core.common.businessentities.Provider;

/**
 * The provider proxy factory can create a provider proxy according to the provider definition.
 */
public class ProviderProxyFactory {

    /**
     * Create the proxy used to communicate with the given provider.
     *
     * @param provider
     *            The provider to create the proxy for.
     * @return The proxy for communicating with the provider
     */
    public static ProviderProxy create(Provider provider) {
        return null;
    }
}

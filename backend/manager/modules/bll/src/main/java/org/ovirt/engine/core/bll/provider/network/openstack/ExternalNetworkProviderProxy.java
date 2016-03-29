package org.ovirt.engine.core.bll.provider.network.openstack;

import org.ovirt.engine.core.common.businessentities.ExternalNetworkProviderProperties;
import org.ovirt.engine.core.common.businessentities.Provider;

import com.woorea.openstack.base.client.OpenStackTokenProvider;
import com.woorea.openstack.quantum.Quantum;

public class ExternalNetworkProviderProxy extends BaseNetworkProviderProxy<ExternalNetworkProviderProperties> {
    public ExternalNetworkProviderProxy(Provider<ExternalNetworkProviderProperties> provider) {
        super(provider);
    }

    @Override
    protected void setClientTokenProvider(Quantum client) {
        OpenStackTokenProvider tokenProvider = new ExternalNetworkTokenProvider(provider);
        client.setTokenProvider(tokenProvider);
    }
}

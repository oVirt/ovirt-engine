package org.ovirt.engine.core.bll.provider.network.openstack;

import org.ovirt.engine.core.bll.provider.NetworkProviderValidator;
import org.ovirt.engine.core.bll.provider.ProviderValidator;
import org.ovirt.engine.core.common.businessentities.OpenstackNetworkProviderProperties;
import org.ovirt.engine.core.common.businessentities.Provider;

public class OpenstackNetworkProviderProxy extends BaseNetworkProviderProxy<OpenstackNetworkProviderProperties> {

    public OpenstackNetworkProviderProxy(Provider<OpenstackNetworkProviderProperties> provider) {
        super(provider);
    }

    @Override
    public ProviderValidator getProviderValidator() {
        return new NetworkProviderValidator(getProvider());
    }
}

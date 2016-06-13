package org.ovirt.engine.core.bll.provider.network.openstack;

import java.util.Map;

import org.ovirt.engine.core.bll.provider.NetworkProviderValidator;
import org.ovirt.engine.core.bll.provider.ProviderValidator;
import org.ovirt.engine.core.common.businessentities.OpenstackNetworkProviderProperties;
import org.ovirt.engine.core.common.businessentities.Provider;
import com.woorea.openstack.quantum.model.Port;

public class OpenstackNetworkProviderProxy extends BaseNetworkProviderProxy<OpenstackNetworkProviderProperties> {

    public OpenstackNetworkProviderProxy(Provider<OpenstackNetworkProviderProperties> provider) {
        super(provider);
    }

    @Override
    protected Map<String, String> createPortAllocationRuntimeProperties(Port port) {
        Map<String, String> runtimeProperties = super.createPortAllocationRuntimeProperties(port);
        runtimeProperties.put("plugin_type", provider.getAdditionalProperties().getPluginType());
        return runtimeProperties;
    }

    @Override
    public ProviderValidator getProviderValidator() {
        return new NetworkProviderValidator(provider);
    }
}

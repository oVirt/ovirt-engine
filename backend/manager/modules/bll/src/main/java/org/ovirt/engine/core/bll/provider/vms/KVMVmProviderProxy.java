package org.ovirt.engine.core.bll.provider.vms;

import org.ovirt.engine.core.common.businessentities.KVMVmProviderProperties;
import org.ovirt.engine.core.common.businessentities.OriginType;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.queries.GetVmsFromExternalProviderQueryParameters;

public class KVMVmProviderProxy extends AbstractVmProviderProxy<KVMVmProviderProperties> {

    public KVMVmProviderProxy(Provider<KVMVmProviderProperties> provider) {
        super(provider);
    }

    @Override
    protected GetVmsFromExternalProviderQueryParameters buildGetVmsFromExternalProviderQueryParameters() {
        return new GetVmsFromExternalProviderQueryParameters(
                provider.getUrl(),
                provider.getUsername(),
                provider.getPassword(),
                OriginType.KVM,
                provider.getAdditionalProperties().getProxyHostId(),
                provider.getAdditionalProperties().getStoragePoolId()
                );
    }
}

package org.ovirt.engine.core.bll.provider.vms;

import org.ovirt.engine.core.common.businessentities.OriginType;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.VmwareVmProviderProperties;
import org.ovirt.engine.core.common.queries.GetVmsFromExternalProviderQueryParameters;

public class VmwareVmProviderProxy extends AbstractVmProviderProxy<VmwareVmProviderProperties> {

    public VmwareVmProviderProxy(Provider<VmwareVmProviderProperties> provider) {
        super(provider);
    }

    @Override
    protected GetVmsFromExternalProviderQueryParameters buildGetVmsFromExternalProviderQueryParameters() {
        return new GetVmsFromExternalProviderQueryParameters(
                provider.getUrl(),
                provider.getUsername(),
                provider.getPassword(),
                OriginType.VMWARE,
                provider.getAdditionalProperties().getProxyHostId(),
                provider.getAdditionalProperties().getStoragePoolId()
                );
    }
}

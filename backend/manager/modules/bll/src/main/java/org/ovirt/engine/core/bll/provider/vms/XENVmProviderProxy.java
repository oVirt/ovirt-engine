package org.ovirt.engine.core.bll.provider.vms;

import org.ovirt.engine.core.common.businessentities.OriginType;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.XENVmProviderProperties;
import org.ovirt.engine.core.common.queries.GetVmsFromExternalProviderQueryParameters;

public class XENVmProviderProxy extends AbstractVmProviderProxy<XENVmProviderProperties> {

    public XENVmProviderProxy(Provider<XENVmProviderProperties> provider) {
        super(provider);
    }

    @Override
    protected GetVmsFromExternalProviderQueryParameters buildGetVmsFromExternalProviderQueryParameters() {
        return new GetVmsFromExternalProviderQueryParameters(
                provider.getUrl(),
                "",
                "",
                OriginType.XEN,
                provider.getAdditionalProperties().getProxyHostId(),
                provider.getAdditionalProperties().getStoragePoolId()
                );
    }
}

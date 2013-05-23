package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.common.businessentities.ProviderType;

public class GetAllProvidersParameters extends VdcQueryParametersBase {

    private static final long serialVersionUID = 1L;
    private ProviderType providerType;

    public GetAllProvidersParameters(ProviderType providerType) {
        super();
        this.providerType = providerType;
    }

    public GetAllProvidersParameters() {
        super();
    }

    public ProviderType getProviderType() {
        return providerType;
    }

    public void setProviderType(ProviderType providerType) {
        this.providerType = providerType;
    }
}

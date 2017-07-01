package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.common.businessentities.ProviderType;

public class GetAllProvidersParameters extends QueryParametersBase {

    private static final long serialVersionUID = 1L;
    private ProviderType[] providerTypes;

    public GetAllProvidersParameters(ProviderType ... providerType) {
        this.providerTypes = providerType;
    }

    public GetAllProvidersParameters() {
        super();
    }

    public ProviderType[] getProviderTypes() {
        return providerTypes;
    }
}

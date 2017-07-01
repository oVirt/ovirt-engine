package org.ovirt.engine.core.common.queries;

import javax.validation.Valid;

import org.ovirt.engine.core.common.businessentities.Provider;

public class ProviderQueryParameters extends QueryParametersBase {

    private static final long serialVersionUID = 308877238121233739L;

    @Valid
    private Provider<?> provider;

    public ProviderQueryParameters() {
    }

    public ProviderQueryParameters(Provider<?> provider) {
        this.provider = provider;
    }

    public Provider<?> getProvider() {
        return provider;
    }

    public void setProvider(Provider<?> provider) {
        this.provider = provider;
    }

}

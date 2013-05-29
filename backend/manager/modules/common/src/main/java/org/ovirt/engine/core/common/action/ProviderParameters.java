package org.ovirt.engine.core.common.action;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.ovirt.engine.core.common.businessentities.Provider;

public class ProviderParameters extends VdcActionParametersBase {

    private static final long serialVersionUID = 308877238353433739L;

    @Valid
    @NotNull
    private Provider<?> provider;

    public ProviderParameters() {
    }

    public ProviderParameters(Provider<?> provider) {
        this.provider = provider;
    }

    public Provider<?> getProvider() {
        return provider;
    }

    public void setProvider(Provider<?> provider) {
        this.provider = provider;
    }
}

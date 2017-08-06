package org.ovirt.engine.core.common.action;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.ovirt.engine.core.common.businessentities.Provider;

public class ProviderParameters extends ActionParametersBase {

    private static final long serialVersionUID = 308877238353433739L;

    @Valid
    @NotNull
    private Provider<?> provider;

    private boolean force;

    public ProviderParameters() {
    }

    public ProviderParameters(Provider<?> provider) {
        this(provider, false);
    }

    public ProviderParameters(Provider<?> provider, boolean force) {
        this.provider = provider;
        this.force = force;
    }

    public Provider<?> getProvider() {
        return provider;
    }

    public void setProvider(Provider<?> provider) {
        this.provider = provider;
    }

    public boolean isForce() {
        return force;
    }

    public void setForce(boolean force) {
        this.force = force;
    }
}

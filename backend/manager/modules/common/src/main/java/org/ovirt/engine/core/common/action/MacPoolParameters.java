package org.ovirt.engine.core.common.action;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.ovirt.engine.core.common.businessentities.MacPool;

public class MacPoolParameters extends ActionParametersBase {

    public MacPoolParameters() {
    }

    @NotNull
    @Valid
    private MacPool macPool;

    public MacPoolParameters(MacPool macPool) {
        this.macPool = macPool;
    }

    public MacPool getMacPool() {
        return macPool;
    }

    public void setMacPool(MacPool macPool) {
        this.macPool = macPool;
    }
}

package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.common.businessentities.VM;

public class GetVmUpdatesOnNextRunExistsParameters extends VdcQueryParametersBase {

    private VM original;

    private VM updated;

    public GetVmUpdatesOnNextRunExistsParameters() {
    }

    public GetVmUpdatesOnNextRunExistsParameters(VM original, VM updated) {
        this.original = original;
        this.updated = updated;
    }

    public VM getOriginal() {
        return original;
    }

    public VM getUpdated() {
        return updated;
    }

    public void setOriginal(VM original) {
        this.original = original;
    }

    public void setUpdated(VM updated) {
        this.updated = updated;
    }
}

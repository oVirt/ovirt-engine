package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.common.businessentities.VmDeviceId;

public class VmDeviceIdQueryParameters extends QueryParametersBase {
    private VmDeviceId id;

    public VmDeviceIdQueryParameters() {
    }

    public VmDeviceIdQueryParameters(VmDeviceId id) {
        this.id = id;
    }

    public VmDeviceId getId() {
        return id;
    }

    public void setId(VmDeviceId id) {
        this.id = id;
    }
}

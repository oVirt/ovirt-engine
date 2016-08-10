package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.Guid;

public class MaintenanceVdsParameters extends VdsActionParameters {
    private static final long serialVersionUID = -962696566094119431L;
    private boolean internal;
    private boolean stopGlusterService;

    public MaintenanceVdsParameters() {
    }

    public MaintenanceVdsParameters(Guid vdsId, boolean internal) {
        super(vdsId);
        this.internal = internal;
    }

    public MaintenanceVdsParameters(Guid vdsId, boolean internal, boolean stopGlusterService) {
        this(vdsId, internal);
        this.stopGlusterService = stopGlusterService;
    }

    public boolean isInternal() {
        return internal;
    }

    public boolean isStopGlusterService() {
        return stopGlusterService;
    }

}

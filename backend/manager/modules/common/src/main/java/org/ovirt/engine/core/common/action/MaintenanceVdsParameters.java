package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.Guid;

public class MaintenanceVdsParameters extends VdsActionParameters {
    private static final long serialVersionUID = -962696566094119431L;
    private boolean _isInternal;
    private boolean stopGlusterService;

    public MaintenanceVdsParameters(Guid vdsId, boolean isInternal) {
        super(vdsId);
        _isInternal = isInternal;
    }

    public MaintenanceVdsParameters(Guid vdsId, boolean isInternal, boolean stopGlusterService) {
        this(vdsId, isInternal);
        this.stopGlusterService = stopGlusterService;
    }

    public boolean getIsInternal() {
        return _isInternal;
    }

    public boolean isStopGlusterService() {
        return stopGlusterService;
    }

    public MaintenanceVdsParameters() {
    }
}

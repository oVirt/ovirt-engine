package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.common.businessentities.HaMaintenanceMode;
import org.ovirt.engine.core.compat.Guid;

public class SetHaMaintenanceParameters extends VdsActionParameters {
    private static final long serialVersionUID = -3296572537630656681L;
    private HaMaintenanceMode mode;
    private boolean enabled;

    public SetHaMaintenanceParameters(Guid vdsId, HaMaintenanceMode mode, boolean enabled) {
        super(vdsId);
        this.mode = mode;
        this.enabled = enabled;
    }

    public SetHaMaintenanceParameters(HaMaintenanceMode mode, boolean enabled) {
        this(null, mode, enabled);
    }

    public SetHaMaintenanceParameters() {
    }

    public HaMaintenanceMode getMode() {
        return mode;
    }

    public boolean getIsEnabled() {
        return enabled;
    }
}

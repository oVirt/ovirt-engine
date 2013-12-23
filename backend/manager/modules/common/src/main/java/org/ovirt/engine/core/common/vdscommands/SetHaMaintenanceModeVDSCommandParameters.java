package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.common.businessentities.HaMaintenanceMode;
import org.ovirt.engine.core.common.businessentities.VDS;

/**
 * Parameters used to change Hosted Engine maintenance mode
 */
public class SetHaMaintenanceModeVDSCommandParameters extends VdsIdVDSCommandParametersBase {
    private HaMaintenanceMode mode;
    private boolean enabled;

    public SetHaMaintenanceModeVDSCommandParameters(VDS vds, HaMaintenanceMode mode, boolean enabled) {
        super(vds.getId());
        this.mode = mode;
        this.enabled = enabled;
    }

    public SetHaMaintenanceModeVDSCommandParameters() {
    }

    public HaMaintenanceMode getMode() {
        return mode;
    }

    public void setMode(HaMaintenanceMode mode) {
        this.mode = mode;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}

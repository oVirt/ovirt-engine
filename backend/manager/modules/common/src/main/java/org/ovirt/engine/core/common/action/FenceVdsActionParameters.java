package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.common.businessentities.FenceActionType;
import org.ovirt.engine.core.compat.Guid;

public class FenceVdsActionParameters extends VdsActionParameters {
    private static final long serialVersionUID = 6174371941176548263L;
    private boolean changeHostToMaintenanceOnStart=false;

    /*
     * If the power management policy is responsible for this action
     * pass true so we keep the powerManagementControlledByPolicy flag set.
     *
     * If the user triggered this action, clear the flag.
     */
    private boolean keepPolicyPMEnabled;

    private FenceActionType action;

    public FenceVdsActionParameters() {
        this(null, FenceActionType.Restart);
    }

    public FenceVdsActionParameters(Guid vdsId, FenceActionType action) {
        this(vdsId, action, false);
    }

    public FenceVdsActionParameters(Guid vdsId, FenceActionType action, boolean keepPolicyPMEnabled) {
        super(vdsId);
        this.action = action;
        this.keepPolicyPMEnabled = keepPolicyPMEnabled;
    }

    public FenceActionType getAction() {
        return action;
    }

    public boolean getKeepPolicyPMEnabled() {
        return keepPolicyPMEnabled;
    }

    public void setKeepPolicyPMEnabled(boolean _keepPolicyPMEnabled) {
        this.keepPolicyPMEnabled = _keepPolicyPMEnabled;
    }

    public boolean isChangeHostToMaintenanceOnStart() {
        return changeHostToMaintenanceOnStart;
    }

    public void setChangeHostToMaintenanceOnStart(boolean changeHostStatusOnStart) {
        this.changeHostToMaintenanceOnStart = changeHostStatusOnStart;
    }
}

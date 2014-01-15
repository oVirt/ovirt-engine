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
    private boolean keepPolicyPMEnabled = false;

    public FenceVdsActionParameters(Guid vdsId, FenceActionType action) {
        super(vdsId);
        _action = action;
    }

    public FenceVdsActionParameters(Guid vdsId, FenceActionType action, boolean keepPolicyPMEnabled) {
        this(vdsId, action);
        this.keepPolicyPMEnabled = keepPolicyPMEnabled;
    }

    private FenceActionType _action;

    public FenceActionType getAction() {
        return _action;
    }

    public FenceVdsActionParameters() {
        _action = FenceActionType.Restart;
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

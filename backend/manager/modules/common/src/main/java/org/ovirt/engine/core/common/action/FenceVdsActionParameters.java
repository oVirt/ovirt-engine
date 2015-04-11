package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.common.businessentities.FencingPolicy;
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

    private FencingPolicy fencingPolicy;

    public FenceVdsActionParameters() {
        this(null);
    }

    public FenceVdsActionParameters(Guid vdsId) {
        this(vdsId, false);
    }

    public FenceVdsActionParameters(Guid vdsId, boolean keepPolicyPMEnabled) {
        super(vdsId);
        this.keepPolicyPMEnabled = keepPolicyPMEnabled;
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

    public FencingPolicy getFencingPolicy() {
        return fencingPolicy;
    }

    public void setFencingPolicy(FencingPolicy fencingPolicy) {
        this.fencingPolicy = fencingPolicy;
    }
}

package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.Guid;

public class VdsPowerDownParameters extends VdsActionParameters {
    /*
     * If the power management policy is responsible for this action
     * pass true so we keep the powerManagementControlledByPolicy flag set.
     *
     * If the user triggered this action, clear the flag.
     */
    private boolean keepPolicyPMEnabled = false;

    /*
     * If this flag is set and ssh fails, then the command
     * tries the standard power management procedures to cut the power.
     */
    private boolean fallbackToPowerManagement = true;

    public VdsPowerDownParameters(Guid vdsId) {
        super(vdsId);
    }

    public VdsPowerDownParameters() {
    }

    public boolean getKeepPolicyPMEnabled() {
        return keepPolicyPMEnabled;
    }

    public void setKeepPolicyPMEnabled(boolean keepPolicyPMEnabled) {
        this.keepPolicyPMEnabled = keepPolicyPMEnabled;
    }

    public boolean getFallbackToPowerManagement() {
        return fallbackToPowerManagement;
    }

    public void setFallbackToPowerManagement(boolean fallbackToPowerManagement) {
        this.fallbackToPowerManagement = fallbackToPowerManagement;
    }
}

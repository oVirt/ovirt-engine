package org.ovirt.engine.core.common.action;

import java.io.Serializable;

import org.ovirt.engine.core.compat.Guid;

public class AttachUserToVmFromPoolAndRunParameters extends VmPoolUserParameters implements Serializable {

    private static final long serialVersionUID = -5672324868972973061L;

    private boolean vmPrestarted;
    private boolean nonPrestartedVmLocked;

    public AttachUserToVmFromPoolAndRunParameters() {
    }

    public AttachUserToVmFromPoolAndRunParameters(Guid vmPoolId, Guid userId) {
        super(vmPoolId, userId);
    }

    public boolean isVmPrestarted() {
        return vmPrestarted;
    }

    public void setVmPrestarted(boolean vmPrestarted) {
        this.vmPrestarted = vmPrestarted;
    }

    public boolean isNonPrestartedVmLocked() {
        return nonPrestartedVmLocked;
    }

    public void setNonPrestartedVmLocked(boolean nonPrestartedVmLocked) {
        this.nonPrestartedVmLocked = nonPrestartedVmLocked;
    }

}

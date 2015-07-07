package org.ovirt.engine.core.common.action;

import java.io.Serializable;

import org.ovirt.engine.core.compat.Guid;

public class RemoveVmParameters extends VmOperationParameterBase implements Serializable {

    private static final long serialVersionUID = 4931085923357689965L;
    private boolean force;
    private boolean removeDisks;
    private boolean removePermissions;

    public boolean getForce() {
        return force;
    }

    private void setForce(boolean force) {
        this.force = force;
    }

    public boolean isRemoveDisks() {
        return removeDisks;
    }

    public void setRemoveDisks(boolean removeDisks) {
        this.removeDisks = removeDisks;
    }

    public boolean isRemovePermissions() {
        return removePermissions;
    }

    public void setRemovePermissions(boolean removePermissions) {
        this.removePermissions = removePermissions;
    }

    public RemoveVmParameters(Guid vmId, boolean force) {
        super(vmId);
        setForce(force);
        removeDisks = true;
        removePermissions = true;
    }

    public RemoveVmParameters(Guid vmId, boolean force, boolean removeDisks) {
        this(vmId, force);
        setRemoveDisks(removeDisks);
    }

    public RemoveVmParameters() {
        removeDisks = true;
        removePermissions = true;
    }
}

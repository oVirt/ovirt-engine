package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.Guid;

public class RemoveVmParameters extends VmOperationParameterBase implements java.io.Serializable {

    private static final long serialVersionUID = 4931085923357689965L;
    private boolean force;
    private boolean removeDisks = true;


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

    public RemoveVmParameters(Guid vmId, boolean force) {
        super(vmId);
        setForce(force);
    }

    public RemoveVmParameters(Guid vmId, boolean force, boolean removeDisks) {
        this(vmId, force);
        setRemoveDisks(removeDisks);
    }

    public RemoveVmParameters() {
    }
}

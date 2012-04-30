package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.*;

public class RemoveVmParameters extends VmOperationParameterBase implements java.io.Serializable {
    private static final long serialVersionUID = -6256468461166321723L;
    private boolean privateForce;

    public boolean getForce() {
        return privateForce;
    }

    private void setForce(boolean value) {
        privateForce = value;
    }

    public RemoveVmParameters(Guid vmId, boolean force) {
        super(vmId);
        setForce(force);
    }

    public RemoveVmParameters() {
    }
}

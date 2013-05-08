package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.Guid;

public class HibernateVmParameters extends VmOperationParameterBase implements java.io.Serializable {
    private static final long serialVersionUID = 4526154915680207381L;
    private boolean privateAutomaticSuspend;

    public boolean getAutomaticSuspend() {
        return privateAutomaticSuspend;
    }

    public void setAutomaticSuspend(boolean value) {
        privateAutomaticSuspend = value;
    }

    public HibernateVmParameters(Guid vmId) {
        super(vmId);
    }

    public HibernateVmParameters() {
    }
}

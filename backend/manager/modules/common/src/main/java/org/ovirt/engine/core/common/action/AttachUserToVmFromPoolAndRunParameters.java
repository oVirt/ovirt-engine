package org.ovirt.engine.core.common.action;

import java.io.Serializable;

import org.ovirt.engine.core.compat.Guid;

public class AttachUserToVmFromPoolAndRunParameters extends VmPoolUserParameters implements Serializable {

    private static final long serialVersionUID = -5672324868972973061L;

    public AttachUserToVmFromPoolAndRunParameters() {
    }

    public AttachUserToVmFromPoolAndRunParameters(Guid vmPoolId, Guid userId, boolean internal) {
        super(vmPoolId, userId);
        setInternal(internal);
    }

    private boolean internal;

    public boolean isInternal() {
        return internal;
    }

    private void setInternal(boolean value) {
        internal = value;
    }

}

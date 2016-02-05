package org.ovirt.engine.core.common.action;

import java.io.Serializable;

import org.ovirt.engine.core.compat.Guid;

public class VmPoolUserParameters extends VmPoolSimpleUserParameters implements Serializable {
    private static final long serialVersionUID = -5672324868972973061L;

    public VmPoolUserParameters(Guid vmPoolId, Guid userId, boolean isInternal) {
        super(vmPoolId, userId);
        setIsInternal(isInternal);
    }

    private boolean privateIsInternal;

    public boolean getIsInternal() {
        return privateIsInternal;
    }

    private void setIsInternal(boolean value) {
        privateIsInternal = value;
    }

    public VmPoolUserParameters() {
    }
}

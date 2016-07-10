package org.ovirt.engine.core.common.action;

import javax.validation.constraints.NotNull;

import org.ovirt.engine.core.compat.Guid;

public class VmPoolUserParameters extends VmPoolParametersBase {
    private static final long serialVersionUID = -956095100193433604L;

    @NotNull
    private Guid userId;

    public VmPoolUserParameters(Guid vmPoolId, Guid userId) {
        super(vmPoolId);
        this.userId = userId;
        vmId = Guid.Empty;
    }

    public VmPoolUserParameters(Guid vmPoolId, Guid userId, Guid vmId) {
        this(vmPoolId, userId);
        this.vmId = vmId;
    }

    public Guid getUserId() {
        return userId;
    }

    private Guid vmId;

    public Guid getVmId() {
        return vmId;
    }

    public void setVmId(Guid value) {
        vmId = value;
    }

    public VmPoolUserParameters() {
        userId = Guid.Empty;
        vmId = Guid.Empty;
    }
}

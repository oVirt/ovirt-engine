package org.ovirt.engine.core.common.action;

import javax.validation.constraints.NotNull;

import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NGuid;

public class VmPoolSimpleUserParameters extends VmPoolParametersBase {
    private static final long serialVersionUID = -956095100193433604L;

    @NotNull
    private Guid userId = Guid.Empty;

    public VmPoolSimpleUserParameters(NGuid vmPoolId, Guid userId) {
        super(vmPoolId);
        this.userId = userId;
    }

    public VmPoolSimpleUserParameters(NGuid vmPoolId, Guid userId, Guid vmId) {
        this(vmPoolId, userId);
        this.vmId = vmId;
    }

    public Guid getUserId() {
        return userId;
    }

    private Guid vmId = Guid.Empty;

    public Guid getVmId() {
        return vmId;
    }

    public void setVmId(Guid value) {
        vmId = value;
    }

    public VmPoolSimpleUserParameters() {
    }
}

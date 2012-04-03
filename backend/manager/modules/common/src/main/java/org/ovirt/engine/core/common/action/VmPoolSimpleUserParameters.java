package org.ovirt.engine.core.common.action;

import javax.validation.constraints.NotNull;

import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NGuid;

public class VmPoolSimpleUserParameters extends VmPoolParametersBase {
    private static final long serialVersionUID = -956095100193433604L;

    @NotNull
    private Guid _userId = Guid.Empty;

    public VmPoolSimpleUserParameters(NGuid vmPoolId, Guid userId) {
        super(vmPoolId);
        _userId = userId;
    }

    public Guid getUserId() {
        return _userId;
    }

    public VmPoolSimpleUserParameters() {
    }
}

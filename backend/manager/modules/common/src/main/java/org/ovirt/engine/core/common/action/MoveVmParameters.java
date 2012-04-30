package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.Guid;

public class MoveVmParameters extends MoveOrCopyParameters implements java.io.Serializable {
    private static final long serialVersionUID = -168358966446399575L;

    public MoveVmParameters(Guid vmId, Guid storageDomainId) {
        super(vmId, storageDomainId);
    }

    public MoveVmParameters() {
    }
}

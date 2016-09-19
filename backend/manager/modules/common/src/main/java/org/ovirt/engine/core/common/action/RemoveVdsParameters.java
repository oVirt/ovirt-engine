package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.Guid;

public class RemoveVdsParameters extends VdsActionParameters {
    private static final long serialVersionUID = 3959465593772384532L;

    private boolean forceAction;

    public RemoveVdsParameters() {
    }

    public RemoveVdsParameters(Guid vdsId) {
        super(vdsId);
    }

    public RemoveVdsParameters(Guid vdsId, boolean forceAction) {
     super(vdsId);
     this.forceAction = forceAction;
    }

    public boolean isForceAction() {
        return forceAction;
    }

    public void setForceAction(boolean forceAction) {
        this.forceAction = forceAction;
    }
}

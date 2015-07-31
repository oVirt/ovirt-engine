package org.ovirt.engine.core.common.action.gluster;

import org.ovirt.engine.core.common.action.VdsActionParameters;
import org.ovirt.engine.core.compat.Guid;

public class SyncGlusterStorageDevicesParameter extends VdsActionParameters {
    private static final long serialVersionUID = 3959465593772384532L;

    private boolean forceAction;

    public SyncGlusterStorageDevicesParameter() {
    }

    public SyncGlusterStorageDevicesParameter(Guid vdsId) {
        super(vdsId);
    }

    public SyncGlusterStorageDevicesParameter(Guid vdsId, boolean forceAction) {
     super(vdsId);
     this.forceAction = forceAction;
    }

    public boolean isForceAction() {
        return forceAction;
    }
}

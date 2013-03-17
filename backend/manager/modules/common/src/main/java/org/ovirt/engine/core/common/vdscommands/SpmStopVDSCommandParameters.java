package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.compat.Guid;

public class SpmStopVDSCommandParameters extends GetStorageConnectionsListVDSCommandParameters {
    public SpmStopVDSCommandParameters(Guid vdsId, Guid storagePoolId) {
        super(vdsId, storagePoolId);
    }

    public SpmStopVDSCommandParameters() {
    }
}

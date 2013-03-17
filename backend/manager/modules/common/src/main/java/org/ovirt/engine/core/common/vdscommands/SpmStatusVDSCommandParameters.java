package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.compat.Guid;

public class SpmStatusVDSCommandParameters extends GetStorageConnectionsListVDSCommandParameters {
    public SpmStatusVDSCommandParameters(Guid vdsId, Guid storagePoolId) {
        super(vdsId, storagePoolId);
    }

    public SpmStatusVDSCommandParameters() {
    }
}

package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.compat.*;

public class SpmStatusVDSCommandParameters extends GetStorageConnectionsListVDSCommandParameters {
    public SpmStatusVDSCommandParameters(Guid vdsId, Guid storagePoolId) {
        super(vdsId, storagePoolId);
    }

    public SpmStatusVDSCommandParameters() {
    }
}

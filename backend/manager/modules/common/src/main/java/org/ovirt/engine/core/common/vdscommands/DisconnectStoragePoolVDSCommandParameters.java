package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.compat.*;

public class DisconnectStoragePoolVDSCommandParameters extends ConnectStoragePoolVDSCommandParameters {
    public DisconnectStoragePoolVDSCommandParameters(Guid vdsId, Guid storagePoolId, int vds_spm_id) {
        super(vdsId, storagePoolId, vds_spm_id, Guid.Empty, 0);
    }

    public DisconnectStoragePoolVDSCommandParameters() {
    }
}

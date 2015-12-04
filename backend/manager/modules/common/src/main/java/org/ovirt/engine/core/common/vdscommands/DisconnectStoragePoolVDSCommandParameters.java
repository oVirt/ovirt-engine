package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.compat.Guid;

public class DisconnectStoragePoolVDSCommandParameters extends GetStorageConnectionsListVDSCommandParameters {
    public DisconnectStoragePoolVDSCommandParameters(Guid vdsId, Guid storagePoolId, int vds_spm_id) {
        super(vdsId, storagePoolId);
        setVdsSpmId(vds_spm_id);
    }

    public DisconnectStoragePoolVDSCommandParameters() {
    }

    private int privatevds_spm_id;

    public int getVdsSpmId() {
        return privatevds_spm_id;
    }

    private void setVdsSpmId(int value) {
        privatevds_spm_id = value;
    }

    @Override
    protected ToStringBuilder appendAttributes(ToStringBuilder tsb) {
        return super.appendAttributes(tsb).append("vds_spm_id", getVdsSpmId());
    }
}

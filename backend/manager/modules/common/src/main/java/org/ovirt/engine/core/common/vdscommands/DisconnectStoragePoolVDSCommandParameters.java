package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.compat.Guid;

public class DisconnectStoragePoolVDSCommandParameters extends GetStorageConnectionsListVDSCommandParameters {
    public DisconnectStoragePoolVDSCommandParameters(Guid vdsId, Guid storagePoolId, int vds_spm_id) {
        super(vdsId, storagePoolId);
        setvds_spm_id(vds_spm_id);
    }

    public DisconnectStoragePoolVDSCommandParameters() {
    }

    private int privatevds_spm_id;

    public int getvds_spm_id() {
        return privatevds_spm_id;
    }

    private void setvds_spm_id(int value) {
        privatevds_spm_id = value;
    }

    @Override
    protected ToStringBuilder appendAttributes(ToStringBuilder tsb) {
        return super.appendAttributes(tsb)
                .append("vds_spm_id", getvds_spm_id());
    }
}

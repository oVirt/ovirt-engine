package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.compat.Guid;

public class GetStorageConnectionsListVDSCommandParameters extends VdsIdVDSCommandParametersBase {
    private Guid privateStoragePoolId;

    public Guid getStoragePoolId() {
        return privateStoragePoolId;
    }

    private void setStoragePoolId(Guid value) {
        privateStoragePoolId = value;
    }

    public GetStorageConnectionsListVDSCommandParameters(Guid vdsId, Guid storagePoolId) {
        super(vdsId);
        setStoragePoolId(storagePoolId);
    }

    public GetStorageConnectionsListVDSCommandParameters() {
        privateStoragePoolId = Guid.Empty;
    }

    @Override
    protected ToStringBuilder appendAttributes(ToStringBuilder tsb) {
        return super.appendAttributes(tsb)
                .append("storagePoolId", getStoragePoolId());
    }
}

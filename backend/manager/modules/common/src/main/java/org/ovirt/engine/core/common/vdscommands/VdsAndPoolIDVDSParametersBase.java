package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.compat.Guid;

public class VdsAndPoolIDVDSParametersBase extends VdsIdVDSCommandParametersBase {
    private Guid storagePoolId;

    public VdsAndPoolIDVDSParametersBase(Guid vdsId, Guid storagePoolId) {
        super(vdsId);
        this.storagePoolId = storagePoolId;
    }

    public VdsAndPoolIDVDSParametersBase() {
        storagePoolId = Guid.Empty;
    }

    public Guid getStoragePoolId() {
        return storagePoolId;
    }

    @Override
    protected ToStringBuilder appendAttributes(ToStringBuilder tsb) {
        return super.appendAttributes(tsb)
                .append("storagePoolId", getStoragePoolId());
    }
}

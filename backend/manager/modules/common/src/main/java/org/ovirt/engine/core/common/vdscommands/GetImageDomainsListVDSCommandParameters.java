package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.compat.Guid;

public class GetImageDomainsListVDSCommandParameters extends IrsBaseVDSCommandParameters {
    public GetImageDomainsListVDSCommandParameters(Guid storagePoolId, Guid imageGroupId) {
        super(storagePoolId);
        setImageGroupId(imageGroupId);
    }

    private Guid privateImageGroupId;

    public Guid getImageGroupId() {
        return privateImageGroupId;
    }

    private void setImageGroupId(Guid value) {
        privateImageGroupId = value;
    }

    public GetImageDomainsListVDSCommandParameters() {
        privateImageGroupId = Guid.Empty;
    }

    @Override
    public String toString() {
        return String.format("%s, imageGroupId = %s", super.toString(), getImageGroupId());
    }
}

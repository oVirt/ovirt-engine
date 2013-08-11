package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.compat.Guid;

public class GetImagesListVDSCommandParameters extends IrsBaseVDSCommandParameters {
    public GetImagesListVDSCommandParameters(Guid sdUUID, Guid spUUID) {
        super(spUUID);
        setStorageDomainId(sdUUID);
    }

    private Guid storageDomainId;

    public Guid getStorageDomainId() {
        return storageDomainId;
    }

    private void setStorageDomainId(Guid value) {
        storageDomainId = value;
    }

    public GetImagesListVDSCommandParameters() {
        storageDomainId = Guid.Empty;
    }

    @Override
    public String toString() {
        return String.format("%s, sdUUID = %s", super.toString(), getStorageDomainId());
    }
}

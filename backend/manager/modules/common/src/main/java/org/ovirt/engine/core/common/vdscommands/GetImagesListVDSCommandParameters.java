package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.common.utils.ToStringBuilder;
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
    protected ToStringBuilder appendAttributes(ToStringBuilder tsb) {
        return super.appendAttributes(tsb)
                .append("sdUUID", getStorageDomainId());
    }
}

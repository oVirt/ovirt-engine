package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.compat.Guid;

public class StoragePoolDomainAndGroupIdBaseVDSCommandParameters extends StorageDomainIdParametersBase {
    public StoragePoolDomainAndGroupIdBaseVDSCommandParameters(Guid storagePoolId, Guid storageDomainId,
            Guid imageGroupId) {
        super(storagePoolId);
        setStorageDomainId(storageDomainId);
        setImageGroupId(imageGroupId);
    }

    private Guid privateImageGroupId;

    public Guid getImageGroupId() {
        return privateImageGroupId;
    }

    public void setImageGroupId(Guid value) {
        privateImageGroupId = value;
    }

    public StoragePoolDomainAndGroupIdBaseVDSCommandParameters() {
        privateImageGroupId = Guid.Empty;
    }

    @Override
    protected ToStringBuilder appendAttributes(ToStringBuilder tsb) {
        return super.appendAttributes(tsb)
                .append("imageGroupId", getImageGroupId());
    }
}

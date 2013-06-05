package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.compat.Guid;

public class StoragePoolDomainAndGroupIdBaseVDSCommandParameters extends StorageDomainIdParametersBase {
    public StoragePoolDomainAndGroupIdBaseVDSCommandParameters(Guid storagePoolId, Guid storageDomainId,
            Guid imageGroupId) {
        super(storagePoolId);
        setStorageDomainId(storageDomainId);
        setImageGroupId(imageGroupId);
    }

    private Guid privateImageGroupId = Guid.Empty;

    public Guid getImageGroupId() {
        return privateImageGroupId;
    }

    public void setImageGroupId(Guid value) {
        privateImageGroupId = value;
    }

    public StoragePoolDomainAndGroupIdBaseVDSCommandParameters() {
    }

    @Override
    public String toString() {
        return String.format("%s, imageGroupId = %s", super.toString(), getImageGroupId());
    }
}

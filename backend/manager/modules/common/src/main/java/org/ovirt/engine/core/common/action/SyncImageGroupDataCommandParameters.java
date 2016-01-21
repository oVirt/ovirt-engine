package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.Guid;

public class SyncImageGroupDataCommandParameters extends StorageDomainParametersBase {
    private Guid imageGroup;
    private Guid dstStorageDomain;

    public SyncImageGroupDataCommandParameters(Guid storagePoolId, Guid imageGroup, Guid srcStorageDomain,
                                                   Guid dstStorageDomain) {
        super(storagePoolId, srcStorageDomain);
        this.imageGroup = imageGroup;
        this.dstStorageDomain = dstStorageDomain;
    }

    public SyncImageGroupDataCommandParameters() {
        super();
    }

    public Guid getImageGroup() {
        return imageGroup;
    }

    public void setImageGroup(Guid imageGroup) {
        this.imageGroup = imageGroup;
    }

    public Guid getDstStorageDomain() {
        return dstStorageDomain;
    }

    public void setDstStorageDomain(Guid dstStorageDomain) {
        this.dstStorageDomain = dstStorageDomain;
    }
}

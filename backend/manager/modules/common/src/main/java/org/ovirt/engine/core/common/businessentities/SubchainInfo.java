package org.ovirt.engine.core.common.businessentities;

import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.compat.Guid;

public class SubchainInfo {

    private Guid storageDomainId;
    private Guid imageGroupId;
    private Guid baseImageId;
    private Guid topImageId;

    public SubchainInfo() {
    }

    public SubchainInfo(Guid storageDomainId, Guid imageGroupId, Guid baseImageId, Guid topImageId) {
        this.storageDomainId = storageDomainId;
        this.imageGroupId = imageGroupId;
        this.baseImageId = baseImageId;
        this.topImageId = topImageId;
    }

    public Guid getStorageDomainId() {
        return storageDomainId;
    }

    public Guid getImageGroupId() {
        return imageGroupId;
    }

    public Guid getBaseImageId() {
        return baseImageId;
    }

    public Guid getTopImageId() {
        return topImageId;
    }

    @Override
    public String toString() {
        return ToStringBuilder.forInstance(this)
                .append("storageDomainId", storageDomainId)
                .append("imageGroupId", imageGroupId)
                .append("baseImageId", baseImageId)
                .append("topImageId", topImageId)
                .build();
    }
}

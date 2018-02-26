package org.ovirt.engine.core.common.vdscommands;

import java.util.Map;

import org.ovirt.engine.core.common.utils.PDIVMapBuilder;
import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.compat.Guid;

public class MergeVDSCommandParameters extends VdsAndVmIDVDSParametersBase {
    private Guid storagePoolId;
    private Guid storageDomainId;
    private Guid imageGroupId;
    private Guid imageId;
    private Guid baseImageId;
    private Guid topImageId;
    private long bandwidth;

    private MergeVDSCommandParameters() {}

    public MergeVDSCommandParameters(
            Guid vdsId,
            Guid vmId,
            Guid storagePoolId,
            Guid storageDomainId,
            Guid imageGroupId,
            Guid imageId,
            Guid baseImageId,
            Guid topImageId,
            long bandwidth) {
        super(vdsId, vmId);
        this.storagePoolId = storagePoolId;
        this.storageDomainId = storageDomainId;
        this.imageGroupId = imageGroupId;
        this.imageId = imageId;
        this.baseImageId = baseImageId;
        this.topImageId = topImageId;
        this.bandwidth = bandwidth;
    }

    public Guid getStoragePoolId() {
        return storagePoolId;
    }

    public void setStoragePoolId(Guid storagePoolId) {
        this.storagePoolId = storagePoolId;
    }

    public Guid getStorageDomainId() {
        return storageDomainId;
    }

    public void setStorageDomainId(Guid storageDomainId) {
        this.storageDomainId = storageDomainId;
    }

    public Guid getImageGroupId() {
        return imageGroupId;
    }

    public void setImageGroupId(Guid imageGroupId) {
        this.imageGroupId = imageGroupId;
    }

    public Guid getImageId() {
        return imageId;
    }

    public void setImageId(Guid imageId) {
        this.imageId = imageId;
    }

    public Guid getBaseImageId() {
        return baseImageId;
    }

    public void setBaseImageId(Guid baseImageId) {
        this.baseImageId = baseImageId;
    }

    public Guid getTopImageId() {
        return topImageId;
    }

    public void setTopImageId(Guid topImageId) {
        this.topImageId = topImageId;
    }

    public long getBandwidth() {
        return bandwidth;
    }

    public void setBandwidth(long bandwidth) {
        this.bandwidth = bandwidth;
    }

    public Map<String, String> getDriveSpecs() {
        return PDIVMapBuilder.create()
                .setPoolId(getStoragePoolId())
                .setDomainId(getStorageDomainId())
                .setImageGroupId(getImageGroupId())
                .setVolumeId(getImageId()).build();
    }

    @Override
    protected ToStringBuilder appendAttributes(ToStringBuilder tsb) {
        return super.appendAttributes(tsb)
                .append("storagePoolId", storagePoolId)
                .append("storageDomainId", storageDomainId)
                .append("imageGroupId", imageGroupId)
                .append("imageId", imageId)
                .append("baseImageId", baseImageId)
                .append("topImageId", topImageId)
                .append("bandwidth", bandwidth);
    }
}

package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.compat.Guid;

public class AllocateVolumeVDSCommandParameters extends StorageJobVdsCommandParameters {
    private Guid imageGroupId;
    private Guid volumeId;
    private long sizeInBytes;

    public AllocateVolumeVDSCommandParameters(Guid storageDomainId, Guid jobId, Guid imageGroupId, Guid
            volumeId, long sizeInBytes) {
        super(storageDomainId, jobId);
        this.imageGroupId = imageGroupId;
        this.volumeId = volumeId;
        this.sizeInBytes = sizeInBytes;
    }

    public AllocateVolumeVDSCommandParameters() {
    }

    public Guid getImageGroupId() {
        return imageGroupId;
    }

    public void setImageGroupId(Guid imageGroupId) {
        this.imageGroupId = imageGroupId;
    }

    public Guid getVolumeId() {
        return volumeId;
    }

    public void setVolumeId(Guid volumeId) {
        this.volumeId = volumeId;
    }

    public long getSizeInBytes() {
        return sizeInBytes;
    }

    public void setSizeInBytes(long sizeInBytes) {
        this.sizeInBytes = sizeInBytes;
    }

    @Override
    protected ToStringBuilder appendAttributes(ToStringBuilder tsb) {
        return super.appendAttributes(tsb).append("imageGroupId", getJobId())
                .append("volumeId", getVolumeId()).append("sizeInBytes", getSizeInBytes());
    }
}

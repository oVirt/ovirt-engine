package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.compat.Guid;

public class SparsifyImageVDSCommandParameters extends StorageJobVdsCommandParameters {

    private Guid imageId;
    private Guid volumeId;

    public SparsifyImageVDSCommandParameters() {
    }

    public SparsifyImageVDSCommandParameters(Guid jobId, Guid storageId, Guid imageId, Guid volumeId) {
        super(storageId, null, jobId);
        this.imageId = imageId;
        this.volumeId = volumeId;
    }

    public Guid getImageId() {
        return imageId;
    }

    public void setImageId(Guid imageId) {
        this.imageId = imageId;
    }

    public Guid getVolumeId() {
        return volumeId;
    }

    public void setVolumeId(Guid volumeId) {
        this.volumeId = volumeId;
    }

    @Override
    protected ToStringBuilder appendAttributes(ToStringBuilder tsb) {
        return super.appendAttributes(tsb)
                .append("imageId", imageId)
                .append("volumeId", volumeId);
    }

}

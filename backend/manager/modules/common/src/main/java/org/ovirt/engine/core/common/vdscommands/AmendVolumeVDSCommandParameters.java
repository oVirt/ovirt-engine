package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.common.businessentities.storage.QcowCompat;
import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.compat.Guid;

public class AmendVolumeVDSCommandParameters extends StorageJobVdsCommandParameters {

    private Guid imageId;
    private Guid volumeId;
    private QcowCompat qcowCompat;

    public AmendVolumeVDSCommandParameters() {
    }

    public AmendVolumeVDSCommandParameters(Guid jobId,
                                           Guid storageId,
                                           Guid imageId,
                                           Guid volumeId,
                                           QcowCompat qcowCompat) {
        super(storageId, jobId);
        this.imageId = imageId;
        this.volumeId = volumeId;
        this.qcowCompat = qcowCompat;
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

    public QcowCompat getQcowCompat() {
        return qcowCompat;
    }

    public void setQcowCompat(QcowCompat qcowCompat) {
        this.qcowCompat = qcowCompat;
    }

    @Override
    protected ToStringBuilder appendAttributes(ToStringBuilder tsb) {
        return super.appendAttributes(tsb)
                .append("imageId", imageId)
                .append("volumeId", volumeId)
                .append("qcowCompat", qcowCompat);
    }
}

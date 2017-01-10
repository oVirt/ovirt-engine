package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.common.businessentities.storage.QcowCompat;
import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.compat.Guid;

public class AmendVolumeVDSCommandParameters extends StorageJobVdsCommandParameters {

    private Guid imageId;
    private Guid volumeId;
    private QcowCompat qcowCompat;
    private Integer generation;

    public AmendVolumeVDSCommandParameters() {
    }

    public AmendVolumeVDSCommandParameters(Guid jobId,
                                           Guid storageId,
                                           Guid imageId,
                                           Guid volumeId,
                                           Integer generation,
                                           QcowCompat qcowCompat) {
        super(storageId, jobId);
        this.imageId = imageId;
        this.volumeId = volumeId;
        this.generation = generation;
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

    public Integer getGeneration() {
        return generation;
    }

    public void setGeneration(Integer generation) {
        this.generation = generation;
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
                .append("generation", generation)
                .append("qcowCompat", qcowCompat);
    }
}

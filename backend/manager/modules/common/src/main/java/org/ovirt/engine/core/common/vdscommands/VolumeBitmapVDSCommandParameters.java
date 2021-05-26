package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.compat.Guid;

public class VolumeBitmapVDSCommandParameters extends StorageJobVdsCommandParameters {
    private Guid imageGroupId;
    private Guid imageId;
    private Integer generation;
    private String bitmapName;

    public VolumeBitmapVDSCommandParameters() {}

    public VolumeBitmapVDSCommandParameters(Guid storageDomainId, Guid vdsId, Guid jobId, Guid imageGroupId,
                                            Guid imageId, Integer generation, String bitmapName) {
        super(storageDomainId, vdsId, jobId);
        setImageGroupId(imageGroupId);
        setImageId(imageId);
        setGeneration(generation);
        setBitmapName(bitmapName);
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

    public Integer getGeneration() {
        return generation;
    }

    public void setGeneration(Integer generation) {
        this.generation = generation;
    }

    public String getBitmapName() {
        return bitmapName;
    }

    public void setBitmapName(String bitmapName) {
        this.bitmapName = bitmapName;
    }
}

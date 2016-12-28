package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.common.businessentities.VdsmImageLocationInfo;
import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.compat.Guid;

public class UpdateVolumeVDSCommandParameters extends StorageJobVdsCommandParameters {

    private VdsmImageLocationInfo volumeInfo;
    private Boolean legal;
    private String description;
    private Integer generation;
    private Boolean shared;

    public UpdateVolumeVDSCommandParameters(Guid jobId, VdsmImageLocationInfo volumeInfo) {
        super(null, jobId);
        this.volumeInfo = volumeInfo;
    }

    public UpdateVolumeVDSCommandParameters() {
    }

    public VdsmImageLocationInfo getVolumeInfo() {
        return volumeInfo;
    }

    public void setVolumeInfo(VdsmImageLocationInfo volumeInfo) {
        this.volumeInfo = volumeInfo;
    }

    public Boolean getLegal() {
        return legal;
    }

    public void setLegal(Boolean legal) {
        this.legal = legal;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getGeneration() {
        return generation;
    }

    public void setGeneration(Integer generation) {
        this.generation = generation;
    }

    public Boolean getShared() {
        return shared;
    }

    public void setShared(Boolean shared) {
        this.shared = shared;
    }

    @Override
    protected ToStringBuilder appendAttributes(ToStringBuilder tsb) {
        return super.appendAttributes(tsb).append("volumeInfo", getVolumeInfo())
                .append("legal", getLegal())
                .append("description", getDescription())
                .append("generation", getGeneration())
                .append("shared", getShared());
    }
}

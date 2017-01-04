package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.common.businessentities.VdsmImageLocationInfo;
import org.ovirt.engine.core.compat.Guid;

public class UpdateVolumeCommandParameters extends StorageJobCommandParameters {

    private static final long serialVersionUID = -2680417060781930533L;

    private VdsmImageLocationInfo volInfo;
    private Boolean legal;
    private String description;
    private Integer generation;
    private Boolean shared;

    public UpdateVolumeCommandParameters() {
    }

    public UpdateVolumeCommandParameters(
            Guid storagePoolId,
            VdsmImageLocationInfo volInfo,
            Boolean legal,
            String description,
            Integer generation,
            Boolean shared) {
        setStoragePoolId(storagePoolId);
        this.volInfo = volInfo;
        this.legal = legal;
        this.description = description;
        this.generation = generation;
        this.shared = shared;
    }

    public VdsmImageLocationInfo getVolInfo() {
        return volInfo;
    }

    public void setVolInfo(VdsmImageLocationInfo volInfo) {
        this.volInfo = volInfo;
    }

    public Boolean getLegal() {
        return legal;
    }

    public void setLegal(Boolean legal) {
        this.legal = legal;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
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

}

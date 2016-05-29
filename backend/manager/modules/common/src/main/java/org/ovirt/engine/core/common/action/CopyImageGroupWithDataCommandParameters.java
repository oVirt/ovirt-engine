package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.common.businessentities.storage.VolumeFormat;
import org.ovirt.engine.core.compat.Guid;

public class CopyImageGroupWithDataCommandParameters extends ImagesActionsParametersBase {
    private Guid destDomain;
    private Guid srcDomain;
    private boolean collapse;
    private Guid destImageGroupId;
    private VolumeFormat destinationFormat;
    private CopyStage stage = CopyStage.DEST_CREATION;

    public CopyImageGroupWithDataCommandParameters() {
    }

    public CopyImageGroupWithDataCommandParameters(Guid storagePoolId, Guid srcDomain, Guid destDomain, Guid
            imageGroupId, Guid imageId, Guid destImageGroupId, Guid destImageId, VolumeFormat destinationFormat,
                                                   boolean collapse) {
        this.destDomain = destDomain;
        this.srcDomain = srcDomain;
        this.collapse = collapse;
        this.destImageGroupId = destImageGroupId;
        this.destinationFormat = destinationFormat;
        setStoragePoolId(storagePoolId);
        setImageGroupID(imageGroupId);
        setImageId(imageId);
        setDestinationImageId(destImageId);
    }

    public Guid getDestDomain() {
        return destDomain;
    }

    public void setDestDomain(Guid destDomain) {
        this.destDomain = destDomain;
    }

    public Guid getSrcDomain() {
        return srcDomain;
    }

    public void setSrcDomain(Guid srcDomain) {
        this.srcDomain = srcDomain;
    }

    public boolean isCollapse() {
        return collapse;
    }

    public void setCollapse(boolean collapse) {
        this.collapse = collapse;
    }

    public Guid getDestImageGroupId() {
        return destImageGroupId;
    }

    public void setDestImageGroupId(Guid destImageGroupId) {
        this.destImageGroupId = destImageGroupId;
    }

    public VolumeFormat getDestinationFormat() {
        return destinationFormat;
    }

    public void setDestinationFormat(VolumeFormat destinationFormat) {
        this.destinationFormat = destinationFormat;
    }

    public CopyStage getStage() {
        return stage;
    }

    public void setStage(CopyStage stage) {
        this.stage = stage;
    }

    public enum CopyStage {
        DEST_CREATION, DATA_COPY
    }
}

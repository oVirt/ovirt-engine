package org.ovirt.engine.core.common.action;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.VolumeFormat;
import org.ovirt.engine.core.common.businessentities.storage.VolumeType;
import org.ovirt.engine.core.compat.Guid;

public class CopyImageGroupWithDataCommandParameters extends ImagesActionsParametersBase {
    private Guid destDomain;
    private Guid srcDomain;
    private boolean collapse;
    private Guid destImageGroupId;
    private List<DiskImage> destImages = new ArrayList<>();
    private VolumeFormat destinationFormat;
    private VolumeType destinationVolumeType;
    private CopyStage stage = CopyStage.DEST_CREATION;

    // MBS
    private String sourcePath;
    private String targetPath;
    private Guid leaseStorageId;

    public CopyImageGroupWithDataCommandParameters() {
    }

    public CopyImageGroupWithDataCommandParameters(
            Guid storagePoolId,
            Guid srcDomain,
            Guid destDomain,
            Guid imageGroupId,
            Guid imageId,
            Guid destImageGroupId,
            Guid destImageId,
            VolumeFormat destinationFormat,
            VolumeType destinationType,
            boolean collapse) {
        this.destDomain = destDomain;
        this.srcDomain = srcDomain;
        this.collapse = collapse;
        this.destImageGroupId = destImageGroupId;
        this.destinationFormat = destinationFormat;
        this.destinationVolumeType = destinationType;
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

    public VolumeType getDestinationVolumeType() {
        return destinationVolumeType;
    }

    public void setDestinationVolumeType(VolumeType destinationVolumeType) {
        this.destinationVolumeType = destinationVolumeType;
    }

    public CopyStage getStage() {
        return stage;
    }

    public void setStage(CopyStage stage) {
        this.stage = stage;
    }

    public List<DiskImage> getDestImages() {
        return destImages;
    }

    public void setDestImages(List<DiskImage> destImages) {
        this.destImages = destImages;
    }

    public String getSourcePath() {
        return sourcePath;
    }

    public void setSourcePath(String sourcePath) {
        this.sourcePath = sourcePath;
    }

    public String getTargetPath() {
        return targetPath;
    }

    public void setTargetPath(String targetPath) {
        this.targetPath = targetPath;
    }

    public Guid getLeaseStorageId() {
        return leaseStorageId;
    }

    public void setLeaseStorageId(Guid leaseStorageId) {
        this.leaseStorageId = leaseStorageId;
    }

    public enum CopyStage {
        DEST_CREATION, DATA_COPY, UPDATE_VOLUME,
        // MBS
        LEASE_CREATION, DETACH_VOLUME
    }
}

package org.ovirt.engine.core.common.action;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.storage.CopyVolumeType;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.ImageDbOperationScope;
import org.ovirt.engine.core.common.businessentities.storage.ImageOperation;
import org.ovirt.engine.core.common.businessentities.storage.VolumeFormat;
import org.ovirt.engine.core.common.businessentities.storage.VolumeType;
import org.ovirt.engine.core.compat.Guid;

public class MoveOrCopyImageGroupParameters extends ImagesContainterParametersBase {
    private static final long serialVersionUID = -5874446297123213719L;
    private ImageOperation operation;
    private boolean useCopyCollapse;
    private VolumeFormat volumeFormat;
    private VolumeType volumeType;
    private CopyVolumeType copyVolumeType;
    private boolean addImageDomainMapping;
    private boolean forceOverride;
    private Guid sourceDomainId;
    private Guid destImageGroupId;
    private ImageDbOperationScope revertDbOperationScope;
    private boolean shouldLockImageOnRevert;
    private String newAlias;
    private List<DiskImage> destImages = new ArrayList<>();


    public MoveOrCopyImageGroupParameters() {
        operation = ImageOperation.Unassigned;
        volumeFormat = VolumeFormat.UNUSED0;
        volumeType = VolumeType.Unassigned;
        copyVolumeType = CopyVolumeType.SharedVol;
    }

    public MoveOrCopyImageGroupParameters(Guid imageId,
            Guid sourceDomainId,
            Guid destDomainId,
            ImageOperation operation) {
        super(imageId);
        setSourceDomainId(sourceDomainId);
        setStorageDomainId(destDomainId);
        setOperation(operation);
        volumeFormat = VolumeFormat.UNUSED0;
        volumeType = VolumeType.Unassigned;
        copyVolumeType = CopyVolumeType.SharedVol;
    }

    public MoveOrCopyImageGroupParameters(Guid containerId, Guid imageGroupId, Guid leafSnapshotID,
            Guid storageDomainId, ImageOperation operation) {
        super(leafSnapshotID, containerId);
        setStorageDomainId(storageDomainId);
        setImageGroupID(imageGroupId);
        setOperation(operation);
        setUseCopyCollapse(false);
        setVolumeFormat(VolumeFormat.Unassigned);
        setVolumeType(VolumeType.Unassigned);
        setForceOverride(false);
        setDestinationImageId(leafSnapshotID);
        setDestImageGroupId(imageGroupId);
        copyVolumeType = CopyVolumeType.SharedVol;
        setShouldLockImageOnRevert(true);
    }

    public MoveOrCopyImageGroupParameters(Guid containerId,
            Guid imageGroupId,
            Guid imageId,
            Guid destImageGroupId,
            Guid destImageId,
            Guid storageDomainId, ImageOperation operation) {
        this(containerId, imageGroupId, imageId, storageDomainId, operation);
        setDestImageGroupId(destImageGroupId);
        setDestinationImageId(destImageId);
    }

    public MoveOrCopyImageGroupParameters(MoveOrCopyImageGroupParameters other) {
        super(other);
        this.operation = other.operation;
        this.useCopyCollapse = other.useCopyCollapse;
        this.volumeFormat = other.volumeFormat;
        this.volumeType = other.volumeType;
        this.copyVolumeType = other.copyVolumeType;
        this.addImageDomainMapping = other.addImageDomainMapping;
        this.forceOverride = other.forceOverride;
        this.sourceDomainId = other.sourceDomainId;
        this.destImageGroupId = other.destImageGroupId;
        this.revertDbOperationScope = other.revertDbOperationScope;
        this.shouldLockImageOnRevert = other.shouldLockImageOnRevert;
        this.newAlias = other.newAlias;
    }

    public Guid getDestImageGroupId() {
        return destImageGroupId;
    }

    public void setDestImageGroupId(Guid destImageGroupId) {
        this.destImageGroupId = destImageGroupId;
    }

    public ImageOperation getOperation() {
        return operation;
    }

    public void setOperation(ImageOperation value) {
        operation = value;
    }

    public boolean getUseCopyCollapse() {
        return useCopyCollapse;
    }

    public void setUseCopyCollapse(boolean value) {
        useCopyCollapse = value;
    }

    public VolumeFormat getVolumeFormat() {
        return volumeFormat;
    }

    public void setVolumeFormat(VolumeFormat value) {
        volumeFormat = value;
    }

    public VolumeType getVolumeType() {
        return volumeType;
    }

    public void setVolumeType(VolumeType value) {
        volumeType = value;
    }

    public CopyVolumeType getCopyVolumeType() {
        return copyVolumeType;
    }

    public void setCopyVolumeType(CopyVolumeType value) {
        copyVolumeType = value;
    }

    public boolean getAddImageDomainMapping() {
        return addImageDomainMapping;
    }

    public void setAddImageDomainMapping(boolean value) {
        addImageDomainMapping = value;
    }

    public boolean getForceOverride() {
        return forceOverride;
    }

    public void setForceOverride(boolean value) {
        forceOverride = value;
    }

    public Guid getSourceDomainId() {
        return sourceDomainId;
    }

    public void setSourceDomainId(Guid value) {
        sourceDomainId = value;
    }

    public ImageDbOperationScope getRevertDbOperationScope() {
        return revertDbOperationScope;
    }

    public void setRevertDbOperationScope(ImageDbOperationScope revertDbOperationScope) {
        this.revertDbOperationScope = revertDbOperationScope;
    }

    public boolean isShouldLockImageOnRevert() {
        return shouldLockImageOnRevert;
    }

    public void setShouldLockImageOnRevert(boolean shouldLockImageOnRevert) {
        this.shouldLockImageOnRevert = shouldLockImageOnRevert;
    }

    public String getNewAlias() {
        return newAlias;
    }

    public void setNewAlias(String newAlias) {
        this.newAlias = newAlias;
    }

    public List<DiskImage> getDestImages() {
        return destImages;
    }

    public void setDestImages(List<DiskImage> destImages) {
        this.destImages = destImages;
    }
}

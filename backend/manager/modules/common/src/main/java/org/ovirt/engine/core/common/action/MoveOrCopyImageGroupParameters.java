package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.common.businessentities.CopyVolumeType;
import org.ovirt.engine.core.common.businessentities.ImageOperation;
import org.ovirt.engine.core.common.businessentities.VolumeFormat;
import org.ovirt.engine.core.common.businessentities.VolumeType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NGuid;

public class MoveOrCopyImageGroupParameters extends ImagesContainterParametersBase {
    private static final long serialVersionUID = -5874446297123213719L;
    private ImageOperation operation = ImageOperation.Unassigned;
    private boolean useCopyCollapse;
    private VolumeFormat volumeFormat = VolumeFormat.UNUSED0;
    private VolumeType volumeType = VolumeType.Unassigned;
    private CopyVolumeType copyVolumeType = CopyVolumeType.SharedVol;
    private boolean addImageDomainMapping;
    private boolean postZero;
    private boolean forceOverride;
    private NGuid sourceDomainId;
    private Guid destImageGroupId;

    public MoveOrCopyImageGroupParameters() {
    }

    public MoveOrCopyImageGroupParameters(Guid imageId,
            NGuid sourceDomainId,
            Guid destDomainId,
            ImageOperation operation) {
        super(imageId);
        setSourceDomainId(sourceDomainId);
        setStorageDomainId(destDomainId);
        setOperation(operation);
    }

    public MoveOrCopyImageGroupParameters(Guid containerId, Guid imageGroupId, Guid leafSnapshotID,
                                          Guid storageDomainId, ImageOperation operation) {
        super(leafSnapshotID, "", containerId);
        setStorageDomainId(storageDomainId);
        setImageGroupID(imageGroupId);
        setOperation(operation);
        setUseCopyCollapse(false);
        setVolumeFormat(VolumeFormat.Unassigned);
        setVolumeType(VolumeType.Unassigned);
        setPostZero(false);
        setForceOverride(false);
        setDestinationImageId(leafSnapshotID);
        setDestImageGroupId(imageGroupId);
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

    public Guid getDestImageGroupId() {
        return destImageGroupId;
    }

    public void setDestImageGroupId(Guid destImageGroupId) {
        this.destImageGroupId = destImageGroupId;
    }

    public ImageOperation getOperation() {
        return operation;
    }

    private void setOperation(ImageOperation value) {
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

    public boolean getPostZero() {
        return postZero;
    }

    public void setPostZero(boolean value) {
        postZero = value;
    }

    public boolean getForceOverride() {
        return forceOverride;
    }

    public void setForceOverride(boolean value) {
        forceOverride = value;
    }

    public NGuid getSourceDomainId() {
        return sourceDomainId;
    }

    public void setSourceDomainId(NGuid value) {
        sourceDomainId = value;
    }
}

package org.ovirt.engine.core.common.action;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.compat.Guid;

public class CloneImageGroupVolumesStructureCommandParameters extends ImagesActionsParametersBase {
    private List<Guid> imageIds;
    private Guid destDomain;
    private Guid srcDomain;
    private Guid destImageGroupId;
    private Guid destImageId;
    private List<DiskImage> destImages = new ArrayList<>();

    public CloneImageGroupVolumesStructureCommandParameters() {
    }

    public CloneImageGroupVolumesStructureCommandParameters(Guid storagePoolId,
                                                            Guid srcDomain,
                                                            Guid destDomain,
                                                            Guid srcImageGroupId,
                                                            ActionType parentType,
                                                            ActionParametersBase parentParams) {
        this.destDomain = destDomain;
        this.srcDomain = srcDomain;
        setStoragePoolId(storagePoolId);
        setImageGroupID(srcImageGroupId);
        setParentCommand(parentType);
        setParentParameters(parentParams);
    }

    public List<Guid> getImageIds() {
        return imageIds;
    }

    public void setImageIds(List<Guid> imageIds) {
        this.imageIds = imageIds;
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

    public Guid getDestImageGroupId() {
        return destImageGroupId;
    }

    public void setDestImageGroupId(Guid destImageGroupId) {
        this.destImageGroupId = destImageGroupId;
    }

    public Guid getDestImageId() {
        return destImageId;
    }

    public void setDestImageId(Guid destImageId) {
        this.destImageId = destImageId;
    }

    public List<DiskImage> getDestImages() {
        return destImages;
    }

    public void setDestImages(List<DiskImage> destImages) {
        this.destImages = destImages;
    }
}

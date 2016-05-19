package org.ovirt.engine.core.common.action;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.storage.VolumeFormat;
import org.ovirt.engine.core.compat.Guid;

public class CloneImageGroupVolumesStructureCommandParameters extends ImagesActionsParametersBase {
    private List<Guid> imageIds;
    private Guid destDomain;
    private Guid srcDomain;
    private VolumeFormat destFormat;

    public CloneImageGroupVolumesStructureCommandParameters() {
    }

    public CloneImageGroupVolumesStructureCommandParameters(Guid storagePoolId,
                                                            Guid srcDomain,
                                                            Guid destDomain,
                                                            Guid srcImageGroupId,
                                                            VolumeFormat destFormat,
                                                            VdcActionType parentType,
                                                            VdcActionParametersBase parentParams) {
        this.destDomain = destDomain;
        this.srcDomain = srcDomain;
        this.destFormat = destFormat;
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

    public VolumeFormat getDestFormat() {
        return destFormat;
    }

    public void setDestFormat(VolumeFormat destFormat) {
        this.destFormat = destFormat;
    }
}

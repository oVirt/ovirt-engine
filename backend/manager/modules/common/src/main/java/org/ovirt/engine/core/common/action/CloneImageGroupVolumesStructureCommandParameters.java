package org.ovirt.engine.core.common.action;

import java.util.List;

import org.ovirt.engine.core.compat.Guid;

public class CloneImageGroupVolumesStructureCommandParameters extends ImagesActionsParametersBase {
    private List<Guid> imageIds;
    private Guid destDomain;
    private Guid srcDomain;

    public CloneImageGroupVolumesStructureCommandParameters() {
    }

    public CloneImageGroupVolumesStructureCommandParameters(Guid storagePoolId,
                                                            Guid srcDomain,
                                                            Guid destDomain,
                                                            Guid srcImageGroupId,
                                                            VdcActionType parentType,
                                                            VdcActionParametersBase parentParams) {
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
}

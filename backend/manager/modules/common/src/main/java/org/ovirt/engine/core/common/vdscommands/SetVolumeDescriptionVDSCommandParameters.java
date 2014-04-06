package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.compat.Guid;

public class SetVolumeDescriptionVDSCommandParameters extends AllStorageAndImageIdVDSCommandParametersBase{
    private String description;

    public SetVolumeDescriptionVDSCommandParameters(Guid storagePoolId, Guid storageDomainId, Guid imageGroupId, Guid imageId, String description) {
        super(storagePoolId, storageDomainId, imageGroupId, imageId);
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public SetVolumeDescriptionVDSCommandParameters() {
    }

    public void setDescription(String description) {
        this.description = description;
    }
}

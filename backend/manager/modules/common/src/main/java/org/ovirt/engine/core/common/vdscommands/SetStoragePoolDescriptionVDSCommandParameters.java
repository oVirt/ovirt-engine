package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.compat.Guid;

public class SetStoragePoolDescriptionVDSCommandParameters extends IrsBaseVDSCommandParameters {
    private String privateDescription;

    public String getDescription() {
        return privateDescription;
    }

    private void setDescription(String value) {
        privateDescription = value;
    }

    public SetStoragePoolDescriptionVDSCommandParameters(Guid storagePoolId, String description) {
        super(storagePoolId);
        setDescription(description);
    }

    public SetStoragePoolDescriptionVDSCommandParameters() {
    }

    @Override
    public String toString() {
        return String.format("%s, description = %s", super.toString(), getDescription());
    }
}

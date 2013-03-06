package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.compat.Guid;

public class SetStorageDomainDescriptionVDSCommandParameters extends ActivateStorageDomainVDSCommandParameters {
    private String privateDescription;

    public String getDescription() {
        return privateDescription;
    }

    private void setDescription(String value) {
        privateDescription = value;
    }

    public SetStorageDomainDescriptionVDSCommandParameters(Guid storagePoolId, Guid storageDomainId, String description) {
        super(storagePoolId, storageDomainId);
        setDescription(description);
    }

    public SetStorageDomainDescriptionVDSCommandParameters() {
    }

    @Override
    public String toString() {
        return String.format("%s, description = %s", super.toString(), getDescription());
    }
}

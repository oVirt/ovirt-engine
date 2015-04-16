package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.common.utils.ToStringBuilder;
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
    protected ToStringBuilder appendAttributes(ToStringBuilder tsb) {
        return super.appendAttributes(tsb)
                .append("description", getDescription());
    }
}

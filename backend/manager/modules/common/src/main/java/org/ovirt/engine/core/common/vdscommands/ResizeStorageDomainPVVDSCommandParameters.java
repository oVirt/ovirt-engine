package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.compat.Guid;

public class ResizeStorageDomainPVVDSCommandParameters extends StorageDomainIdParametersBase {

    private String device;

    public ResizeStorageDomainPVVDSCommandParameters(Guid storagePoolId, Guid storageDomainId,
                                                     String device) {
        super(storagePoolId);
        setStorageDomainId(storageDomainId);
        this.device = device;
    }

    public ResizeStorageDomainPVVDSCommandParameters() {
    }

    public String getDevice() {
        return device;
    }

    @Override
    public String toString() {
        return String.format("%s, device = %s", super.toString(), getDevice());
    }
}

package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.common.utils.ToStringBuilder;
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
    protected ToStringBuilder appendAttributes(ToStringBuilder tsb) {
        return super.appendAttributes(tsb).append("device", getDevice());
    }
}

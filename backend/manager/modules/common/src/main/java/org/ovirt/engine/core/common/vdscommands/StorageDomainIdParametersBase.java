package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.compat.Guid;

/**
 * Base parameters class adding a storage domain id field.
 */
public class StorageDomainIdParametersBase extends IrsBaseVDSCommandParameters {

    private Guid privateStorageDomainId;

    protected StorageDomainIdParametersBase(Guid storagePoolId) {
        super(storagePoolId);
        privateStorageDomainId = Guid.Empty;
    }

    protected StorageDomainIdParametersBase() {
        super();
        privateStorageDomainId = Guid.Empty;
    }

    public Guid getStorageDomainId() {
        return privateStorageDomainId;
    }

    public void setStorageDomainId(Guid value) {
        privateStorageDomainId = value;
    }

    @Override
    protected ToStringBuilder appendAttributes(ToStringBuilder tsb) {
        return super.appendAttributes(tsb)
                .append("storageDomainId", getStorageDomainId());
    }
}

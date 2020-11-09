package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.compat.Guid;

public class DeactivateStorageDomainVDSCommandParameters extends ActivateStorageDomainVDSCommandParameters {
    private Guid masterStorageDomainId;
    private int masterVersion;

    public DeactivateStorageDomainVDSCommandParameters() {
        masterStorageDomainId = Guid.Empty;
    }

    public DeactivateStorageDomainVDSCommandParameters(Guid storagePoolId, Guid storageDomainId,
            Guid masterStorageDomainId, int masterVersion) {
        super(storagePoolId, storageDomainId);
        setMasterStorageDomainId(masterStorageDomainId);
        setMasterVersion(masterVersion);
    }

    public Guid getMasterStorageDomainId() {
        return masterStorageDomainId;
    }

    private void setMasterStorageDomainId(Guid value) {
        masterStorageDomainId = value;
    }

    public int getMasterVersion() {
        return masterVersion;
    }

    private void setMasterVersion(int value) {
        masterVersion = value;
    }

    @Override
    protected ToStringBuilder appendAttributes(ToStringBuilder tsb) {
        return super.appendAttributes(tsb)
                .append("masterDomainId", getMasterStorageDomainId())
                .append("masterVersion", getMasterVersion());
    }
}

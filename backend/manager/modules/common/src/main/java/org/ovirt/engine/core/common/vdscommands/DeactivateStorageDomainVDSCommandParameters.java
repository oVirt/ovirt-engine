package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.compat.Guid;

public class DeactivateStorageDomainVDSCommandParameters extends ActivateStorageDomainVDSCommandParameters {
    private Guid privateMasterStorageDomainId;

    public Guid getMasterStorageDomainId() {
        return privateMasterStorageDomainId;
    }

    private void setMasterStorageDomainId(Guid value) {
        privateMasterStorageDomainId = value;
    }

    private int privateMasterVersion;

    public int getMasterVersion() {
        return privateMasterVersion;
    }

    private void setMasterVersion(int value) {
        privateMasterVersion = value;
    }

    public DeactivateStorageDomainVDSCommandParameters(Guid storagePoolId, Guid storageDomainId,
            Guid masterStorageDomainId, int masterVersion) {
        super(storagePoolId, storageDomainId);
        setMasterStorageDomainId(masterStorageDomainId);
        setMasterVersion(masterVersion);
    }

    public DeactivateStorageDomainVDSCommandParameters() {
        privateMasterStorageDomainId = Guid.Empty;
    }

    @Override
    protected ToStringBuilder appendAttributes(ToStringBuilder tsb) {
        return super.appendAttributes(tsb)
                .append("masterDomainId", getMasterStorageDomainId())
                .append("masterVersion", getMasterVersion());
    }
}

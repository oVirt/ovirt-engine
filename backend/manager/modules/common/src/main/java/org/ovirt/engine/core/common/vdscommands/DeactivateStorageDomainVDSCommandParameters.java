package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.compat.Guid;

public class DeactivateStorageDomainVDSCommandParameters extends ActivateStorageDomainVDSCommandParameters {
    private Guid privateMasterStorageDomainId = new Guid();

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
    }

    @Override
    public String toString() {
        return String.format("%s, masterDomainId = %s, masterVersion = %s", super.toString(),
                getMasterStorageDomainId(), getMasterVersion());
    }
}

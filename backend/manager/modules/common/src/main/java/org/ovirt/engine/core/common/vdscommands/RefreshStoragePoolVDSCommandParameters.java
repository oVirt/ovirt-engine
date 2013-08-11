package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.compat.Guid;

public class RefreshStoragePoolVDSCommandParameters extends GetStorageConnectionsListVDSCommandParameters {
    public RefreshStoragePoolVDSCommandParameters(Guid vdsId, Guid storagePoolId, Guid masterStorageDomainId,
            int masterVersion) {
        super(vdsId, storagePoolId);
        setMasterStorageDomainId(masterStorageDomainId);
        setMasterVersion(masterVersion);
    }

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

    public RefreshStoragePoolVDSCommandParameters() {
        privateMasterStorageDomainId = Guid.Empty;
    }

    @Override
    public String toString() {
        return String.format("%s, masterStorageDomainId=%s, masterVersion=%s",
                super.toString(),
                getMasterStorageDomainId(),
                getMasterVersion());
    }
}

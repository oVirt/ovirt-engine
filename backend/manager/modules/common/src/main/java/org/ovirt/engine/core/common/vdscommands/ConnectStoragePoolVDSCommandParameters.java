package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.compat.Guid;

public class ConnectStoragePoolVDSCommandParameters extends DisconnectStoragePoolVDSCommandParameters {
    public ConnectStoragePoolVDSCommandParameters(Guid vdsId, Guid storagePoolId, int vds_spm_id, Guid masterDomainId,
            int masterVersion) {
        super(vdsId, storagePoolId, vds_spm_id);
        setMasterDomainId(masterDomainId);
        setMasterVersion(masterVersion);
    }

    private int privateMasterVersion;

    public int getMasterVersion() {
        return privateMasterVersion;
    }

    private void setMasterVersion(int value) {
        privateMasterVersion = value;
    }

    public ConnectStoragePoolVDSCommandParameters() {
    }

    private Guid privateMasterDomainId = Guid.Empty;

    public Guid getMasterDomainId() {
        return privateMasterDomainId;
    }

    private void setMasterDomainId(Guid value) {
        privateMasterDomainId = value;
    }

    @Override
    public String toString() {
        return String.format("%s, masterDomainId = %s, masterVersion = %s",
                super.toString(),
                getMasterDomainId(),
                getMasterVersion());
    }
}

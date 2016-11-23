package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.common.businessentities.SubchainInfo;
import org.ovirt.engine.core.compat.Guid;

public class ColdMergeCommandParameters extends StorageJobCommandParameters {

    private static final long serialVersionUID = -6337018038220230095L;
    private SubchainInfo subchainInfo;

    public ColdMergeCommandParameters() {
    }

    public ColdMergeCommandParameters(Guid storagePoolId, SubchainInfo subchainInfo) {
        setStoragePoolId(storagePoolId);
        this.subchainInfo = subchainInfo;
    }

    public SubchainInfo getSubchainInfo() {
        return subchainInfo;
    }
}

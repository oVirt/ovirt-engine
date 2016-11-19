package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.common.businessentities.SubchainInfo;
import org.ovirt.engine.core.compat.Guid;

public class ColdMergeVDSCommandParameters extends StorageJobVdsCommandParameters {

    private SubchainInfo subchainInfo;

    public ColdMergeVDSCommandParameters() {
    }

    public ColdMergeVDSCommandParameters(Guid storageJobId, SubchainInfo subchainInfo) {
        super(null, storageJobId);
        this.subchainInfo = subchainInfo;
    }

    public SubchainInfo getSubchainInfo() {
        return subchainInfo;
    }
}

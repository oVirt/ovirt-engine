package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.common.businessentities.SubchainInfo;
import org.ovirt.engine.core.compat.Guid;

public class SPMColdMergeVDSCommandParameters extends IrsBaseVDSCommandParameters {

    private SubchainInfo subchainInfo;

    public SPMColdMergeVDSCommandParameters() {
    }

    public SPMColdMergeVDSCommandParameters(Guid storagePoolId, SubchainInfo subchainInfo) {
        super(storagePoolId);
        this.subchainInfo = subchainInfo;
    }

    public SubchainInfo getSubchainInfo() {
        return subchainInfo;
    }
}

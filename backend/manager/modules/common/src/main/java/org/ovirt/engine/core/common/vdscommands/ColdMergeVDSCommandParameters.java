package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.common.businessentities.SubchainInfo;
import org.ovirt.engine.core.compat.Guid;

public class ColdMergeVDSCommandParameters extends StorageJobVdsCommandParameters {

    private SubchainInfo subchainInfo;
    private boolean mergeBitmaps;

    public ColdMergeVDSCommandParameters() {
    }

    public ColdMergeVDSCommandParameters(Guid storageJobId, SubchainInfo subchainInfo, boolean mergeBitmaps) {
        super(null, storageJobId);
        this.subchainInfo = subchainInfo;
        this.mergeBitmaps = mergeBitmaps;
    }

    public SubchainInfo getSubchainInfo() {
        return subchainInfo;
    }

    public boolean isMergeBitmaps() {
        return mergeBitmaps;
    }
}

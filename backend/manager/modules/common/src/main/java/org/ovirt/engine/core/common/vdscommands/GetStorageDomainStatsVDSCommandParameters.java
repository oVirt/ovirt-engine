package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.compat.*;

public class GetStorageDomainStatsVDSCommandParameters extends ValidateStorageDomainVDSCommandParameters {
    public GetStorageDomainStatsVDSCommandParameters(Guid vdsId, Guid storageDomainId) {
        super(vdsId, storageDomainId);
    }

    public GetStorageDomainStatsVDSCommandParameters() {
    }
}

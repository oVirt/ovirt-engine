package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.compat.Guid;

public class FormatStorageDomainVDSCommandParameters extends ValidateStorageDomainVDSCommandParameters {
    public FormatStorageDomainVDSCommandParameters(Guid vdsId, Guid storageDomainId) {
        super(vdsId, storageDomainId);
    }

    public FormatStorageDomainVDSCommandParameters() {
    }
}

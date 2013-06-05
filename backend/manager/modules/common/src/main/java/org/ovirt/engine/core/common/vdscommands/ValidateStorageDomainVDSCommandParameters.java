package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.compat.Guid;

public class ValidateStorageDomainVDSCommandParameters extends VdsIdVDSCommandParametersBase {
    private Guid privateStorageDomainId = Guid.Empty;

    public Guid getStorageDomainId() {
        return privateStorageDomainId;
    }

    private void setStorageDomainId(Guid value) {
        privateStorageDomainId = value;
    }

    public ValidateStorageDomainVDSCommandParameters(Guid vdsId, Guid storageDomainId) {
        super(vdsId);
        setStorageDomainId(storageDomainId);
    }

    public ValidateStorageDomainVDSCommandParameters() {
    }

    @Override
    public String toString() {
        return String.format("%s, storageDomainId=%s", super.toString(), getStorageDomainId());
    }
}

package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.compat.Guid;

public class ValidateStorageDomainVDSCommandParameters extends VdsIdVDSCommandParametersBase {
    private Guid privateStorageDomainId;

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
        privateStorageDomainId = Guid.Empty;
    }

    @Override
    protected ToStringBuilder appendAttributes(ToStringBuilder tsb) {
        return super.appendAttributes(tsb)
                .append("storageDomainId", getStorageDomainId());
    }
}

package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.compat.Guid;

public class StorageDomainVdsCommandParameters extends VdsIdVDSCommandParametersBase {
    Guid storageDomainId;

    public Guid getStorageDomainId() {
        return storageDomainId;
    }

    public void setStorageDomainId(Guid storageDomainId) {
        this.storageDomainId = storageDomainId;
    }

    public StorageDomainVdsCommandParameters(Guid storageDomainId) {
        super(null);
        this.storageDomainId = storageDomainId;
    }

    public StorageDomainVdsCommandParameters(Guid storageDomainId, Guid vdsId) {
        super(vdsId);
        this.storageDomainId = storageDomainId;
    }

    public StorageDomainVdsCommandParameters() {
    }

    @Override
    public String toString() {
        return String.format("%s, storageDomainId=%s", super.toString(), getStorageDomainId());
    }
}

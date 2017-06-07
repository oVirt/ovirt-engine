package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.compat.Guid;

public class LeaseVDSParameters extends VdsAndVmIDVDSParametersBase {
    private Guid storageDomainId;

    public LeaseVDSParameters() {
    }

    public LeaseVDSParameters(Guid vdsId, Guid vmId, Guid storageDomainId) {
        super(vdsId, vmId);
        setStorageDomainId(storageDomainId);
    }

    public Guid getStorageDomainId() {
        return storageDomainId;
    }

    public void setStorageDomainId(Guid storageDomainId) {
        this.storageDomainId = storageDomainId;
    }
}

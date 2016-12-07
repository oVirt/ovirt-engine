package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.compat.Guid;

public class VmLeaseVDSParameters extends IrsBaseVDSCommandParameters {

    private Guid storageDomainId;
    private Guid leaseId;

    public VmLeaseVDSParameters(Guid storagePoolId, Guid storageDomainId, Guid leaseId) {
        super(storagePoolId);
        this.storageDomainId = storageDomainId;
        this.leaseId = leaseId;
    }

    public VmLeaseVDSParameters() {
    }

    public Guid getStorageDomainId() {
        return storageDomainId;
    }

    public void setStorageDomainId(Guid storageDomainId) {
        this.storageDomainId = storageDomainId;
    }

    public Guid getLeaseId() {
        return leaseId;
    }

    public void setLeaseId(Guid leaseId) {
        this.leaseId = leaseId;
    }

    @Override
    protected ToStringBuilder appendAttributes(ToStringBuilder tsb) {
        return super.appendAttributes(tsb)
                .append("leaseId", getLeaseId())
                .append("storageDomainId", getStorageDomainId());
    }

}

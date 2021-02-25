package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.compat.Guid;

public class GetLeaseStatusVDSCommandParameters extends VdsIdVDSCommandParametersBase {
    private Guid storagePoolId;
    private Guid storageDomainId;
    private Guid leaseId;

    public GetLeaseStatusVDSCommandParameters() {
    }

    public GetLeaseStatusVDSCommandParameters(Guid vdsId, Guid storagePoolId, Guid storageDomainId, Guid leaseId) {
        super(vdsId);
        this.storagePoolId = storagePoolId;
        this.storageDomainId = storageDomainId;
        this.leaseId = leaseId;
    }

    public GetLeaseStatusVDSCommandParameters(Guid storagePoolId, Guid storageDomainId, Guid leaseId) {
        this(null, storagePoolId, storageDomainId, leaseId);
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
    public ToStringBuilder appendAttributes(ToStringBuilder tsb) {
        return super.appendAttributes(tsb)
                .append("storagePoolId", storagePoolId)
                .append("storageDomainId", storageDomainId)
                .append("leaseId", leaseId);
    }
}

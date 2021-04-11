package org.ovirt.engine.core.common.vdscommands;

import java.util.Map;

import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.compat.Guid;

public class AddExternalLeaseVDSParameters extends IrsBaseVDSCommandParameters {
    private Guid storageDomainId;
    private Guid leaseId;
    private Map<String, Object> metadata;

    public AddExternalLeaseVDSParameters(Guid storagePoolId,
            Guid storageDomainId,
            Guid leaseId,
            Map<String, Object> metadata) {
        super(storagePoolId);
        this.storageDomainId = storageDomainId;
        this.leaseId = leaseId;
        this.metadata = metadata;
    }

    public AddExternalLeaseVDSParameters() {
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

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    @Override
    protected ToStringBuilder appendAttributes(ToStringBuilder tsb) {
        return super.appendAttributes(tsb)
                .append("leaseId", getLeaseId())
                .append("storageDomainId", getStorageDomainId())
                .append("metadata", getMetadata());
    }
}

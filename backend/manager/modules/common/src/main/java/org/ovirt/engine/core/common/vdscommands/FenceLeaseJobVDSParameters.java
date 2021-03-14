package org.ovirt.engine.core.common.vdscommands;

import java.util.Map;

import org.ovirt.engine.core.compat.Guid;

public class FenceLeaseJobVDSParameters extends StorageJobVdsCommandParameters {
    private Guid storagePoolId;
    private Guid leaseId;
    private Map<String, Object> metadata;

    public FenceLeaseJobVDSParameters() {
    }

    public FenceLeaseJobVDSParameters(Guid storageDomainId,
            Guid jobId,
            Guid storagePoolId,
            Guid leaseId,
            Map<String, Object> metadata) {
        super(storageDomainId, jobId);
        this.storagePoolId = storagePoolId;
        this.leaseId = leaseId;
        this.metadata = metadata;
    }

    public Guid getStoragePoolId() {
        return storagePoolId;
    }

    public void setStoragePoolId(Guid storagePoolId) {
        this.storagePoolId = storagePoolId;
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
}

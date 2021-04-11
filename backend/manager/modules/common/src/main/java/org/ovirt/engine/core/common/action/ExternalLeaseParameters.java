package org.ovirt.engine.core.common.action;

import java.io.Serializable;
import java.util.Map;

import org.ovirt.engine.core.compat.Guid;

public class ExternalLeaseParameters extends ActionParametersBase implements Serializable {
    private static final long serialVersionUID = -5337054806997094897L;
    private Guid storagePoolId;
    private Guid storageDomainId;
    private Guid leaseId;
    private Map<String, Object> leaseMetadata;

    public ExternalLeaseParameters() {
    }

    public ExternalLeaseParameters(Guid storagePoolId,
            Guid storageDomainId,
            Guid leaseId,
            Map<String, Object> leaseMetadata) {
        this.storagePoolId = storagePoolId;
        this.storageDomainId = storageDomainId;
        this.leaseId = leaseId;
        this.leaseMetadata = leaseMetadata;
    }

    public Guid getStoragePoolId() {
        return storagePoolId;
    }

    public void setStoragePoolId(Guid storagePoolId) {
        this.storagePoolId = storagePoolId;
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

    public Map<String, Object> getLeaseMetadata() {
        return leaseMetadata;
    }

    public void setLeaseMetadata(Map<String, Object> leaseMetadata) {
        this.leaseMetadata = leaseMetadata;
    }
}

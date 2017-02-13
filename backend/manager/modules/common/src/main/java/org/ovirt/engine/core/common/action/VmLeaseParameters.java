package org.ovirt.engine.core.common.action;

import java.io.Serializable;

import org.ovirt.engine.core.compat.Guid;

public class VmLeaseParameters  extends VdcActionParametersBase implements Serializable {

    private Guid storagePoolId;
    private Guid storageDomainId;
    private Guid vmId;

    public VmLeaseParameters() {}

    public VmLeaseParameters(Guid storagePoolId, Guid storageDomainId, Guid vmId) {
        this.storagePoolId = storagePoolId;
        this.storageDomainId = storageDomainId;
        this.vmId = vmId;
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

    public Guid getVmId() {
        return vmId;
    }

    public void setVmId(Guid vmId) {
        this.vmId = vmId;
    }
}

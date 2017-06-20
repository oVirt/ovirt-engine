package org.ovirt.engine.core.common.action;

import java.io.Serializable;

import org.ovirt.engine.core.compat.Guid;

public class VmLeaseParameters  extends ActionParametersBase implements Serializable {

    private Guid storagePoolId;
    private Guid storageDomainId;
    private Guid vmId;
    private Guid vdsId;
    private boolean hotPlugLease;

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

    public Guid getVdsId() {
        return vdsId;
    }

    public void setVdsId(Guid vdsId) {
        this.vdsId = vdsId;
    }

    public boolean isHotPlugLease() {
        return hotPlugLease;
    }

    public void setHotPlugLease(boolean hotPlugLease) {
        this.hotPlugLease = hotPlugLease;
    }
}

package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.Guid;

public class GetAllAttachableDisks extends VdcQueryParametersBase {

    private static final long serialVersionUID = 155490543085422118L;

    private Guid storagePoolId;
    private Guid vmId;

    public GetAllAttachableDisks() {
    }

    public GetAllAttachableDisks(Guid storagePoolId) {
        this.setStoragePoolId(storagePoolId);
    }

    public void setStoragePoolId(Guid storagePoolId) {
        this.storagePoolId = storagePoolId;
    }

    public Guid getStoragePoolId() {
        return storagePoolId;
    }

    public Guid getVmId() {
        return vmId;
    }

    public void setVmId(Guid vmId) {
        this.vmId = vmId;
    }

}

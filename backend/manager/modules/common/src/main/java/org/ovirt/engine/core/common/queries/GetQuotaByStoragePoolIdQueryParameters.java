package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.Guid;

public class GetQuotaByStoragePoolIdQueryParameters extends VdcQueryParametersBase {

    /**
     * Generated serial Id.
     */
    private static final long serialVersionUID = 766005677528376770L;
    private Guid storagePoolId;

    public GetQuotaByStoragePoolIdQueryParameters() {}

    public GetQuotaByStoragePoolIdQueryParameters(Guid storagePoolId) {
        this.storagePoolId = storagePoolId;
    }

    public void setStoragePoolId(Guid storagePoolId) {
        this.storagePoolId = storagePoolId;
    }

    public Guid getStoragePoolId() {
        return storagePoolId;
    }
}

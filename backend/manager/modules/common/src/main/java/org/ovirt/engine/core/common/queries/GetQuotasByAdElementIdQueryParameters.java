package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.Guid;

public class GetQuotasByAdElementIdQueryParameters extends QueryParametersBase {

    /**
     * Auto generated serial id.
     */
    private static final long serialVersionUID = 4072642442090555682L;
    private Guid adElementId;
    private Guid storagePoolId;

    public void setAdElementId(Guid adElementId) {
        this.adElementId = adElementId;
    }

    public Guid getAdElementId() {
        return adElementId;
    }

    public Guid getStoragePoolId() {
        return storagePoolId;
    }

    public void setStoragePoolId(Guid storagePoolId) {
        this.storagePoolId = storagePoolId;
    }
}

package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.Guid;

public class GetQuotasConsumptionForCurrentUserQueryParameters extends VdcQueryParametersBase {

    /**
     * Auto generated serial id.
     */
    private static final long serialVersionUID = 4072642442090555682L;
    private Guid storagePoolId;

    public Guid getStoragePoolId() {
        return storagePoolId;
    }

    public void setStoragePoolId(Guid storagePoolId) {
        this.storagePoolId = storagePoolId;
    }
}

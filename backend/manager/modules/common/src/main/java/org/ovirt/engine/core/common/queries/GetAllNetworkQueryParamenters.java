package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.*;

public class GetAllNetworkQueryParamenters extends VdcQueryParametersBase {
    private static final long serialVersionUID = 272929658978296731L;

    public GetAllNetworkQueryParamenters(Guid storagePoolId) {
        _storagePoolId = storagePoolId;
    }

    private Guid _storagePoolId = new Guid();

    public Guid getStoragePoolId() {
        return _storagePoolId;
    }

    public GetAllNetworkQueryParamenters() {
    }
}

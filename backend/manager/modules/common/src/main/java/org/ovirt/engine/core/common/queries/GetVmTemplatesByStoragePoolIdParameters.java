package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.Guid;

public class GetVmTemplatesByStoragePoolIdParameters extends VdcQueryParametersBase {
    private static final long serialVersionUID = -3999600118511670633L;

    public GetVmTemplatesByStoragePoolIdParameters(Guid storagePoolId) {
        _storagePoolId = storagePoolId;
    }

    private Guid _storagePoolId = new Guid();

    public Guid getStoragePoolId() {
        return _storagePoolId;
    }

    public GetVmTemplatesByStoragePoolIdParameters() {
    }
}

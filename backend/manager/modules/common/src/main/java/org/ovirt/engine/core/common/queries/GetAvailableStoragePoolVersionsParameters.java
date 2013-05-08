package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.NGuid;

public class GetAvailableStoragePoolVersionsParameters extends VdcQueryParametersBase {
    private static final long serialVersionUID = -4173355602880264922L;
    private NGuid privateStoragePoolId;

    public NGuid getStoragePoolId() {
        return privateStoragePoolId;
    }

    public void setStoragePoolId(NGuid value) {
        privateStoragePoolId = value;
    }

    public GetAvailableStoragePoolVersionsParameters() {
    }
}

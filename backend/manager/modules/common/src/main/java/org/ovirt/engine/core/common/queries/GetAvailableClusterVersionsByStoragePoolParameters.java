package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NGuid;

public class GetAvailableClusterVersionsByStoragePoolParameters extends VdcQueryParametersBase {
    private static final long serialVersionUID = 6803980112895387178L;

    public GetAvailableClusterVersionsByStoragePoolParameters() {
    }

    public GetAvailableClusterVersionsByStoragePoolParameters(Guid storagePoolId) {
        setStoragePoolId(storagePoolId);
    }

    private NGuid privateStoragePoolId;

    public NGuid getStoragePoolId() {
        return privateStoragePoolId;
    }

    public void setStoragePoolId(NGuid value) {
        privateStoragePoolId = value;
    }

}

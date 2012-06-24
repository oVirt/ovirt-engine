package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.*;

public class ReconstructMasterParameters extends StorageDomainPoolParametersBase {
    private static final long serialVersionUID = -640521915810322901L;

    public ReconstructMasterParameters(Guid storagePoolId, Guid storageDomainId, boolean isDeactivate) {
        super(storageDomainId, storagePoolId);
        setDeactivate(isDeactivate);
    }

    public ReconstructMasterParameters() {
    }
}

package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.*;

public class RecoveryStoragePoolParameters extends ReconstructMasterParameters {
    private static final long serialVersionUID = -1967845549935626938L;

    public RecoveryStoragePoolParameters(Guid storagePoolId, Guid newMasterDomainId) {
        super(storagePoolId, newMasterDomainId);
    }

    public RecoveryStoragePoolParameters() {
    }
}

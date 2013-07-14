package org.ovirt.engine.core.vdsbroker.irsbroker;

import org.ovirt.engine.core.common.vdscommands.IrsBaseVDSCommandParameters;
import org.ovirt.engine.core.compat.Guid;

public class SpmStopOnIrsVDSCommandParameters extends IrsBaseVDSCommandParameters {

    private Guid preferredSPMId;

    public SpmStopOnIrsVDSCommandParameters(Guid storagePoolId) {
        super(storagePoolId);
    }

    public SpmStopOnIrsVDSCommandParameters(Guid storagePoolId, Guid preferredSPMId) {
        super(storagePoolId);
        setPreferredSPMId(preferredSPMId);
    }

    public Guid getPreferredSPMId() {
        return preferredSPMId;
    }

    public void setPreferredSPMId(Guid preferredSPMId) {
        this.preferredSPMId = preferredSPMId;
    }
}

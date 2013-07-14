package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.Guid;

public class ForceSelectSPMParameters extends VdcActionParametersBase {

    private static final long serialVersionUID = -755083459086386402L;

    private Guid preferredSPMId;
    private Guid storagePoolId = Guid.Empty;

    public ForceSelectSPMParameters() {
    }

    public ForceSelectSPMParameters(Guid storagePoolId, Guid prefferedSPMId) {
        setStoragePoolId(storagePoolId);
        setPreferredSPMId(prefferedSPMId);
    }

    public Guid getPreferredSPMId() {
        return preferredSPMId;
    }

    public void setPreferredSPMId(Guid preferredSPMId) {
        this.preferredSPMId = preferredSPMId;
    }

    public Guid getStoragePoolId() {
        return storagePoolId;
    }

    public void setStoragePoolId(Guid storagePoolId) {
        this.storagePoolId = storagePoolId;
    }
}

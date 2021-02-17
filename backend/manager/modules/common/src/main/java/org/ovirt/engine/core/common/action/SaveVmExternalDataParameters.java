package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.Guid;

public class SaveVmExternalDataParameters extends VmOperationParameterBase {

    private static final long serialVersionUID = 1L;

    private ExternalDataStatus externalDataStatus;
    private boolean forceUpdate;

    public SaveVmExternalDataParameters() {
    }

    public SaveVmExternalDataParameters(Guid vmId, ExternalDataStatus externalDataStatus, boolean forceUpdate) {
        super(vmId);
        this.externalDataStatus = externalDataStatus;
        this.forceUpdate = forceUpdate;
    }

    public ExternalDataStatus getExternalDataStatus() {
        return externalDataStatus;
    }

    public void setExternalDataStatus(ExternalDataStatus externalDataStatus) {
        this.externalDataStatus = externalDataStatus;
    }

    public boolean getForceUpdate() {
        return forceUpdate;
    }

    public void setForceUpdate(boolean forceUpdate) {
        this.forceUpdate = forceUpdate;
    }
}

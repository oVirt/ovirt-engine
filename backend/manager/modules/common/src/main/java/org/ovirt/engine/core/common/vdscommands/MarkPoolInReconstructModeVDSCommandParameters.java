package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.compat.Guid;

public class MarkPoolInReconstructModeVDSCommandParameters extends IrsBaseVDSCommandParameters {

    private ReconstructMarkAction reconstructMarkAction;

    public MarkPoolInReconstructModeVDSCommandParameters(Guid storagePoolId, ReconstructMarkAction reconstructMarkAction) {
        super(storagePoolId);
        this.reconstructMarkAction = reconstructMarkAction;
    }

    // Setters and Getters

    public ReconstructMarkAction getReconstructMarkAction() {
        return reconstructMarkAction;
    }

    public void setReconstructMarkAction(ReconstructMarkAction reconstructMarkAction) {
        this.reconstructMarkAction = reconstructMarkAction;
    }

    @Override
    public String toString() {
        return String.format("%s, reconstructMarkAction = %s", super.toString(), getReconstructMarkAction());
    }
}

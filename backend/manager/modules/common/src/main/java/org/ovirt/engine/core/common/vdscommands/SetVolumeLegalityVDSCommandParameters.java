package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.compat.Guid;

public class SetVolumeLegalityVDSCommandParameters extends AllStorageAndImageIdVDSCommandParametersBase {
    private boolean legal;

    public SetVolumeLegalityVDSCommandParameters(Guid spId, Guid sdId, Guid imgGroupId,
            Guid imgId, boolean legal) {
        super(spId, sdId, imgGroupId, imgId);
        setLegality(legal);
    }
    public SetVolumeLegalityVDSCommandParameters() {};

    public void setLegality(boolean legal) {
        this.legal = legal;
    }

    public boolean getLegality() {
        return legal;
    }
}

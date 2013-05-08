package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.Guid;

public class FenceVdsManualyParameters extends StoragePoolParametersBase {
    private static final long serialVersionUID = -5313772300704786422L;
    private boolean privateClearVMs;

    public boolean getClearVMs() {
        return privateClearVMs;
    }

    public void setClearVMs(boolean value) {
        privateClearVMs = value;
    }

    public FenceVdsManualyParameters(boolean clearVMs) {
        super(Guid.Empty);
        setClearVMs(clearVMs);
    }

    public FenceVdsManualyParameters() {
    }
}

package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.Guid;

public class AdElementParametersBase extends VdcActionParametersBase {
    private static final long serialVersionUID = -8078914032408357639L;
    private Guid _adElementId;

    public AdElementParametersBase(Guid adElementId) {
        _adElementId = adElementId;
    }

    public Guid getAdElementId() {
        return _adElementId;
    }

    public AdElementParametersBase() {
        _adElementId = Guid.Empty;
    }
}

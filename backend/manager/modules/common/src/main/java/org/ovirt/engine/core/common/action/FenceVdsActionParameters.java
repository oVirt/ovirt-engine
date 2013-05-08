package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.common.businessentities.FenceActionType;
import org.ovirt.engine.core.compat.Guid;

public class FenceVdsActionParameters extends VdsActionParameters {
    private static final long serialVersionUID = 6174371941176548263L;

    public FenceVdsActionParameters(Guid vdsId, FenceActionType action) {
        super(vdsId);
        _action = action;
    }

    private FenceActionType _action = FenceActionType.forValue(0);

    public FenceActionType getAction() {
        return _action;
    }

    public FenceVdsActionParameters() {
    }
}

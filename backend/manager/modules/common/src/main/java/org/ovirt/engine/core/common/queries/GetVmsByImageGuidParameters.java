package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.Guid;

public class GetVmsByImageGuidParameters extends VdcQueryParametersBase {
    private static final long serialVersionUID = -6977853398143134633L;
    private final Guid imageGuid;

    public GetVmsByImageGuidParameters() {
        this(new Guid());
    }

    public GetVmsByImageGuidParameters(Guid imageGuid) {
        this.imageGuid = imageGuid;
    }

    public Guid getImageGuid() {
        return imageGuid;
    }
}

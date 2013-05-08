package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.Guid;

public class GetVdsGroupByVdsGroupIdParameters extends VdcQueryParametersBase {
    private static final long serialVersionUID = -3812474779897884090L;

    public GetVdsGroupByVdsGroupIdParameters(Guid vdsGroupId) {
        _vdsGroupId = vdsGroupId;
    }

    private Guid _vdsGroupId;

    public Guid getVdsGroupId() {
        return _vdsGroupId;
    }

    public GetVdsGroupByVdsGroupIdParameters() {
    }
}

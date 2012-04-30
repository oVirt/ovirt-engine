package org.ovirt.engine.core.common.queries;

public class GetVdsMessagesParameters extends VdcQueryParametersBase {
    private static final long serialVersionUID = -7522465676706160504L;

    public GetVdsMessagesParameters(int vdsId) {
        _vdsId = vdsId;
    }

    private int _vdsId;

    public int getVdsId() {
        return _vdsId;
    }

    public GetVdsMessagesParameters() {
    }
}

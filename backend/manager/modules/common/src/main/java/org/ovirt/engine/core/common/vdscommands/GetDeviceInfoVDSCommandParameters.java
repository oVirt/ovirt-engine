package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.compat.Guid;

public class GetDeviceInfoVDSCommandParameters extends VdsIdVDSCommandParametersBase {
    public GetDeviceInfoVDSCommandParameters(Guid vdsId, String lunId) {
        super(vdsId);
        setLUNID(lunId);
    }

    private String privateLUNID;

    public String getLUNID() {
        return privateLUNID;
    }

    private void setLUNID(String value) {
        privateLUNID = value;
    }

    public GetDeviceInfoVDSCommandParameters() {
    }

    @Override
    public String toString() {
        return String.format("%s, LUNID=%s", super.toString(), getLUNID());
    }

}

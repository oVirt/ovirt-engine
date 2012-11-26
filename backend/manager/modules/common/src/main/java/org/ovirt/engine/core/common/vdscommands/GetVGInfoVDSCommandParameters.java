package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.compat.Guid;

public class GetVGInfoVDSCommandParameters extends VdsIdVDSCommandParametersBase {
    private String privateVGID;

    public GetVGInfoVDSCommandParameters() {
    }

    public GetVGInfoVDSCommandParameters(Guid vdsId, String vgId) {
        super(vdsId);
        setVGID(vgId);
    }

    public String getVGID() {
        return privateVGID;
    }

    private void setVGID(String value) {
        privateVGID = value;
    }

    @Override
    public String toString() {
        return String.format("%s, VGID=%s", super.toString(), getVGID());
    }
}

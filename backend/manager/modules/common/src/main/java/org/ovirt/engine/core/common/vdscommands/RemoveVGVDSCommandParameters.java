package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.compat.Guid;

@Deprecated
public class RemoveVGVDSCommandParameters extends VdsIdVDSCommandParametersBase {
    public RemoveVGVDSCommandParameters(Guid vdsId, String vgId) {
        super(vdsId);
        setVGID(vgId);
    }

    private String privateVGID;

    public String getVGID() {
        return privateVGID;
    }

    private void setVGID(String value) {
        privateVGID = value;
    }

    public RemoveVGVDSCommandParameters() {
    }

    @Override
    public String toString() {
        return String.format("%s, VGID=%s", super.toString(), getVGID());
    }
}

package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.common.utils.ToStringBuilder;
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
    protected ToStringBuilder appendAttributes(ToStringBuilder tsb) {
        return super.appendAttributes(tsb)
                .append("VGID", getVGID());
    }
}

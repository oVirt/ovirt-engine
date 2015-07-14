package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.compat.Guid;

public class GetOvaInfoParameters extends VdsIdVDSCommandParametersBase {

    private String ovaPath;

    public GetOvaInfoParameters() {
    }

    public GetOvaInfoParameters(Guid vdsId, String ovaPath) {
        super(vdsId);
        this.ovaPath = ovaPath;
    }

    public String getPath() {
        return ovaPath;
    }

    public void setPath(String ovaPath) {
        this.ovaPath = ovaPath;
    }

    @Override
    protected ToStringBuilder appendAttributes(ToStringBuilder tsb) {
        return super.appendAttributes(tsb)
                .append("path", ovaPath);
    }
}

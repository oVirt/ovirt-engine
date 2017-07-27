package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.compat.Guid;

public class GetLldpVDSCommandParameters extends VdsIdVDSCommandParametersBase {
    private String[] interfaces;

    public GetLldpVDSCommandParameters() { }

    public GetLldpVDSCommandParameters(Guid vdsId, String[] interfaces) {
        super(vdsId);
        this.interfaces = interfaces;
    }

    public String[] getInterfaces() {
        return interfaces;
    }
}

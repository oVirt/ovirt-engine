package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.Guid;

public class CopyHostNetworksParameters extends VdsActionParameters {
    private static final long serialVersionUID = 6319273943636350323L;

    private Guid sourceHostId;

    public CopyHostNetworksParameters() {

    }

    public CopyHostNetworksParameters(Guid sourceHostId, Guid destinationHostId) {
        setVdsId(destinationHostId);
        this.sourceHostId = sourceHostId;
    }

    public Guid getSourceHostId() {
        return sourceHostId;
    }

    public void setSourceHostId(Guid sourceHostId) {
        this.sourceHostId = sourceHostId;
    }
}

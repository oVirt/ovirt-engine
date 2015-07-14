package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.Guid;

public class GetVmFromOvaQueryParameters extends VdcQueryParametersBase {
    private static final long serialVersionUID = 8902089542141597782L;

    private Guid vdsId;
    private String path;

    public GetVmFromOvaQueryParameters() {
    }

    public GetVmFromOvaQueryParameters(Guid vdsId, String path) {
        this.vdsId = vdsId;
        this.path = path;
    }

    public Guid getVdsId() {
        return vdsId;
    }

    public void setVdsId(Guid vdsId) {
        this.vdsId = vdsId;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}

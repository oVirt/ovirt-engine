package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.Guid;

public class GetVmFromOvaQueryParameters extends QueryParametersBase {
    private static final long serialVersionUID = 8902089542141597782L;

    private Guid vdsId;
    private String path;
    private boolean listDirectory;

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

    public boolean isListDirectory() {
        return listDirectory;
    }

    public void setListDirectory(boolean listDirectory) {
        this.listDirectory = listDirectory;
    }
}

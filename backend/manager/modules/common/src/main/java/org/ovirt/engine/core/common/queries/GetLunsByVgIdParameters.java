package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.Guid;

public class GetLunsByVgIdParameters extends IdQueryParameters {
    private static final long serialVersionUID = 423980044505739585L;

    public GetLunsByVgIdParameters() {
    }

    public GetLunsByVgIdParameters(String vgId) {
        this.vgId = vgId;
    }

    public GetLunsByVgIdParameters(String vgId, Guid vdsId) {
        super(vdsId);
        this.vgId = vgId;
    }

    private String vgId;

    public String getVgId() {
        return vgId;
    }

    public void setVgId(String vgId) {
        this.vgId = vgId;
    }
}

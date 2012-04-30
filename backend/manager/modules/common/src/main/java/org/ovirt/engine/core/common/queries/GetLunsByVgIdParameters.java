package org.ovirt.engine.core.common.queries;

public class GetLunsByVgIdParameters extends VdcQueryParametersBase {
    private static final long serialVersionUID = 423980044505739585L;

    public GetLunsByVgIdParameters() {
    }

    public GetLunsByVgIdParameters(String vgId) {
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

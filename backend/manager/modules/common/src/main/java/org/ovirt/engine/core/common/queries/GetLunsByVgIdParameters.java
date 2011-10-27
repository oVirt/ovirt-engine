package org.ovirt.engine.core.common.queries;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "GetLunsByVgIdParameters")
public class GetLunsByVgIdParameters extends VdcQueryParametersBase {
    private static final long serialVersionUID = 423980044505739585L;

    public GetLunsByVgIdParameters() {
    }

    public GetLunsByVgIdParameters(String vgId) {
        this.vgId = vgId;
    }

    @XmlElement(name = "VgId")
    private String vgId;

    public String getVgId() {
        return vgId;
    }

    public void setVgId(String vgId) {
        this.vgId = vgId;
    }
}

package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "VGQueryParametersBase")
public class VGQueryParametersBase extends VdsIdParametersBase {
    private static final long serialVersionUID = -4612413633577153266L;

    @XmlElement(name = "VGId")
    private String privateVGId;

    public String getVGId() {
        return privateVGId;
    }

    private void setVGId(String value) {
        privateVGId = value;
    }

    public VGQueryParametersBase(String vgID, Guid vdsId) {
        super(vdsId);
        setVGId(vgID);
    }

    @Override
    public RegisterableQueryReturnDataType GetReturnedDataTypeByVdcQueryType(VdcQueryType queryType) {
        return RegisterableQueryReturnDataType.UNDEFINED;
    }

    public VGQueryParametersBase() {
    }
}

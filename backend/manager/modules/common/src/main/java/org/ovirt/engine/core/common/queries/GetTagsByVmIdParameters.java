package org.ovirt.engine.core.common.queries;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "GetTagsByVmIdParameters")
public class GetTagsByVmIdParameters extends VdcQueryParametersBase {
    private static final long serialVersionUID = -8537901288950684062L;

    public GetTagsByVmIdParameters(String vmId) {
        _vmId = vmId;
    }

    @XmlElement(name = "VmId")
    private String _vmId;

    public String getVmId() {
        return _vmId;
    }

    @Override
    public RegisterableQueryReturnDataType GetReturnedDataTypeByVdcQueryType(VdcQueryType queryType) {
        return RegisterableQueryReturnDataType.UNDEFINED;
    }

    public GetTagsByVmIdParameters() {
    }
}

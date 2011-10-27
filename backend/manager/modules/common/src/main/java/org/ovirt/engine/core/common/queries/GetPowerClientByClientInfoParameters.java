package org.ovirt.engine.core.common.queries;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "GetPowerClientByClientInfoParameters")
public class GetPowerClientByClientInfoParameters extends VdcQueryParametersBase {
    private static final long serialVersionUID = 1049519488813110436L;

    public GetPowerClientByClientInfoParameters(String clientIp) {
        _clientIp = clientIp;
    }

    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    @XmlElement(name = "ClientIp")
    private String _clientIp;

    public String getClientIp() {
        return _clientIp;
    }

    @Override
    public RegisterableQueryReturnDataType GetReturnedDataTypeByVdcQueryType(VdcQueryType queryType) {
        return RegisterableQueryReturnDataType.IQUERYABLE;
    }

    public GetPowerClientByClientInfoParameters() {
    }
}

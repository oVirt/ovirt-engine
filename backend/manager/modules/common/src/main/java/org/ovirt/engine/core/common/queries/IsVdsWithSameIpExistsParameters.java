package org.ovirt.engine.core.common.queries;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "IsVdsWithSameIpExistsParameters")
public class IsVdsWithSameIpExistsParameters extends VdcQueryParametersBase {
    private static final long serialVersionUID = 3967554280743440995L;

    public IsVdsWithSameIpExistsParameters(String ipAddress) {
        _ipAddress = ipAddress;
    }

    @XmlElement(name = "IpAddress")
    private String _ipAddress;

    public String getIpAddress() {
        return _ipAddress;
    }

    @Override
    public RegisterableQueryReturnDataType GetReturnedDataTypeByVdcQueryType(VdcQueryType queryType) {
        return RegisterableQueryReturnDataType.UNDEFINED;
    }

    public IsVdsWithSameIpExistsParameters() {
    }
}

package org.ovirt.engine.core.common.queries;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "IsVdsWithSameHostExistParameters")
public class IsVdsWithSameHostExistParameters extends VdcQueryParametersBase {
    private static final long serialVersionUID = 8539975719107135326L;

    public IsVdsWithSameHostExistParameters(String hostName) {
        _hostName = hostName;
    }

    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    @XmlElement(name = "HostName")
    private String _hostName;

    public String getHostName() {
        return _hostName;
    }

    @Override
    public RegisterableQueryReturnDataType GetReturnedDataTypeByVdcQueryType(VdcQueryType queryType) {
        return RegisterableQueryReturnDataType.UNDEFINED;
    }

    public IsVdsWithSameHostExistParameters() {
    }
}

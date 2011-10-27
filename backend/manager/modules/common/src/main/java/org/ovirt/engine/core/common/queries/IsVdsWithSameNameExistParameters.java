package org.ovirt.engine.core.common.queries;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "IsVdsWithSameNameExistParameters")
public class IsVdsWithSameNameExistParameters extends VdcQueryParametersBase {
    private static final long serialVersionUID = 7065542492217271851L;

    public IsVdsWithSameNameExistParameters(String vdsName) {
        _vdsName = vdsName;
    }

    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    @XmlElement(name = "VdsName")
    private String _vdsName;

    public String getVmName() {
        return _vdsName;
    }

    @Override
    public RegisterableQueryReturnDataType GetReturnedDataTypeByVdcQueryType(VdcQueryType queryType) {
        return RegisterableQueryReturnDataType.UNDEFINED;
    }

    public IsVdsWithSameNameExistParameters() {
    }
}

package org.ovirt.engine.core.common.queries;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "IsVmPoolWithSameNameExistsParameters")
public class IsVmPoolWithSameNameExistsParameters extends VdcQueryParametersBase {
    private static final long serialVersionUID = -3533344997052757929L;

    public IsVmPoolWithSameNameExistsParameters(String vmPoolName) {
        _vmPoolName = vmPoolName;
    }

    @XmlElement(name = "VmPoolName")
    private String _vmPoolName;

    public String getVmPoolName() {
        return _vmPoolName;
    }

    @Override
    public RegisterableQueryReturnDataType GetReturnedDataTypeByVdcQueryType(VdcQueryType queryType) {
        return RegisterableQueryReturnDataType.UNDEFINED;
    }

    public IsVmPoolWithSameNameExistsParameters() {
    }
}

package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "GetUserVmByUserIdAndByVmGuidParameters")
public class GetUserVmByUserIdAndByVmGuidParameters extends VdcQueryParametersBase {
    private static final long serialVersionUID = 3575536561865155699L;

    public GetUserVmByUserIdAndByVmGuidParameters(Guid userId, Guid vmId) {
        _userId = userId;
        _vmId = vmId;
    }

    @XmlElement(name = "UserId")
    private Guid _userId = new Guid();
    @XmlElement(name = "VmId")
    private Guid _vmId = new Guid();

    public Guid getUserId() {
        return _userId;
    }

    public Guid getVmId() {
        return _vmId;
    }

    @Override
    public RegisterableQueryReturnDataType GetReturnedDataTypeByVdcQueryType(VdcQueryType queryType) {
        return RegisterableQueryReturnDataType.UNDEFINED;
    }

    public GetUserVmByUserIdAndByVmGuidParameters() {
    }
}

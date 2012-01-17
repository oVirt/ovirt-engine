package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "HasFreeVmsInPoolParameters")
public class HasFreeVmsInPoolParameters extends GetVmPoolByIdParametersBase {
    private static final long serialVersionUID = 3927293237624681243L;

    public HasFreeVmsInPoolParameters(Guid poolId) {
        super(poolId);
    }

    @Override
    public RegisterableQueryReturnDataType GetReturnedDataTypeByVdcQueryType(VdcQueryType queryType) {
        return RegisterableQueryReturnDataType.UNDEFINED;
    }

    public HasFreeVmsInPoolParameters() {
    }
}

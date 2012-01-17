package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "GetVmPoolByIdParameters")
public class GetVmPoolByIdParameters extends GetVmPoolByIdParametersBase {
    private static final long serialVersionUID = 4065751487095904776L;

    public GetVmPoolByIdParameters(Guid poolId) {
        super(poolId);
    }

    @Override
    public RegisterableQueryReturnDataType GetReturnedDataTypeByVdcQueryType(VdcQueryType queryType) {
        return RegisterableQueryReturnDataType.IQUERYABLE;
    }

    public GetVmPoolByIdParameters() {
    }
}

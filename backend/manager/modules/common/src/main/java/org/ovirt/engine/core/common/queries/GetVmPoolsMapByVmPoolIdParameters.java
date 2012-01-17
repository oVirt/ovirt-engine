package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "GetVmPoolsMapByVmPoolIdParameters")
public class GetVmPoolsMapByVmPoolIdParameters extends GetVmPoolByIdParametersBase {
    private static final long serialVersionUID = -2006515844211778283L;

    public GetVmPoolsMapByVmPoolIdParameters(Guid poolId) {
        super(poolId);
    }

    @Override
    public RegisterableQueryReturnDataType GetReturnedDataTypeByVdcQueryType(VdcQueryType queryType) {
        return RegisterableQueryReturnDataType.UNDEFINED;
    }

    public GetVmPoolsMapByVmPoolIdParameters() {
    }
}

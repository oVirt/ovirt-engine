package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.common.businessentities.*;

public class GetVdsByTypeParameters extends VdcQueryParametersBase {
    private static final long serialVersionUID = 9106511793583677642L;

    public GetVdsByTypeParameters(VDSType vdsType) {
        _vdsType = vdsType;
    }

    private VDSType _vdsType = VDSType.forValue(0);

    public VDSType getVdsType() {
        return _vdsType;
    }

    public GetVdsByTypeParameters() {
    }
}

package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "GetVmPoolByIdParametersBase")
public class GetVmPoolByIdParametersBase extends VdcQueryParametersBase {
    private static final long serialVersionUID = -4229590551595438086L;

    public GetVmPoolByIdParametersBase(Guid poolId) {
        _poolId = poolId;
    }

    @XmlElement(name = "PoolId")
    private Guid _poolId;

    public Guid getPoolId() {
        return _poolId;
    }

    public GetVmPoolByIdParametersBase() {
    }
}

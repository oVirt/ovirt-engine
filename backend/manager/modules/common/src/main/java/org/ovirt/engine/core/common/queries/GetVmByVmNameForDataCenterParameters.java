package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.Guid;

public class GetVmByVmNameForDataCenterParameters extends QueryParametersBase {
    private static final long serialVersionUID = -3232978226860645746L;

    private Guid dataCenterId;
    private String name;

    public GetVmByVmNameForDataCenterParameters() {
    }

    public GetVmByVmNameForDataCenterParameters(Guid dataCenterId, String name) {
        this.dataCenterId = dataCenterId;
        this.name = name;
    }

    public Guid getDataCenterId() {
        return dataCenterId;
    }

    public String getName() {
        return name;
    }

}

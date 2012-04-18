package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.Guid;

public class GetHostsByClusterIdParameters extends VdcQueryParametersBase {
    private static final long serialVersionUID = -1208517415183117603L;

    private Guid id = new Guid();

    public GetHostsByClusterIdParameters(Guid clusterId) {
        this.id = clusterId;
    }

    public Guid getId() {
        return id;
    }
}

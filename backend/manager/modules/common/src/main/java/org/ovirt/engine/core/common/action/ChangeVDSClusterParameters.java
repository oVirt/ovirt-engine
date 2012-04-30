package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.Guid;

public class ChangeVDSClusterParameters extends VdsActionParameters {
    private static final long serialVersionUID = -4484499078098460017L;
    private Guid clusterId;

    public ChangeVDSClusterParameters() {
    }

    public ChangeVDSClusterParameters(Guid clusterId, Guid vdsId) {
        super(vdsId);
        this.clusterId = clusterId;
    }

    public Guid getClusterId() {
        return clusterId;
    }

    public void setClusterId(Guid clusterId) {
        this.clusterId = clusterId;
    }

}

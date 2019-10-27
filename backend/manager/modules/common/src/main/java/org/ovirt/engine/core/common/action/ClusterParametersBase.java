package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.Guid;

public class ClusterParametersBase extends ActionParametersBase {
    private static final long serialVersionUID = -9133528679053901135L;
    private Guid clusterId;
    private boolean force;

    public ClusterParametersBase(Guid clusterId) {
        this.clusterId = clusterId;
    }

    public Guid getClusterId() {
        return clusterId;
    }

    public ClusterParametersBase() {
    }

    public boolean isForce() {
        return force;
    }

    public void setForce(boolean force) {
        this.force = force;
    }
}

package org.ovirt.engine.core.common.action.gluster;

import javax.validation.constraints.NotNull;

import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.compat.Guid;

public class GlusterClusterParameters extends ActionParametersBase {

    private static final long serialVersionUID = 2260339638936514331L;

    @NotNull(message = "VALIDATION_GLUSTER_VOLUME_CLUSTER_ID_NOT_NULL")
    private Guid clusterId;

    public GlusterClusterParameters() {
    }

    public GlusterClusterParameters(Guid clusterId) {
        super();
        this.clusterId = clusterId;
    }

    public Guid getClusterId() {
        return clusterId;
    }
}

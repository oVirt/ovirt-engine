package org.ovirt.engine.core.common.action.gluster;

import javax.validation.constraints.NotNull;

import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.compat.Guid;

public class GlusterClusterParameters extends VdcActionParametersBase {

    private static final long serialVersionUID = 2260339638936514331L;

    @NotNull(message = "VALIDATION.GLUSTER.VOLUME.CLUSTER_ID.NOT_NULL")
    private final Guid clusterId;

    public GlusterClusterParameters(Guid clusterId) {
        super();
        this.clusterId = clusterId;
    }

    public Guid getClusterId() {
        return clusterId;
    }



}

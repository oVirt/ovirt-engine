package org.ovirt.engine.core.common.queries.gluster;

import javax.validation.constraints.NotNull;

import org.ovirt.engine.core.common.queries.QueryParametersBase;
import org.ovirt.engine.core.compat.Guid;

/**
 * Parameter class with cluster Id as parameters. <br>
 */
public class GlusterParameters extends QueryParametersBase {
    private static final long serialVersionUID = -1224829720081853632L;

    @NotNull(message = "VALIDATION_GLUSTER_VOLUME_CLUSTER_ID_NOT_NULL")
    private Guid clusterId;

    public GlusterParameters() {
    }

    public GlusterParameters(Guid clusterId) {
        setClusterId(clusterId);
    }

    public Guid getClusterId() {
        return clusterId;
    }

    public void setClusterId(Guid clusterId) {
        this.clusterId = clusterId;
    }


}

package org.ovirt.engine.core.common.action.gluster;

import javax.validation.constraints.NotNull;

import org.ovirt.engine.core.compat.Guid;

/**
 * Parameter class with Gluster cluster id, hook id as parameters. <br>
 * This will be used by enable and disable gluster hook commands. <br>
 */
public class GlusterHookStatusChangeParameters extends GlusterHookParameters {
    private static final long serialVersionUID = 3398376087476446699L;

    @NotNull(message = "VALIDATION.GLUSTER.VOLUME.CLUSTER_ID.NOT_NULL")
    private Guid clusterId;

    @NotNull(message = "VALIDATION.GLUSTER.GLUSTER_HOOK_ID.NOT_NULL")
    private Guid hookId;

    public GlusterHookStatusChangeParameters(Guid clusterId,
            Guid hookId) {
        super(hookId);
        setClusterId(clusterId);
    }

    public void setClusterId(Guid clusterId) {
        this.clusterId = clusterId;
    }

    public Guid getClusterId() {
        return clusterId;
    }

    @Override
    public Guid getHookId() {
        return hookId;
    }

    @Override
    public void setHookId(Guid hookId) {
        this.hookId = hookId;
    }

}

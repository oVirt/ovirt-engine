package org.ovirt.engine.core.common.action.gluster;

import javax.validation.constraints.NotNull;

import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.compat.Guid;

/**
 * Parameter class with Gluster cluster id, hook id as parameters. <br>
 * This will be used by enable and disable gluster hook commands. <br>
 */
public class GlusterHookParameters extends VdcActionParametersBase {
    private static final long serialVersionUID = 3398376087476446699L;

    @NotNull(message = "VALIDATION.GLUSTER.VOLUME.CLUSTER_ID.NOT_NULL")
    private Guid clusterId;

    @NotNull(message = "VALIDATION.GLUSTER.GLUSTER_HOOK_ID.NOT_NULL")
    private Guid hookId;

    public GlusterHookParameters(Guid clusterId,
            Guid hookId) {
        setClusterId(clusterId);
        setHookId(hookId);
    }

    public void setClusterId(Guid clusterId) {
        this.clusterId = clusterId;
    }

    public Guid getClusterId() {
        return clusterId;
    }

    public Guid getHookId() {
        return hookId;
    }

    public void setHookId(Guid hookId) {
        this.hookId = hookId;
    }

}

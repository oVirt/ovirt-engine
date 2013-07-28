package org.ovirt.engine.core.common.action.gluster;

import org.ovirt.engine.core.compat.Guid;

/**
 *
 * This will be used by resolve conflicts related gluster hook commands -
 * update, add and remove. <br>
 */
public class GlusterHookManageParameters extends GlusterHookParameters {
    private static final long serialVersionUID = 3398376087476446699L;

    private Guid sourceServerId;

    public GlusterHookManageParameters() {
    }

    public GlusterHookManageParameters(Guid hookId) {
        super(hookId);
    }

    public GlusterHookManageParameters(Guid hookId,
            Guid sourceServerId) {
        super(hookId);
        this.sourceServerId = sourceServerId;
    }

    public Guid getSourceServerId() {
        return sourceServerId;
    }

    public void setSourceServerId(Guid sourceServerId) {
        this.sourceServerId = sourceServerId;
    }

}

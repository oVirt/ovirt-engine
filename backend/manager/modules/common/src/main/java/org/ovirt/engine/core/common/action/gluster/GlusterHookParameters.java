package org.ovirt.engine.core.common.action.gluster;

import javax.validation.constraints.NotNull;

import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.compat.Guid;

public class GlusterHookParameters extends ActionParametersBase {

    private static final long serialVersionUID = -8236696198344082891L;

    @NotNull(message = "VALIDATION_GLUSTER_GLUSTER_HOOK_ID_NOT_NULL")
    private Guid hookId;

    public GlusterHookParameters() {
    }

    public GlusterHookParameters(Guid hookId) {
        setHookId(hookId);
    }

    public Guid getHookId() {
        return hookId;
    }

    public void setHookId(Guid hookId) {
        this.hookId = hookId;
    }
}

package org.ovirt.engine.core.common.queries.gluster;

import org.ovirt.engine.core.common.queries.QueryParametersBase;
import org.ovirt.engine.core.compat.Guid;

public class GlusterHookQueryParameters extends QueryParametersBase {

    private static final long serialVersionUID = -7687304241216035713L;

    private Guid hookId;

    private boolean includeServerHooks;

    public GlusterHookQueryParameters() {
    }

    public GlusterHookQueryParameters(Guid hookId) {
        this.hookId = hookId;
    }

    public GlusterHookQueryParameters(Guid hookId, boolean includeServerHooks) {
        this(hookId);
        this.includeServerHooks = includeServerHooks;
    }

    public Guid getHookId() {
        return hookId;
    }

    public void setHookId(Guid hookId) {
        this.hookId = hookId;
    }

    public boolean isIncludeServerHooks() {
        return includeServerHooks;
    }

    public void setIncludeServerHooks(boolean includeServerHooks) {
        this.includeServerHooks = includeServerHooks;
    }

}

package org.ovirt.engine.api.restapi.resource.gluster;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.common.util.EnumValidator;
import org.ovirt.engine.api.model.Action;
import org.ovirt.engine.api.model.GlusterHook;
import org.ovirt.engine.api.model.ResolutionType;
import org.ovirt.engine.api.resource.gluster.GlusterHookResource;
import org.ovirt.engine.api.restapi.resource.AbstractBackendActionableResource;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.gluster.GlusterHookManageParameters;
import org.ovirt.engine.core.common.action.gluster.GlusterHookParameters;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterHookEntity;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.common.queries.gluster.GlusterHookContentQueryParameters;
import org.ovirt.engine.core.common.queries.gluster.GlusterHookQueryParameters;
import org.ovirt.engine.core.compat.Guid;

/**
 * Implementation of the "glusterhooks/{id}" resource
 */
public class BackendGlusterHookResource
        extends AbstractBackendActionableResource<GlusterHook, GlusterHookEntity>
        implements GlusterHookResource {
    private BackendGlusterHooksResource parent;

    public BackendGlusterHookResource(String hookId, BackendGlusterHooksResource parent) {
        this(hookId);
        setParent(parent);
    }

    public BackendGlusterHookResource(String hookId) {
        super(hookId, GlusterHook.class, GlusterHookEntity.class);
    }

    @Override
    protected GlusterHook addParents(GlusterHook model) {
        model.setId(id);
        parent.addParents(model);
        return model;
    }

    @Override
    public GlusterHook get() {
        GlusterHook hook = performGet(QueryType.GetGlusterHookById, new GlusterHookQueryParameters(guid, true));
        QueryReturnValue result = runQuery(QueryType.GetGlusterHookContent, new GlusterHookContentQueryParameters(guid));
        if (result != null
                && result.getSucceeded()
                && result.getReturnValue() != null) {
            hook.setContent(result.getReturnValue());
        }
        return hook;
    }

    @Override
    public Response enable(Action action) {
        return doAction(ActionType.EnableGlusterHook, new GlusterHookParameters(guid), action);
    }

    @Override
    public Response disable(Action action) {
        return doAction(ActionType.DisableGlusterHook, new GlusterHookParameters(guid), action);
    }

    @Override
    public Response resolve(Action action) {
        validateParameters(action, "resolutionType");

        ResolutionType resolutionType = EnumValidator.validateEnum(ResolutionType.class, action.getResolutionType(), true);

        switch (resolutionType) {
        case ADD:
            return addToMissingServers(action);
        case COPY:
            return copy(action);
        default:
            return null;
        }
    }

    private Response addToMissingServers(Action action) {
        return doAction(ActionType.AddGlusterHook, new GlusterHookManageParameters(guid), action);
    }

    private Response copy(Action action) {
        GlusterHookManageParameters params = new GlusterHookManageParameters(guid);
        if (action.isSetHost()) {
            validateParameters(action.getHost(), "id|name");
            Guid hostId = getHostId(action);
            params.setSourceServerId(hostId);
        }
        return doAction(ActionType.UpdateGlusterHook, params, action);
    }


    public BackendGlusterHooksResource getParent() {
        return parent;
    }

    public void setParent(BackendGlusterHooksResource parent) {
        this.parent = parent;
    }

    public String getId() {
       return this.id;
    }

    @Override
    public Response remove() {
        get();
        return performAction(ActionType.RemoveGlusterHook, new GlusterHookManageParameters(guid));
    }
}

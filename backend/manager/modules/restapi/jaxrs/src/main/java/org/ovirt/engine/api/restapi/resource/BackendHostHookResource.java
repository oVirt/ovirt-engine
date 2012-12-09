package org.ovirt.engine.api.restapi.resource;

import javax.ws.rs.GET;

import org.ovirt.engine.api.model.Hook;
import org.ovirt.engine.api.model.Hooks;
import org.ovirt.engine.api.resource.HostHookResource;
import org.ovirt.engine.api.restapi.types.HostMapper;

public class BackendHostHookResource extends AbstractBackendSubResource<Hook, Object> implements HostHookResource {

    private BackendHostHooksResource parent;

    public BackendHostHookResource(String id, BackendHostHooksResource parent) {
        super(id, Hook.class, Object.class, new String[0]);
        this.parent = parent;
    }

    @Override
    @GET
    public Hook get() {
        Hooks hooks = parent.list();
        return getHook(hooks);
    }

    public Hook getHook(Hooks hooks) {
        for (Hook hook : hooks.getHooks()) {
            if (HostMapper.generateHookId(hook.getEventName(), hook.getName(), hook.getMd5()).equals(guid)) {
                return hook;
            }
        }
        return notFound();
    }

    public BackendHostHooksResource getParent() {
        return parent;
    }

    public void setParent(BackendHostHooksResource parent) {
        this.parent = parent;
    }

    @Override
    protected Hook doPopulate(Hook model, Object entity) {
        return model;
    }
}

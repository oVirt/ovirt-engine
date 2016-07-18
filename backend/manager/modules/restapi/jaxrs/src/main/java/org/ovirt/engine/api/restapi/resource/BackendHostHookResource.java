package org.ovirt.engine.api.restapi.resource;

import org.ovirt.engine.api.model.Hook;
import org.ovirt.engine.api.model.Hooks;
import org.ovirt.engine.api.resource.HostHookResource;

public class BackendHostHookResource extends AbstractBackendSubResource<Hook, Object> implements HostHookResource {

    private BackendHostHooksResource parent;

    public BackendHostHookResource(String id, BackendHostHooksResource parent) {
        super(id, Hook.class, Object.class);
        this.parent = parent;
    }

    @Override
    public Hook get() {
        Hooks hooks = parent.list();
        return getHook(hooks);
    }

    public Hook getHook(Hooks hooks) {
        String hookId = guid.toString();
        for (Hook hook : hooks.getHooks()) {
            if (hook.getId().equals(hookId)) {
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
}

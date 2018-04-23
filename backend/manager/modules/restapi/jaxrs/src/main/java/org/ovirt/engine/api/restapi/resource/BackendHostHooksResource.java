package org.ovirt.engine.api.restapi.resource;

import java.util.Map;

import org.ovirt.engine.api.model.Hook;
import org.ovirt.engine.api.model.Hooks;
import org.ovirt.engine.api.model.Host;
import org.ovirt.engine.api.resource.HostHookResource;
import org.ovirt.engine.api.resource.HostHooksResource;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;

public class BackendHostHooksResource extends AbstractBackendCollectionResource<Hook, Object> implements HostHooksResource {

    public BackendHostHooksResource(String hostId) {
        super(Hook.class, Object.class);
        this.hostId = hostId;
    }

    private String hostId;

    @Override
    public Hooks list() {
        @SuppressWarnings("unchecked")
        Map<String, Map<String, Map<String, String>>> hooksMap =
                getEntity(Map.class, QueryType.GetVdsHooksById, new IdQueryParameters(asGuid(hostId)), null);
        return mapCollection(hooksMap);
    }

    private Hooks mapCollection(Map<String, Map<String, Map<String, String>>> hooksMap) {
        Hooks hooks = getMapper(Map.class, Hooks.class).map(hooksMap, null);
        for (Hook hook : hooks.getHooks()) {
            addLinks(hook);
        }
        return hooks;
    }

    @Override
    public HostHookResource getHookResource(String id) {
        return inject(new BackendHostHookResource(id, this));
    }

    @Override
    protected Hook addParents(Hook model) {
        Host host = new Host();
        host.setId(hostId);
        model.setHost(host);
        return super.addParents(model);
    }
}

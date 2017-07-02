package org.ovirt.engine.api.restapi.resource.gluster;

import java.util.List;

import org.ovirt.engine.api.model.GlusterHook;
import org.ovirt.engine.api.model.GlusterHooks;
import org.ovirt.engine.api.resource.ClusterResource;
import org.ovirt.engine.api.resource.gluster.GlusterHookResource;
import org.ovirt.engine.api.resource.gluster.GlusterHooksResource;
import org.ovirt.engine.api.restapi.resource.AbstractBackendCollectionResource;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterHookEntity;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.common.queries.gluster.GlusterParameters;

/**
 * Implementation of the "glusterhooks" resource
 */
public class BackendGlusterHooksResource
        extends AbstractBackendCollectionResource<GlusterHook, GlusterHookEntity>
        implements GlusterHooksResource {

    private ClusterResource parent;

    public BackendGlusterHooksResource() {
        super(GlusterHook.class, GlusterHookEntity.class);
    }

    public BackendGlusterHooksResource(ClusterResource parent) {
        this();
        setParent(parent);
    }

    public ClusterResource getParent() {
        return parent;
    }

    public void setParent(ClusterResource parent) {
        this.parent = parent;
    }

    @Override
    public GlusterHooks list() {
        List<GlusterHookEntity> entities = getBackendCollection(QueryType.GetGlusterHooks, new GlusterParameters(asGuid(parent.get().getId())));
        return mapCollection(entities);
    }

    private GlusterHooks mapCollection(List<GlusterHookEntity> entities) {
        GlusterHooks collection = new GlusterHooks();
        for (GlusterHookEntity entity : entities) {
            collection.getGlusterHooks().add(addLinks(populate(map(entity), entity)));
        }
        return collection;
    }

    @Override
    protected GlusterHook addParents(GlusterHook hook) {
        hook.setCluster(parent.get());
        return hook;
    }

    @Override
    public GlusterHookResource getHookResource(String id) {
        return inject(new BackendGlusterHookResource(id, this));
    }
}

package org.ovirt.engine.api.restapi.resource.gluster;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Action;
import org.ovirt.engine.api.model.GlusterBrick;
import org.ovirt.engine.api.resource.ActionResource;
import org.ovirt.engine.api.resource.gluster.GlusterBrickResource;
import org.ovirt.engine.api.restapi.resource.AbstractBackendActionableResource;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.core.compat.NotImplementedException;

public class BackendGlusterBrickResource
        extends AbstractBackendActionableResource<GlusterBrick, GlusterBrickEntity>
        implements GlusterBrickResource {

    public BackendGlusterBrickResource(String brickId) {
        super(brickId, GlusterBrick.class, GlusterBrickEntity.class);
    }

    @Override
    public ActionResource getActionSubresource(String action, String oid) {
        // TODO: To be implemented
        throw new NotImplementedException();
    }

    @Override
    public GlusterBrick get() {
        // TODO: To be implemented
        throw new NotImplementedException();
    }

    @Override
    public Response replace(Action action) {
        // TODO: To be implemented (pending backend functionality)
        throw new NotImplementedException();
    }
}

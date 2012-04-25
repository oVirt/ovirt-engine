package org.ovirt.engine.api.restapi.resource.gluster;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Action;
import org.ovirt.engine.api.model.GlusterVolume;
import org.ovirt.engine.api.resource.ActionResource;
import org.ovirt.engine.api.resource.gluster.GlusterBricksResource;
import org.ovirt.engine.api.resource.gluster.GlusterVolumeResource;
import org.ovirt.engine.api.restapi.resource.AbstractBackendActionableResource;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.compat.NotImplementedException;

/**
 * Implementation of the "glustervolumes/{id}" resource
 */
public class BackendGlusterVolumeResource
        extends AbstractBackendActionableResource<GlusterVolume, GlusterVolumeEntity>
        implements GlusterVolumeResource {

    public BackendGlusterVolumeResource(String volumeId) {
        super(volumeId, GlusterVolume.class, GlusterVolumeEntity.class, BackendGlusterVolumesResource.SUB_COLLECTIONS);
    }

    @Override
    public GlusterVolume get() {
        // TODO: To be implemented
        throw new NotImplementedException();
    }

    @Override
    public ActionResource getActionSubresource(String action, String oid) {
        // TODO: To be implemented (pending backend functionality)
        throw new NotImplementedException();
    }

    @Override
    public Response start(Action action) {
        // TODO: To be implemented
        throw new NotImplementedException();
    }

    @Override
    public Response stop(Action action) {
        // TODO: To be implemented
        throw new NotImplementedException();
    }

    @Override
    public Response rebalance(Action action) {
        // TODO: To be implemented (pending backend functionality)
        throw new NotImplementedException();
    }

    @Override
    public Response setOption(Action action) {
        // TODO: To be implemented
        throw new NotImplementedException();
    }

    @Override
    public Response resetOption(Action action) {
        // TODO: To be implemented
        throw new NotImplementedException();
    }

    @Override
    public Response resetAllOptions(Action action) {
        // TODO: To be implemented
        throw new NotImplementedException();
    }

    @Override
    public GlusterBricksResource getGlusterBrickSubResource() {
        return inject(new BackendGlusterBricksResource(id));
    }
}

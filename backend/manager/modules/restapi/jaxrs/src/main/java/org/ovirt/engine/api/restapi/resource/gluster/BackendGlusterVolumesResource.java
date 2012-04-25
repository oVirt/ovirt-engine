package org.ovirt.engine.api.restapi.resource.gluster;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.GlusterVolume;
import org.ovirt.engine.api.model.GlusterVolumes;
import org.ovirt.engine.api.resource.gluster.GlusterVolumeResource;
import org.ovirt.engine.api.resource.gluster.GlusterVolumesResource;
import org.ovirt.engine.api.restapi.resource.AbstractBackendCollectionResource;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.compat.NotImplementedException;

/**
 * Implementation of the "glustervolumes" resource
 */
public class BackendGlusterVolumesResource
        extends AbstractBackendCollectionResource<GlusterVolume, GlusterVolumeEntity>
        implements GlusterVolumesResource {

    public static final String[] SUB_COLLECTIONS = { "bricks", "options" };
    private final String clusterId;

    public BackendGlusterVolumesResource(String clusterId) {
        super(GlusterVolume.class, GlusterVolumeEntity.class, SUB_COLLECTIONS);
        this.clusterId = clusterId;
    }

    @Override
    public GlusterVolumes list() {
        // TODO: To be implemented
        throw new NotImplementedException();
    }

    @Override
    public Response add(GlusterVolume volume) {
        // TODO: To be implemented
        throw new NotImplementedException();
    }

    @Override
    protected Response performRemove(String id) {
        // TODO: To be implemented
        throw new NotImplementedException();
    }

    @Override
    public GlusterVolumeResource getGlusterVolumeSubResource(String id) {
        return inject(new BackendGlusterVolumeResource(id));
    }
}

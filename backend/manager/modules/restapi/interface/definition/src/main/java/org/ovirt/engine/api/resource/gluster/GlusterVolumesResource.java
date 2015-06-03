package org.ovirt.engine.api.resource.gluster;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.GlusterVolume;
import org.ovirt.engine.api.model.GlusterVolumes;
import org.ovirt.engine.api.resource.ApiMediaType;

/**
 * Resource interface for the "clusters/{cluster_id}/glustervolumes" resource
 */
@Produces({ ApiMediaType.APPLICATION_XML, ApiMediaType.APPLICATION_JSON, ApiMediaType.APPLICATION_X_YAML })
public interface GlusterVolumesResource {
    @GET
    GlusterVolumes list();

    /**
     * Creates a new Gluster Volume and adds it to the database. The Volume is created based on properties of
     * the {@code volume} parameter. The properties {@link GlusterVolume#getName()},
     * {@link GlusterVolume#getVolumeType()} and {@link GlusterVolume#getBricks()} are required.
     *
     * @param volume the Gluster Volume definition from which to create the new Volume
     * @return the new newly created Gluster Volume
     */
    @POST
    @Consumes({ ApiMediaType.APPLICATION_XML, ApiMediaType.APPLICATION_JSON, ApiMediaType.APPLICATION_X_YAML })
    Response add(GlusterVolume volume);

    /**
     * Sub-resource locator method, returns individual GlusterVolumeResource on which the remainder of the URI is
     * dispatched.
     *
     * @param id the volume identifier
     * @return matching subresource if found
     */
    @Path("{id}")
    GlusterVolumeResource getGlusterVolumeSubResource(@PathParam("id") String id);
}

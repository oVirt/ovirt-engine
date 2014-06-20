/**
 *
 */
package org.ovirt.engine.api.resource.gluster;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
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
    public GlusterVolumes list();

    /**
     * Creates a new Gluster Volume and adds it to the database. The Volume is created based on properties of @volume<p>
     *          The properties {@link GlusterVolume#getName()}, {@link GlusterVolume#getVolumeType()} and
     *          {@link GlusterVolume#getGlusterBricks()} are required.
     *
     * @param volume
     *            the Gluster Volume definition from which to create the new Volume
     * @return the new newly created Gluster Volume
     */
    @POST
    @Consumes({ ApiMediaType.APPLICATION_XML, ApiMediaType.APPLICATION_JSON, ApiMediaType.APPLICATION_X_YAML })
    public Response add(GlusterVolume volume);

    /**
     * Removes the given Gluster Volume and deletes it from the database.
     *
     * @param id ID of the volume to be removed
     * @return
     */
    @DELETE
    @Path("{volume_id}")
    public Response remove(@PathParam("volume_id") String id);

    /**
     * Sub-resource locator method, returns individual GlusterVolumeResource on which the remainder of the URI is
     * dispatched.
     *
     * @param name
     *            the GlusterVolume name
     * @return matching subresource if found
     */
    @Path("{volume_id}")
    public GlusterVolumeResource getGlusterVolumeSubResource(@PathParam("volume_id") String id);
}

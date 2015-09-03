package org.ovirt.engine.api.resource.gluster;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Action;
import org.ovirt.engine.api.model.Actionable;
import org.ovirt.engine.api.model.GlusterBricks;
import org.ovirt.engine.api.resource.ActionResource;
import org.ovirt.engine.api.resource.ApiMediaType;

/**
 * Resource interface for the "clusters/{cluster_id}/glustervolumes/{volume_id}/bricks" resource
 */
@Produces({ ApiMediaType.APPLICATION_XML, ApiMediaType.APPLICATION_JSON })
public interface GlusterBricksResource {
    @GET
    GlusterBricks list();

    @Path("{action: (migrate|stopmigrate)}/{oid}")
    ActionResource getActionSubresource(@PathParam("action") String action, @PathParam("oid") String oid);

    /**
     * Adds given list of bricks to the volume, and updates the database accordingly. The properties
     * {@link org.ovirt.engine.api.model.GlusterBrick#getServerId()} and
     * {@link org.ovirt.engine.api.model.GlusterBrick#getBrickDir()} are required.
     *
     * @param bricks
     *            List of bricks to be added to the volume
     */
    @POST
    @Consumes({ ApiMediaType.APPLICATION_XML, ApiMediaType.APPLICATION_JSON })
    Response add(GlusterBricks bricks);

    /**
     * Removes the given brick from the volume and deletes it from the database.
     *
     * @param bricks
     *            List of bricks to be removed
     */
    @DELETE
    Response remove(GlusterBricks bricks);


    @POST
    @Consumes({ApiMediaType.APPLICATION_XML, ApiMediaType.APPLICATION_JSON})
    @Actionable
    @Path("migrate")
    Response migrate(Action action);

    @POST
    @Consumes({ApiMediaType.APPLICATION_XML, ApiMediaType.APPLICATION_JSON})
    @Actionable
    @Path("stopmigrate")
    Response stopMigrate(Action action);

    @POST
    @Consumes({ApiMediaType.APPLICATION_XML, ApiMediaType.APPLICATION_JSON})
    @Actionable
    @Path("activate")
    Response activate(Action action);

    /**
     * Sub-resource locator method, returns individual GlusterBrickResource on which the remainder of the URI is
     * dispatched.
     *
     * @param id the brick id
     * @return matching subresource if found
     */
    @Path("{id}")
    GlusterBrickResource getGlusterBrickSubResource(@PathParam("id") String id);
}

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

import org.ovirt.engine.api.model.Action;
import org.ovirt.engine.api.model.Actionable;
import org.ovirt.engine.api.model.GlusterBricks;
import org.ovirt.engine.api.resource.ActionResource;
import org.ovirt.engine.api.resource.ApiMediaType;
import org.ovirt.engine.api.resource.RsdlIgnore;

/**
 * Resource interface for the "clusters/{cluster_id}/glustervolumes/{volume_id}/bricks" resource
 */
@Produces({ ApiMediaType.APPLICATION_XML, ApiMediaType.APPLICATION_JSON, ApiMediaType.APPLICATION_X_YAML })
public interface GlusterBricksResource {
    @GET
    public GlusterBricks list();

    @Path("{action: (migrate|stopmigrate)}/{oid}")
    public ActionResource getActionSubresource(@PathParam("action") String action, @PathParam("oid") String oid);

    /**
     * Adds given list of bricks to the volume, and updates the database accordingly. The properties
     * {@link org.ovirt.engine.api.model.GlusterBrick#getServerId()} and
     * {@link org.ovirt.engine.api.model.GlusterBrick#getBrickDir()} are required.
     *
     * @param bricks
     *            List of bricks to be added to the volume
     * @return
     */
    @POST
    @Consumes({ ApiMediaType.APPLICATION_XML, ApiMediaType.APPLICATION_JSON, ApiMediaType.APPLICATION_X_YAML })
    public Response add(GlusterBricks bricks);

    /**
     * Removes the given brick from the volume and deletes it from the database.
     *
     * @param bricks
     *            List of bricks to be removed
     * @return
     */
    @DELETE
    @RsdlIgnore //TODO: remove this when we have support for delete at collection level in yaml metadata
    public Response remove(GlusterBricks bricks);

    /**
     * Removes the given brick from the volume and deletes it from the database.
     *
     * @param id
     *            id of the brick to be removed
     * @return
     */
    @DELETE
    @Path("{brick_id}")
    public Response remove(@PathParam("brick_id") String id);

    @POST
    @Consumes({ApiMediaType.APPLICATION_XML, ApiMediaType.APPLICATION_JSON, ApiMediaType.APPLICATION_X_YAML})
    @Actionable
    @Path("migrate")
    public Response migrate(Action action);

    @POST
    @Consumes({ApiMediaType.APPLICATION_XML, ApiMediaType.APPLICATION_JSON, ApiMediaType.APPLICATION_X_YAML})
    @Actionable
    @Path("stopmigrate")
    public Response stopMigrate(Action action);

    @POST
    @Consumes({ApiMediaType.APPLICATION_XML, ApiMediaType.APPLICATION_JSON, ApiMediaType.APPLICATION_X_YAML})
    @Actionable
    @Path("activate")
    public Response activate(Action action);

    /**
     * Sub-resource locator method, returns individual GlusterBrickResource on which the remainder of the URI is
     * dispatched.
     *
     * @param brick_id
     *            the brick id
     * @return matching subresource if found
     */
    @Path("{brick_id}")
    public GlusterBrickResource getGlusterBrickSubResource(@PathParam("brick_id") String id);
}

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
import org.ovirt.engine.api.model.GlusterBrick;
import org.ovirt.engine.api.resource.ActionResource;
import org.ovirt.engine.api.resource.ApiMediaType;
import org.ovirt.engine.api.resource.MeasurableResource;

/**
 * Resource interface for the "clusters/{cluster_id}/glustervolumes/{volume_id}/bricks/{brick_id}" resource
 */
@Produces({ ApiMediaType.APPLICATION_XML, ApiMediaType.APPLICATION_JSON })
public interface GlusterBrickResource extends MeasurableResource{
    @Path("{action: (replace)}/{oid}")
    ActionResource getActionResource(@PathParam("action") String action, @PathParam("oid") String oid);

    @GET
    GlusterBrick get();

    /**
     * Removes this brick from the volume and deletes it from the database.
     */
    @DELETE
    Response remove();

    /**
     * Replaces this brick with a new one. The property {@link Action#getBrick()} is required.
     */
    @Deprecated
    @POST
    @Consumes({ ApiMediaType.APPLICATION_XML, ApiMediaType.APPLICATION_JSON })
    @Actionable
    @Path("replace")
    Response replace(Action action);
}

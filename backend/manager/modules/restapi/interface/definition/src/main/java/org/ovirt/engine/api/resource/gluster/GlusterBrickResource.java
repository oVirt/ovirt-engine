package org.ovirt.engine.api.resource.gluster;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.annotations.providers.jaxb.Formatted;
import org.ovirt.engine.api.model.Action;
import org.ovirt.engine.api.model.Actionable;
import org.ovirt.engine.api.model.GlusterBrick;
import org.ovirt.engine.api.resource.ActionResource;
import org.ovirt.engine.api.resource.MeasurableResource;
import org.ovirt.engine.api.resource.MediaType;

/**
 * Resource interface for the "clusters/{cluster_id}/glustervolumes/{volume_id}/bricks/{brick_id}" resource
 */
@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_X_YAML })
public interface GlusterBrickResource extends MeasurableResource{

    @Path("{action: (replace)}/{oid}")
    public ActionResource getActionSubresource(@PathParam("action") String action, @PathParam("oid") String oid);

    @GET
    @Formatted
    public GlusterBrick get();

    /**
     * Replaces this brick with a new one. The property {@link Action#getNewBrick()} is required.
     *
     * @param action
     * @return
     */
    @POST
    @Formatted
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_X_YAML })
    @Actionable
    @Path("replace")
    public Response replace(Action action);
}

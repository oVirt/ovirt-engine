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
import org.ovirt.engine.api.model.GlusterVolume;
import org.ovirt.engine.api.resource.ActionResource;
import org.ovirt.engine.api.resource.ApiMediaType;

/**
 * Resource interface for the "clusters/{cluster_id}/glustervolumes/{volume_id}" resource
 */
@Produces({ ApiMediaType.APPLICATION_XML, ApiMediaType.APPLICATION_JSON, ApiMediaType.APPLICATION_X_YAML })
public interface GlusterVolumeResource {
    @GET
    @Formatted
    public GlusterVolume get();

    @Path("{action: (start|stop|rebalance|setOption|resetOption|resetAllOptions)}/{oid}")
    public ActionResource getActionSubresource(@PathParam("action") String action, @PathParam("oid") String oid);

    @POST
    @Formatted
    @Consumes({ ApiMediaType.APPLICATION_XML, ApiMediaType.APPLICATION_JSON, ApiMediaType.APPLICATION_X_YAML })
    @Actionable
    @Path("start")
    public Response start(Action action);

    @POST
    @Formatted
    @Consumes({ ApiMediaType.APPLICATION_XML, ApiMediaType.APPLICATION_JSON, ApiMediaType.APPLICATION_X_YAML })
    @Actionable
    @Path("stop")
    public Response stop(Action action);

    @POST
    @Formatted
    @Consumes({ ApiMediaType.APPLICATION_XML, ApiMediaType.APPLICATION_JSON, ApiMediaType.APPLICATION_X_YAML })
    @Actionable
    @Path("rebalance")
    public Response rebalance(Action action);

    @POST
    @Formatted
    @Consumes({ ApiMediaType.APPLICATION_XML, ApiMediaType.APPLICATION_JSON, ApiMediaType.APPLICATION_X_YAML })
    @Actionable
    @Path("setoption")
    public Response setOption(Action action);

    @POST
    @Formatted
    @Consumes({ ApiMediaType.APPLICATION_XML, ApiMediaType.APPLICATION_JSON, ApiMediaType.APPLICATION_X_YAML })
    @Actionable
    @Path("resetoption")
    public Response resetOption(Action action);

    @POST
    @Formatted
    @Consumes({ ApiMediaType.APPLICATION_XML, ApiMediaType.APPLICATION_JSON, ApiMediaType.APPLICATION_X_YAML })
    @Actionable
    @Path("resetalloptions")
    public Response resetAllOptions(Action action);

    /**
     * Sub-resource locator method, returns GlusterBricksResource on which the remainder of the URI is dispatched.
     *
     * @return matching subresource if found
     */
    @Path("bricks")
    public GlusterBricksResource getGlusterBrickSubResource();
}

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
import org.ovirt.engine.api.model.GlusterVolume;
import org.ovirt.engine.api.model.GlusterVolumeProfileDetails;
import org.ovirt.engine.api.resource.ActionResource;
import org.ovirt.engine.api.resource.ApiMediaType;
import org.ovirt.engine.api.resource.MeasurableResource;

/**
 * Resource interface for the "clusters/{cluster_id}/glustervolumes/{volume_id}" resource.
 */
@Produces({ ApiMediaType.APPLICATION_XML, ApiMediaType.APPLICATION_JSON, ApiMediaType.APPLICATION_X_YAML })
public interface GlusterVolumeResource extends MeasurableResource {
    @GET
    GlusterVolume get();

    @DELETE
    Response remove();

    @Path("{action: (start|stop|rebalance|stoprebalance|setOption|resetOption|resetAllOptions)}/{oid}")
    ActionResource getActionSubresource(@PathParam("action") String action, @PathParam("oid") String oid);

    @POST
    @Consumes({ ApiMediaType.APPLICATION_XML, ApiMediaType.APPLICATION_JSON, ApiMediaType.APPLICATION_X_YAML })
    @Actionable
    @Path("start")
    Response start(Action action);

    @POST
    @Consumes({ ApiMediaType.APPLICATION_XML, ApiMediaType.APPLICATION_JSON, ApiMediaType.APPLICATION_X_YAML })
    @Actionable
    @Path("stop")
    Response stop(Action action);

    @POST
    @Consumes({ ApiMediaType.APPLICATION_XML, ApiMediaType.APPLICATION_JSON, ApiMediaType.APPLICATION_X_YAML })
    @Actionable
    @Path("rebalance")
    Response rebalance(Action action);

    @POST
    @Consumes({ ApiMediaType.APPLICATION_XML, ApiMediaType.APPLICATION_JSON, ApiMediaType.APPLICATION_X_YAML })
    @Actionable
    @Path("stoprebalance")
    Response stopRebalance(Action action);

    @POST
    @Consumes({ ApiMediaType.APPLICATION_XML, ApiMediaType.APPLICATION_JSON, ApiMediaType.APPLICATION_X_YAML })
    @Actionable
    @Path("setoption")
    Response setOption(Action action);

    @POST
    @Consumes({ ApiMediaType.APPLICATION_XML, ApiMediaType.APPLICATION_JSON, ApiMediaType.APPLICATION_X_YAML })
    @Actionable
    @Path("resetoption")
    Response resetOption(Action action);

    @POST
    @Consumes({ ApiMediaType.APPLICATION_XML, ApiMediaType.APPLICATION_JSON, ApiMediaType.APPLICATION_X_YAML })
    @Actionable
    @Path("resetalloptions")
    Response resetAllOptions(Action action);

    @POST
    @Consumes({ ApiMediaType.APPLICATION_XML, ApiMediaType.APPLICATION_JSON, ApiMediaType.APPLICATION_X_YAML })
    @Actionable
    @Path("startprofile")
    Response startProfile(Action action);

    @POST
    @Consumes({ ApiMediaType.APPLICATION_XML, ApiMediaType.APPLICATION_JSON, ApiMediaType.APPLICATION_X_YAML })
    @Actionable
    @Path("stopprofile")
    Response stopProfile(Action action);

    @GET
    @Path("profilestatistics")
    @Produces({
        ApiMediaType.APPLICATION_XML,
        ApiMediaType.APPLICATION_JSON,
        ApiMediaType.APPLICATION_X_YAML,
        ApiMediaType.APPLICATION_PDF
    })
    GlusterVolumeProfileDetails getProfileStatistics();

    /**
     * Sub-resource locator method, returns GlusterBricksResource on which the remainder of the URI is dispatched.
     *
     * @return matching subresource if found
     */
    @Path("bricks")
    GlusterBricksResource getGlusterBrickSubResource();
}

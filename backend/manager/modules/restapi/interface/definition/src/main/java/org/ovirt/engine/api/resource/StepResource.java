package org.ovirt.engine.api.resource;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Action;
import org.ovirt.engine.api.model.Actionable;
import org.ovirt.engine.api.model.Step;

@Produces({ ApiMediaType.APPLICATION_XML, ApiMediaType.APPLICATION_JSON, ApiMediaType.APPLICATION_X_YAML })
public interface StepResource extends MeasurableResource {

    @Path("{action: (end)}/{oid}")
    public ActionResource getActionSubresource(@PathParam("action") String action, @PathParam("oid") String oid);

    @POST
    @Consumes({ ApiMediaType.APPLICATION_XML, ApiMediaType.APPLICATION_JSON, ApiMediaType.APPLICATION_X_YAML })
    @Actionable
    @Path("end")
    public Response end(Action action);

    @GET
    public Step get();
}

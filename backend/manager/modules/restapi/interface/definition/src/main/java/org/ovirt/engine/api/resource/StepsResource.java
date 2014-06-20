package org.ovirt.engine.api.resource;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Step;
import org.ovirt.engine.api.model.Steps;

@Produces( { ApiMediaType.APPLICATION_XML, ApiMediaType.APPLICATION_JSON, ApiMediaType.APPLICATION_X_YAML })
public interface StepsResource {

    @GET
    public Steps list();

    @POST
    @Consumes({ApiMediaType.APPLICATION_XML, ApiMediaType.APPLICATION_JSON, ApiMediaType.APPLICATION_X_YAML})
    public Response add(Step step);

    /**
     * Sub-resource locator method, returns individual EventResource on which the remainder of the URI is dispatched.
     *
     * @param id the Event ID
     * @return matching sub-resource if found
     */
    @Path("{id}")
    public StepResource getStepSubResource(@PathParam("id") String id);
}

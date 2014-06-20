package org.ovirt.engine.api.resource;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Job;
import org.ovirt.engine.api.model.Jobs;

@Path("/jobs")
@Produces( { ApiMediaType.APPLICATION_XML, ApiMediaType.APPLICATION_JSON, ApiMediaType.APPLICATION_X_YAML })
public interface JobsResource {

    @GET
    public Jobs list();

    @POST
    @Consumes({ApiMediaType.APPLICATION_XML, ApiMediaType.APPLICATION_JSON, ApiMediaType.APPLICATION_X_YAML})
    public Response add(Job job);

    /**
     * Sub-resource locator method, returns individual EventResource on which the remainder of the URI is dispatched.
     *
     * @param id the Event ID
     * @return matching sub-resource if found
     */
    @Path("{id}")
    public JobResource getJobSubResource(@PathParam("id") String id);
}

package org.ovirt.engine.api.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.ovirt.engine.api.model.Hooks;

@Produces({ ApiMediaType.APPLICATION_XML, ApiMediaType.APPLICATION_JSON, ApiMediaType.APPLICATION_X_YAML })
public interface HostHooksResource {

    @GET
    public Hooks list();

    /**
     * Sub-resource locator method, returns individual HostHookResource on which the remainder of the URI is dispatched.
     *
     * @param id
     *            the hook ID
     * @return matching subresource if found
     */
    @Path("{id}")
    public HostHookResource getHookSubResource(@PathParam("id") String id);
}

package org.ovirt.engine.api.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.jboss.resteasy.annotations.providers.jaxb.Formatted;
import org.ovirt.engine.api.model.Hooks;

@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_X_YAML })
public interface HostHooksResource {

    @GET
    @Formatted
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

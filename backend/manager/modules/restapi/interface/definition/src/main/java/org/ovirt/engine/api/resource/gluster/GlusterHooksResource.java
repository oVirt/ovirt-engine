package org.ovirt.engine.api.resource.gluster;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.ovirt.engine.api.model.GlusterHooks;
import org.ovirt.engine.api.resource.ApiMediaType;

/**
 * Resource interface for the "clusters/{cluster_id}/glusterhooks" resource
 */
@Produces({ ApiMediaType.APPLICATION_XML, ApiMediaType.APPLICATION_JSON })
public interface GlusterHooksResource {
    @GET
    GlusterHooks list();

    /**
     * Sub-resource locator method, returns individual GlusterHookResource on which the remainder of the URI is
     * dispatched.
     *
     * @param id the identifier of the hook
     * @return matching resource if found
     */
    @Path("{id}")
    GlusterHookResource getGlusterHookResource(@PathParam("id") String id);
}

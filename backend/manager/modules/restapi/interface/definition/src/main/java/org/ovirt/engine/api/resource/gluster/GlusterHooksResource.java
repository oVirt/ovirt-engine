/**
 *
 */
package org.ovirt.engine.api.resource.gluster;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.GlusterHooks;
import org.ovirt.engine.api.resource.ApiMediaType;

/**
 * Resource interface for the "clusters/{cluster_id}/glusterhooks" resource
 */
@Produces({ ApiMediaType.APPLICATION_XML, ApiMediaType.APPLICATION_JSON, ApiMediaType.APPLICATION_X_YAML })
public interface GlusterHooksResource {
    @GET
    public GlusterHooks list();


    /**
     * Sub-resource locator method, returns individual GlusterHookResource on which the remainder of the URI is
     * dispatched.
     *
     * @param name
     *            the GlusterHook name
     * @return matching subresource if found
     */
    @Path("{hook_id}")
    public GlusterHookResource getGlusterHookSubResource(@PathParam("hook_id") String id);

    /**
     * Removes the given Gluster hook from all servers in cluster and deletes it from the database.
     *
     * @param id ID of the hook to be removed
     * @return
     */
    @DELETE
    @Path("{hook_id}")
    public Response remove(@PathParam("hook_id") String id);
}

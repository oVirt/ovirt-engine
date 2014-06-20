package org.ovirt.engine.api.resource.gluster;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Action;
import org.ovirt.engine.api.model.Actionable;
import org.ovirt.engine.api.model.GlusterHook;
import org.ovirt.engine.api.resource.ActionResource;
import org.ovirt.engine.api.resource.ApiMediaType;

/**
 * Resource interface for the "clusters/{cluster_id}/glusterhooks/{hook_id}" resource
 */
@Produces({ ApiMediaType.APPLICATION_XML, ApiMediaType.APPLICATION_JSON, ApiMediaType.APPLICATION_X_YAML })
public interface GlusterHookResource {
    @GET
    public GlusterHook get();

    @Path("{action: (enable|disable|resolve)}/{oid}")
    public ActionResource getActionSubresource(@PathParam("action") String action, @PathParam("oid") String oid);

    /**
     * Resolves status conflict of hook among servers in cluster by enabling gluster hook in
     * all servers of the cluster. This updates the hook status to ENABLED in database
     * @param action
     * @return
     */
    @POST
    @Consumes({ ApiMediaType.APPLICATION_XML, ApiMediaType.APPLICATION_JSON, ApiMediaType.APPLICATION_X_YAML })
    @Actionable
    @Path("enable")
    public Response enable(Action action);

    /**
     * Resolves status conflict of hook among servers in cluster by disabling gluster hook in
     * all servers of the cluster. This updates the hook status to DISABLED in database
     * @param action
     * @return
     */
    @POST
    @Consumes({ ApiMediaType.APPLICATION_XML, ApiMediaType.APPLICATION_JSON, ApiMediaType.APPLICATION_X_YAML })
    @Actionable
    @Path("disable")
    public Response disable(Action action);

    /**
     * Resolves missing hook conflict depending on the resolution type
     *  for ADD - resolves by copying hook stored in engine database to all servers
     *            where the hook is missing. The engine maintains a list of all servers
     *            where hook is missing.
     *  for COPY - Resolves conflict in hook content by copying hook stored in engine
     *             database to all servers where the hook is missing.
     *             The engine maintains a list of all servers where the content is
     *             conflicting. If a host id is passed as parameter,the hook content
     *             from the server is used as the master to copy to other servers in cluster.
     *
     * @param action
     * @return
     */
    @POST
    @Consumes({ ApiMediaType.APPLICATION_XML, ApiMediaType.APPLICATION_JSON, ApiMediaType.APPLICATION_X_YAML })
    @Actionable
    @Path("resolve")
    public Response resolve(Action action);
}

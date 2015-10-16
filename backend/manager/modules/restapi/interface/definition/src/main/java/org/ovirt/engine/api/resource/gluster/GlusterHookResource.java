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
import org.ovirt.engine.api.model.GlusterHook;
import org.ovirt.engine.api.resource.ActionResource;
import org.ovirt.engine.api.resource.ApiMediaType;

/**
 * Resource interface for the "clusters/{cluster_id}/glusterhooks/{hook_id}" resource
 */
@Produces({ ApiMediaType.APPLICATION_XML, ApiMediaType.APPLICATION_JSON })
public interface GlusterHookResource {
    @GET
    GlusterHook get();

    /**
     * Removes the this Gluster hook from all servers in cluster and deletes it from the database.
     */
    @DELETE
    Response remove();

    @Path("{action: (enable|disable|resolve)}/{oid}")
    ActionResource getActionResource(@PathParam("action") String action, @PathParam("oid") String oid);

    /**
     * Resolves status conflict of hook among servers in cluster by enabling gluster hook in
     * all servers of the cluster. This updates the hook status to ENABLED in database.
     */
    @POST
    @Consumes({ ApiMediaType.APPLICATION_XML, ApiMediaType.APPLICATION_JSON })
    @Actionable
    @Path("enable")
    Response enable(Action action);

    /**
     * Resolves status conflict of hook among servers in cluster by disabling gluster hook in
     * all servers of the cluster. This updates the hook status to DISABLED in database.
     */
    @POST
    @Consumes({ ApiMediaType.APPLICATION_XML, ApiMediaType.APPLICATION_JSON })
    @Actionable
    @Path("disable")
    Response disable(Action action);

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
     */
    @POST
    @Consumes({ ApiMediaType.APPLICATION_XML, ApiMediaType.APPLICATION_JSON })
    @Actionable
    @Path("resolve")
    Response resolve(Action action);
}

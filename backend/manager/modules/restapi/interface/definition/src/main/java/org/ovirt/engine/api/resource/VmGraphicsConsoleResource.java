package org.ovirt.engine.api.resource;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Action;
import org.ovirt.engine.api.model.Actionable;
import org.ovirt.engine.api.model.GraphicsConsole;

public interface VmGraphicsConsoleResource {

    /**
     * A method handling GET requests with media type XML or JSON.
     * Returns the console entity usable by REST Clients
     *
     * @return GraphicsConsole - the json or XML representation of the console
     */
    @GET
    @Produces({ ApiMediaType.APPLICATION_XML, ApiMediaType.APPLICATION_JSON,
            ApiMediaType.APPLICATION_X_YAML})
    public GraphicsConsole get();

    /**
     * A method handling GET requests with media type x-virt-viewer.
     * Returns a console representation usable by virt-viewer client (e.g. a .vv file)
     *
     * @return a console representation for virt-viewer (e.g. a .vv file)
     */
    @GET
    @Produces({ApiMediaType.APPLICATION_X_VIRT_VIEWER})
    public Response generateDescriptor();

    @DELETE
    public Response remove();

    @POST
    @Consumes({ApiMediaType.APPLICATION_XML, ApiMediaType.APPLICATION_JSON, ApiMediaType.APPLICATION_X_YAML})
    @Actionable
    @Path("proxyticket")
    public Response proxyTicket(Action action);
}

package org.ovirt.engine.api.resource;

import org.ovirt.engine.api.model.GraphicsConsole;
import org.ovirt.engine.api.model.GraphicsConsoles;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

@Produces({ApiMediaType.APPLICATION_XML, ApiMediaType.APPLICATION_JSON, ApiMediaType.APPLICATION_X_YAML})
public interface VmGraphicsConsolesResource {

    @GET
    public GraphicsConsoles list();

    @Path("{iden}")
    public VmGraphicsConsoleResource getVmGraphicsConsoleResource(@PathParam("iden") String id);

    @POST
    @Consumes({ApiMediaType.APPLICATION_XML, ApiMediaType.APPLICATION_JSON, ApiMediaType.APPLICATION_X_YAML})
    Response add(GraphicsConsole device);

}

package org.ovirt.engine.api.resource;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.GraphicsConsole;

public interface GraphicsConsoleResource {

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

    @DELETE
    public Response remove();
}

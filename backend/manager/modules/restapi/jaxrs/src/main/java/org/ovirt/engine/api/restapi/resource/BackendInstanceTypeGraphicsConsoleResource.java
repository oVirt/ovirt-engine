package org.ovirt.engine.api.restapi.resource;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.GraphicsConsole;
import org.ovirt.engine.api.resource.InstanceTypeGraphicsConsoleResource;
import org.ovirt.engine.core.compat.Guid;

public class BackendInstanceTypeGraphicsConsoleResource
    extends BackendResource
    implements InstanceTypeGraphicsConsoleResource {

    private BackendInstanceTypeGraphicsConsolesResource parent;
    private Guid guid;
    private String consoleId;

    public BackendInstanceTypeGraphicsConsoleResource(BackendInstanceTypeGraphicsConsolesResource parent, Guid guid, String consoleId) {
        this.parent = parent;
        this.guid = guid;
        this.consoleId = consoleId;
    }

    @Override
    public GraphicsConsole get() {
        return BackendGraphicsConsoleHelper.get(parent::list, consoleId);
    }

    @Override
    public Response remove() {
        return BackendGraphicsConsoleHelper.remove(this, guid, consoleId);
    }
}

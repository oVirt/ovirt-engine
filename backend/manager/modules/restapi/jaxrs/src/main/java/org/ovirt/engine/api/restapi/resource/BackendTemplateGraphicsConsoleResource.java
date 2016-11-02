package org.ovirt.engine.api.restapi.resource;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.GraphicsConsole;
import org.ovirt.engine.api.resource.TemplateGraphicsConsoleResource;
import org.ovirt.engine.core.compat.Guid;

public class BackendTemplateGraphicsConsoleResource
    extends BackendResource
    implements TemplateGraphicsConsoleResource {

    private BackendTemplateGraphicsConsolesResource parent;
    private Guid guid;
    private String consoleId;

    public BackendTemplateGraphicsConsoleResource(BackendTemplateGraphicsConsolesResource parent, Guid guid, String consoleId) {
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

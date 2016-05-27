package org.ovirt.engine.api.restapi.resource;

import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.GraphicsConsole;
import org.ovirt.engine.api.model.GraphicsType;
import org.ovirt.engine.api.resource.GraphicsConsoleResource;
import org.ovirt.engine.api.resource.GraphicsConsolesResource;
import org.ovirt.engine.api.restapi.util.DisplayHelper;
import org.ovirt.engine.api.restapi.utils.HexUtils;
import org.ovirt.engine.core.common.action.GraphicsParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.GraphicsDevice;
import org.ovirt.engine.core.compat.Guid;

public class BackendGraphicsConsoleResource
    extends BackendResource
    implements GraphicsConsoleResource {

    private GraphicsConsolesResource parent;
    private Guid guid;
    private String consoleId;

    public BackendGraphicsConsoleResource(BackendGraphicsConsolesResource parent, Guid guid, String consoleId) {
        this.parent = parent;
        this.guid = guid;
        this.consoleId = consoleId;
    }

    @Override
    public GraphicsConsole get() {
        for (GraphicsConsole graphicsConsole : parent.list().getGraphicsConsoles()) {
            if (consoleId.equals(graphicsConsole.getId())) {
                return graphicsConsole;
            }
        }

        throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).build());
    }

    protected org.ovirt.engine.core.common.businessentities.GraphicsType asGraphicsType() {
        String consoleString = HexUtils.hex2string(consoleId);

        GraphicsType type = GraphicsType.fromValue(consoleString);
        return getMappingLocator().getMapper(GraphicsType.class, org.ovirt.engine.core.common.businessentities.GraphicsType.class).map(type, null);
    }

    @Override
    public Response remove() {
        org.ovirt.engine.core.common.businessentities.GraphicsType graphicsType = asGraphicsType();

        List<GraphicsDevice> devices = DisplayHelper.getGraphicsDevicesForEntity(this, guid, false);
        if (devices == null) {
            throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).build());
        }

        for (GraphicsDevice device : devices) {
            if (device.getGraphicsType().equals(graphicsType)) {
                return performAction(VdcActionType.RemoveGraphicsDevice, new GraphicsParameters(device));
            }
        }

        throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).build());
    }

    protected String getConsoleId() {
        return consoleId;
    }

    public Guid getGuid() {
        return guid;
    }
}

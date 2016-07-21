package org.ovirt.engine.api.restapi.resource;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.GraphicsConsole;
import org.ovirt.engine.api.model.GraphicsConsoles;
import org.ovirt.engine.api.resource.GraphicsConsolesResource;
import org.ovirt.engine.api.restapi.types.VmMapper;
import org.ovirt.engine.api.restapi.util.DisplayHelper;
import org.ovirt.engine.api.restapi.util.ParametersHelper;
import org.ovirt.engine.core.common.action.GraphicsParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.GraphicsDevice;
import org.ovirt.engine.core.common.businessentities.GraphicsInfo;
import org.ovirt.engine.core.common.businessentities.GraphicsType;
import org.ovirt.engine.core.compat.Guid;

public abstract class BackendGraphicsConsolesResource<T>
        extends AbstractBackendCollectionResource<GraphicsConsole, T>
        implements GraphicsConsolesResource {
    private static final String CURRENT = "current";

    private final Guid guid;

    public BackendGraphicsConsolesResource(Guid guid, Class<T> entiytType) {
        super(GraphicsConsole.class, entiytType);
        this.guid = guid;
    }

    @Override
    public GraphicsConsoles list() {
        GraphicsConsoles consoles = new GraphicsConsoles();

        Map<GraphicsType, GraphicsInfo> graphicsTypeToGraphicsInfo;
        T entity = loadEntity();

        boolean current = ParametersHelper.getBooleanParameter(httpHeaders, uriInfo, CURRENT, true, false);
        if (current) {
            // from entity dynamic (e.g. what is now present on the VM
            graphicsTypeToGraphicsInfo = extractGraphicsInofs(entity);
        } else {
            // from devices (e.g. what is configured on the VM)
            List<GraphicsType> graphicsTypes = DisplayHelper.getGraphicsTypesForEntity(this, guid, true);

            graphicsTypeToGraphicsInfo = new HashMap<>();
            for (GraphicsType type : graphicsTypes) {
                graphicsTypeToGraphicsInfo.put(type, null);
            }
        }

        for (Map.Entry<GraphicsType, GraphicsInfo> graphicsInfo : graphicsTypeToGraphicsInfo.entrySet()) {
            consoles.getGraphicsConsoles().add(addLinks(populate(VmMapper.map(graphicsInfo, null), entity)));
        }

        return consoles;
    }

    protected abstract Map<GraphicsType, GraphicsInfo> extractGraphicsInofs(T vm);

    protected abstract T loadEntity();

    @Override
    public Response add(GraphicsConsole console) {
        GraphicsDevice device = getMapper(GraphicsConsole.class, GraphicsDevice.class).map(console, null);
        device.setVmId(guid);
        VdcReturnValueBase res = doCreateEntity(VdcActionType.AddGraphicsDevice, createAddGraphicsDeviceParams(device));
        if (res != null && res.getSucceeded()) {
            for (GraphicsConsole existing : list().getGraphicsConsoles()) {
                // only one with this protocol can exist so the protocol is unique
                if (existing.getProtocol().equals(console.getProtocol())) {
                    return Response.created(URI.create(existing.getHref())).entity(existing).build();
                }
            }
        }

        throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).build());
    }

    protected GraphicsParameters createAddGraphicsDeviceParams(GraphicsDevice device) {
        return new GraphicsParameters(device);
    }

    public Guid getGuid() {
        return guid;
    }
}


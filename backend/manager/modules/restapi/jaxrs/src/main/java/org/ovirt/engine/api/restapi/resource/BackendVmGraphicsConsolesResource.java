package org.ovirt.engine.api.restapi.resource;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.GraphicsConsole;
import org.ovirt.engine.api.model.GraphicsConsoles;
import org.ovirt.engine.api.model.Vm;
import org.ovirt.engine.api.resource.VmGraphicsConsolesResource;
import org.ovirt.engine.api.restapi.types.VmMapper;
import org.ovirt.engine.api.restapi.util.DisplayHelper;
import org.ovirt.engine.api.restapi.util.ParametersHelper;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.GraphicsParameters;
import org.ovirt.engine.core.common.businessentities.GraphicsDevice;
import org.ovirt.engine.core.common.businessentities.GraphicsInfo;
import org.ovirt.engine.core.common.businessentities.GraphicsType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendVmGraphicsConsolesResource
        extends AbstractBackendCollectionResource<GraphicsConsole, VM>
        implements VmGraphicsConsolesResource {

    public static final String CURRENT = "current";

    private final Guid guid;

    public BackendVmGraphicsConsolesResource(Guid guid) {
        super(GraphicsConsole.class, VM.class);
        this.guid = guid;
    }

    @Override
    public GraphicsConsoles list() {
        GraphicsConsoles consoles = new GraphicsConsoles();

        Map<GraphicsType, GraphicsInfo> graphicsTypeToGraphicsInfo;
        VM entity = loadEntity();

        boolean current = ParametersHelper.getBooleanParameter(httpHeaders, uriInfo, CURRENT, true, false);
        if (current) {
            // from entity dynamic (e.g. what is now present on the VM
            graphicsTypeToGraphicsInfo = extractGraphicsInofs(entity);
        } else {
            // from devices (e.g. what is configured on the VM)
            List<GraphicsType> graphicsTypes = DisplayHelper.getGraphicsTypesForEntity(this, guid, true);

            graphicsTypeToGraphicsInfo = new EnumMap<>(GraphicsType.class);
            for (GraphicsType type : graphicsTypes) {
                graphicsTypeToGraphicsInfo.put(type, null);
            }
        }

        for (Map.Entry<GraphicsType, GraphicsInfo> graphicsInfo : graphicsTypeToGraphicsInfo.entrySet()) {
            consoles.getGraphicsConsoles().add(addLinks(populate(VmMapper.map(graphicsInfo, null), entity)));
        }

        return consoles;
    }

    @Override
    public Response add(GraphicsConsole console) {
        GraphicsDevice device = getMapper(GraphicsConsole.class, GraphicsDevice.class).map(console, null);
        device.setVmId(guid);
        ActionReturnValue res = doCreateEntity(ActionType.AddGraphicsAndVideoDevices, createAddGraphicsDeviceParams(device));

        if (res != null && res.getSucceeded()) {
            return BackendGraphicsConsoleHelper.find(console, this::list);
        }

        throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).build());
    }

    protected GraphicsParameters createAddGraphicsDeviceParams(GraphicsDevice device) {
        return new GraphicsParameters(device);
    }

    protected Map<GraphicsType, GraphicsInfo> extractGraphicsInofs(VM vm) {
        return vm.getGraphicsInfos();
    }

    protected VM loadEntity() {
        return getEntity(VM.class, QueryType.GetVmByVmId,
                new IdQueryParameters(guid), guid.toString(), true);
    }

    @Override
    public BackendVmGraphicsConsoleResource getConsoleResource(String id) {
        return inject(new BackendVmGraphicsConsoleResource(this, guid, id));
    }

    @Override
    protected GraphicsConsole addParents(GraphicsConsole model) {
        model.setVm(new Vm());
        model.getVm().setId(guid.toString());
        return model;
    }
}


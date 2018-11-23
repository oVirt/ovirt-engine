package org.ovirt.engine.api.restapi.resource;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.GraphicsConsole;
import org.ovirt.engine.api.model.GraphicsConsoles;
import org.ovirt.engine.api.resource.InstanceTypeGraphicsConsoleResource;
import org.ovirt.engine.api.resource.InstanceTypeGraphicsConsolesResource;
import org.ovirt.engine.api.restapi.types.VmMapper;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.GraphicsParameters;
import org.ovirt.engine.core.common.businessentities.GraphicsDevice;
import org.ovirt.engine.core.common.businessentities.InstanceType;
import org.ovirt.engine.core.common.queries.GetVmTemplateParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendInstanceTypeGraphicsConsolesResource
    extends AbstractBackendCollectionResource<GraphicsConsole, InstanceType>
    implements InstanceTypeGraphicsConsolesResource {

    private final Guid guid;

    public BackendInstanceTypeGraphicsConsolesResource(Guid guid) {
        super(GraphicsConsole.class, InstanceType.class);
        this.guid = guid;
    }

    @Override
    public GraphicsConsoles list() {
        GraphicsConsoles consoles = new GraphicsConsoles();
        InstanceType entity = loadEntity();

        BackendGraphicsConsoleHelper.list(this, guid).entrySet()
            .forEach(graphicsInfo ->
                consoles.getGraphicsConsoles().add(addLinks(
                        populate(VmMapper.map(graphicsInfo, null), entity),
                        // The suggestedParent parameter is necessary because InstanceType
                        // is a subclass of Template. Without it the Template class would
                        // be detected as parent and the link would contain 'null'.
                        org.ovirt.engine.api.model.InstanceType.class
                ))
            );

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

    protected InstanceType loadEntity() {
        return getEntity(
            InstanceType.class,
            QueryType.GetInstanceType,
            new GetVmTemplateParameters(guid),
            guid.toString(),
            true
        );
    }

    protected GraphicsParameters createAddGraphicsDeviceParams(GraphicsDevice device) {
        return new GraphicsParameters(device).setVm(false);
    }

    @Override
    public InstanceTypeGraphicsConsoleResource getConsoleResource(String id) {
        return inject(new BackendInstanceTypeGraphicsConsoleResource(this, guid, id));
    }

    @Override
    protected GraphicsConsole addParents(GraphicsConsole model) {
        model.setInstanceType(new org.ovirt.engine.api.model.InstanceType());
        model.getInstanceType().setId(guid.toString());
        return model;
    }
}

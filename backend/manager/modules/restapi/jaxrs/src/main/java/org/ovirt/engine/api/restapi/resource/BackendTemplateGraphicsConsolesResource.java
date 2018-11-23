package org.ovirt.engine.api.restapi.resource;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.GraphicsConsole;
import org.ovirt.engine.api.model.GraphicsConsoles;
import org.ovirt.engine.api.model.Template;
import org.ovirt.engine.api.resource.TemplateGraphicsConsolesResource;
import org.ovirt.engine.api.restapi.types.VmMapper;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.GraphicsParameters;
import org.ovirt.engine.core.common.businessentities.GraphicsDevice;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.queries.GetVmTemplateParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendTemplateGraphicsConsolesResource
    extends AbstractBackendCollectionResource<GraphicsConsole, VmTemplate>
    implements TemplateGraphicsConsolesResource {

    private final Guid guid;

    public BackendTemplateGraphicsConsolesResource(Guid guid) {
        super(GraphicsConsole.class, VmTemplate.class);
        this.guid = guid;
    }

    @Override
    public GraphicsConsoles list() {
        GraphicsConsoles consoles = new GraphicsConsoles();
        VmTemplate entity = loadEntity();

        BackendGraphicsConsoleHelper.list(this, guid).entrySet()
            .forEach(graphicsInfo ->
                consoles.getGraphicsConsoles().add(addLinks(
                        populate(VmMapper.map(graphicsInfo, null), entity),
                        Template.class
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

    protected VmTemplate loadEntity() {
        return getEntity(VmTemplate.class, QueryType.GetVmTemplate,
                new GetVmTemplateParameters(guid), guid.toString(), true);
    }

    protected GraphicsParameters createAddGraphicsDeviceParams(GraphicsDevice device) {
        return new GraphicsParameters(device).setVm(false);
    }

    @Override
    public BackendTemplateGraphicsConsoleResource getConsoleResource(String id) {
        return inject(new BackendTemplateGraphicsConsoleResource(this, guid, id));
    }

    @Override
    protected GraphicsConsole addParents(GraphicsConsole model) {
        model.setTemplate(new Template());
        model.getTemplate().setId(guid.toString());
        return model;
    }
}


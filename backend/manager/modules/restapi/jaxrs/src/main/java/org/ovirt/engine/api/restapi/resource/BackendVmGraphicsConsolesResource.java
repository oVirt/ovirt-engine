package org.ovirt.engine.api.restapi.resource;

import org.ovirt.engine.api.common.util.QueryHelper;
import org.ovirt.engine.api.model.GraphicsConsole;
import org.ovirt.engine.api.model.GraphicsConsoles;
import org.ovirt.engine.api.model.VM;
import org.ovirt.engine.api.resource.VmGraphicsConsoleResource;
import org.ovirt.engine.api.resource.VmGraphicsConsolesResource;
import org.ovirt.engine.api.restapi.types.VmMapper;
import org.ovirt.engine.api.restapi.util.DisplayHelper;
import org.ovirt.engine.core.common.action.GraphicsParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.GraphicsDevice;
import org.ovirt.engine.core.common.businessentities.GraphicsInfo;
import org.ovirt.engine.core.common.businessentities.GraphicsType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BackendVmGraphicsConsolesResource
        extends AbstractBackendCollectionResource<GraphicsConsole, org.ovirt.engine.core.common.businessentities.VM>
        implements VmGraphicsConsolesResource {

    private final Guid guid;

    public BackendVmGraphicsConsolesResource(Guid guid) {
        super(GraphicsConsole.class, org.ovirt.engine.core.common.businessentities.VM.class);
        this.guid = guid;
    }

    @Override
    public GraphicsConsoles list() {
        GraphicsConsoles consoles = new GraphicsConsoles();

        Map<GraphicsType, GraphicsInfo> graphicsTypeToGraphicsInfo;
        org.ovirt.engine.core.common.businessentities.VM vm = getEntity(org.ovirt.engine.core.common.businessentities.VM.class, VdcQueryType.GetVmByVmId,
                new IdQueryParameters(guid), guid.toString(), true);

        if (QueryHelper.hasCurrentConstraint(getUriInfo())) {
            // from vm dynamic (e.g. what is now present on the VM
            graphicsTypeToGraphicsInfo = vm.getGraphicsInfos();
        } else {
            // from devices (e.g. what is configured on the VM)
            List<GraphicsType> graphicsTypes = DisplayHelper.getGraphicsTypesForEntity(this, guid);

            graphicsTypeToGraphicsInfo = new HashMap<>();
            for (GraphicsType type : graphicsTypes) {
                graphicsTypeToGraphicsInfo.put(type, null);
            }
        }

        for (Map.Entry<GraphicsType, GraphicsInfo> graphicsInfo : graphicsTypeToGraphicsInfo.entrySet()) {
            consoles.getGraphicsConsoles().add(addLinks(populate(VmMapper.map(graphicsInfo, null), vm)));
        }

        return consoles;
    }

    @Override
    public VmGraphicsConsoleResource getVmGraphicsConsoleResource(String id) {
        return inject(new BackendVmGraphicsConsoleResource(this, guid, id));
    }

    @Override
    public Response add(GraphicsConsole console) {
        GraphicsDevice device = getMapper(GraphicsConsole.class, GraphicsDevice.class).map(console, null);
        device.setVmId(guid);
        VdcReturnValueBase res = doCreateEntity(VdcActionType.AddGraphicsDevice, new GraphicsParameters(device));
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

    @Override
    protected GraphicsConsole addParents(GraphicsConsole model) {
        model.setVm(new VM());
        model.getVm().setId(guid.toString());
        return model;
    }
}


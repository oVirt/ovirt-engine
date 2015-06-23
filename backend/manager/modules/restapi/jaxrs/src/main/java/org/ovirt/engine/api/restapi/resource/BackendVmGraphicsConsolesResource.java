package org.ovirt.engine.api.restapi.resource;

import org.ovirt.engine.api.model.GraphicsConsole;
import org.ovirt.engine.api.model.GraphicsConsoles;
import org.ovirt.engine.api.resource.VmGraphicsConsoleResource;
import org.ovirt.engine.api.resource.VmGraphicsConsolesResource;
import org.ovirt.engine.api.restapi.types.VmMapper;
import org.ovirt.engine.core.common.businessentities.GraphicsInfo;
import org.ovirt.engine.core.common.businessentities.GraphicsType;
import org.ovirt.engine.api.model.VM;

import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

import javax.ws.rs.core.Response;
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
        org.ovirt.engine.core.common.businessentities.VM vm = getEntity(org.ovirt.engine.core.common.businessentities.VM.class, VdcQueryType.GetVmByVmId,
                new IdQueryParameters(guid), guid.toString(), true);

        GraphicsConsoles consoles = new GraphicsConsoles();

        for (Map.Entry<GraphicsType, GraphicsInfo> graphicsInfo : vm.getGraphicsInfos().entrySet()) {
            consoles.getGraphicsConsoles().add(addLinks(populate(VmMapper.map(graphicsInfo, null), vm)));
        }

        return consoles;
    }

    @Override
    public VmGraphicsConsoleResource getVmGraphicsConsoleResource(String id) {
        return inject(new BackendVmGraphicsConsoleResource(this, guid, id));
    }

    @Override
    protected Response performRemove(String id) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected GraphicsConsole addParents(GraphicsConsole model) {
        model.setVm(new VM());
        model.getVm().setId(guid.toString());
        return model;
    }
}


package org.ovirt.engine.api.restapi.resource;

import java.util.Map;

import org.ovirt.engine.api.model.GraphicsConsole;
import org.ovirt.engine.api.model.Vm;
import org.ovirt.engine.api.resource.GraphicsConsoleResource;
import org.ovirt.engine.core.common.businessentities.GraphicsInfo;
import org.ovirt.engine.core.common.businessentities.GraphicsType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendVmGraphicsConsolesResource
        extends BackendGraphicsConsolesResource<org.ovirt.engine.core.common.businessentities.VM> {

    public BackendVmGraphicsConsolesResource(Guid guid) {
        super(guid, org.ovirt.engine.core.common.businessentities.VM.class);
    }

    @Override
    protected Map<GraphicsType, GraphicsInfo> extractGraphicsInofs(org.ovirt.engine.core.common.businessentities.VM vm) {
        return vm.getGraphicsInfos();
    }

    @Override
    protected org.ovirt.engine.core.common.businessentities.VM loadEntity() {
        return getEntity(org.ovirt.engine.core.common.businessentities.VM.class, VdcQueryType.GetVmByVmId,
                new IdQueryParameters(getGuid()), getGuid().toString(), true);
    }

    @Override
    public GraphicsConsoleResource getConsoleResource(String id) {
        return inject(new BackendVmGraphicsConsoleResource(this, getGuid(), id));
    }

    @Override
    protected GraphicsConsole addParents(GraphicsConsole model) {
        model.setVm(new Vm());
        model.getVm().setId(getGuid().toString());
        return model;
    }
}


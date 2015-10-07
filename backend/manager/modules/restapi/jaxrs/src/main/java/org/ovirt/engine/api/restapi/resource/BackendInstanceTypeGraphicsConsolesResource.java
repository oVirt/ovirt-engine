package org.ovirt.engine.api.restapi.resource;

import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.api.model.GraphicsConsole;

import org.ovirt.engine.api.model.InstanceType;
import org.ovirt.engine.api.resource.GraphicsConsoleResource;
import org.ovirt.engine.core.common.action.GraphicsParameters;
import org.ovirt.engine.core.common.businessentities.GraphicsDevice;
import org.ovirt.engine.core.common.businessentities.GraphicsInfo;
import org.ovirt.engine.core.common.businessentities.GraphicsType;
import org.ovirt.engine.core.common.queries.GetVmTemplateParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendInstanceTypeGraphicsConsolesResource
        extends BackendGraphicsConsolesResource<org.ovirt.engine.core.common.businessentities.InstanceType> {

    public BackendInstanceTypeGraphicsConsolesResource(Guid guid) {
        super(guid, org.ovirt.engine.core.common.businessentities.InstanceType.class);
    }

    @Override
    protected Map<GraphicsType, GraphicsInfo> extractGraphicsInofs(org.ovirt.engine.core.common.businessentities.InstanceType vm) {
        // no runtime info for template
        return new HashMap<>();
    }

    @Override
    protected org.ovirt.engine.core.common.businessentities.InstanceType loadEntity() {
        return getEntity(org.ovirt.engine.core.common.businessentities.InstanceType.class, VdcQueryType.GetInstanceType,
                new GetVmTemplateParameters(getGuid()), getGuid().toString(), true);
    }

    @Override
    protected GraphicsParameters createAddGraphicsDeviceParams(GraphicsDevice device) {
        return super.createAddGraphicsDeviceParams(device).setVm(false);
    }

    @Override
    public GraphicsConsoleResource getConsoleResource(String id) {
        return inject(new BackendGraphicsConsoleResource(this, getGuid(), id));
    }

    @Override
    protected GraphicsConsole addParents(GraphicsConsole model) {
        model.setInstanceType(new InstanceType());
        model.getInstanceType().setId(getGuid().toString());
        return model;
    }
}


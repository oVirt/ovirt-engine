package org.ovirt.engine.api.restapi.resource;

import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.api.model.GraphicsConsole;
import org.ovirt.engine.api.model.Template;
import org.ovirt.engine.api.resource.GraphicsConsoleResource;
import org.ovirt.engine.core.common.action.GraphicsParameters;
import org.ovirt.engine.core.common.businessentities.GraphicsDevice;
import org.ovirt.engine.core.common.businessentities.GraphicsInfo;
import org.ovirt.engine.core.common.businessentities.GraphicsType;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.queries.GetVmTemplateParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendTemplateGraphicsConsolesResource
        extends BackendGraphicsConsolesResource<VmTemplate> {

    public BackendTemplateGraphicsConsolesResource(Guid guid) {
        super(guid, VmTemplate.class);
    }

    @Override
    protected Map<GraphicsType, GraphicsInfo> extractGraphicsInofs(VmTemplate vm) {
        // no runtime info for template
        return new HashMap<>();
    }

    @Override
    protected VmTemplate loadEntity() {
        return getEntity(VmTemplate.class, VdcQueryType.GetVmTemplate,
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
        model.setTemplate(new Template());
        model.getTemplate().setId(getGuid().toString());
        return model;
    }
}


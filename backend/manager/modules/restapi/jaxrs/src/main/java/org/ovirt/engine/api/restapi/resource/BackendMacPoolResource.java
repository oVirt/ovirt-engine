package org.ovirt.engine.api.restapi.resource;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.MacPool;
import org.ovirt.engine.api.resource.MacPoolResource;
import org.ovirt.engine.core.common.action.MacPoolParameters;
import org.ovirt.engine.core.common.action.RemoveMacPoolByIdParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;

public class BackendMacPoolResource extends AbstractBackendSubResource<MacPool,
        org.ovirt.engine.core.common.businessentities.MacPool> implements MacPoolResource {

    public BackendMacPoolResource(String id) {
        super(id, MacPool.class, org.ovirt.engine.core.common.businessentities.MacPool.class);
    }

    @Override
    public MacPool get() {
        return performGet(VdcQueryType.GetMacPoolById, new IdQueryParameters(guid));
    }

    @Override
    public MacPool update(MacPool macPool) {
        return performUpdate(macPool,
                new QueryIdResolver<>(VdcQueryType.GetMacPoolById, IdQueryParameters.class),
                VdcActionType.UpdateMacPool,
                new UpdateParametersProvider());
    }

    private class UpdateParametersProvider implements ParametersProvider<MacPool, org.ovirt.engine.core.common.businessentities.MacPool> {
        @Override
        public VdcActionParametersBase getParameters(MacPool model, org.ovirt.engine.core.common.businessentities.MacPool entity) {
            final org.ovirt.engine.core.common.businessentities.MacPool macPool = map(model, entity);
            return new MacPoolParameters(macPool);
        }
    }

    @Override
    public Response remove() {
        get();
        return performAction(VdcActionType.RemoveMacPool, new RemoveMacPoolByIdParameters(guid));
    }
}

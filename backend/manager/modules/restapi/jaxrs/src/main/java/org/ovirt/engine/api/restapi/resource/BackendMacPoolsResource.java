package org.ovirt.engine.api.restapi.resource;

import java.util.List;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.MacPool;
import org.ovirt.engine.api.model.MacPools;
import org.ovirt.engine.api.resource.MacPoolResource;
import org.ovirt.engine.api.resource.MacPoolsResource;
import org.ovirt.engine.core.common.action.MacPoolParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendMacPoolsResource
        extends AbstractBackendCollectionResource<MacPool, org.ovirt.engine.core.common.businessentities.MacPool>
        implements MacPoolsResource{

    public BackendMacPoolsResource() {
        super(MacPool.class, org.ovirt.engine.core.common.businessentities.MacPool.class);
    }

    @Override
    public MacPools list() {
        return mapCollection(getBackendCollection(VdcQueryType.GetAllMacPools, new VdcQueryParametersBase()));
    }

    private MacPools mapCollection(List<org.ovirt.engine.core.common.businessentities.MacPool> entities) {
        MacPools collection = new MacPools();
        for (org.ovirt.engine.core.common.businessentities.MacPool entity : entities) {
            collection.getMacPools().add(addLinks(populate(map(entity), entity)));
        }

        return collection;
    }

    @Override
    public Response add(MacPool macPool) {
        validateParameters(macPool, "name");
        final org.ovirt.engine.core.common.businessentities.MacPool entity = map(macPool);
        return performCreate(VdcActionType.AddMacPool,
                new MacPoolParameters(entity),
                new QueryIdResolver<Guid>(VdcQueryType.GetMacPoolById, IdQueryParameters.class));
    }

    @Override
    public MacPoolResource getMacPoolResource(String id) {
        return inject(new BackendMacPoolResource(id));
    }
}

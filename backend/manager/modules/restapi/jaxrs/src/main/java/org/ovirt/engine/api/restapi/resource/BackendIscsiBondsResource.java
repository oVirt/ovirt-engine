package org.ovirt.engine.api.restapi.resource;

import java.util.List;

import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.IscsiBond;
import org.ovirt.engine.api.model.IscsiBonds;
import org.ovirt.engine.api.resource.IscsiBondResource;
import org.ovirt.engine.api.resource.IscsiBondsResource;
import org.ovirt.engine.api.restapi.types.IscsiBondMapper;
import org.ovirt.engine.core.common.action.AddIscsiBondParameters;
import org.ovirt.engine.core.common.action.RemoveIscsiBondParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendIscsiBondsResource extends AbstractBackendCollectionResource<IscsiBond, org.ovirt.engine.core.common.businessentities.IscsiBond>
        implements IscsiBondsResource {

    static final String[] SUB_COLLECTIONS = {"storageconnections", "networks", "add", };

    private Guid dataCenterId;

    protected BackendIscsiBondsResource(String datacenterId) {
        super(IscsiBond.class, org.ovirt.engine.core.common.businessentities.IscsiBond.class, SUB_COLLECTIONS);
        this.dataCenterId = asGuid(datacenterId);
    }

    @Override
    public IscsiBonds list() {
        return mapCollection(
                getBackendCollection(VdcQueryType.GetIscsiBondsByStoragePoolId, new IdQueryParameters(dataCenterId))
        );
    }

    @Override
    public Response add(IscsiBond iscsiBond) {
        validateParameters(iscsiBond, "name");
        org.ovirt.engine.core.common.businessentities.IscsiBond entity =
                getMapper(IscsiBond.class, org.ovirt.engine.core.common.businessentities.IscsiBond.class).map(iscsiBond, null);
        entity.setStoragePoolId(dataCenterId);

        return performCreate(VdcActionType.AddIscsiBond,
                new AddIscsiBondParameters(entity),
                new QueryIdResolver<Guid>(VdcQueryType.GetIscsiBondById, IdQueryParameters.class));
    }

    @Override
    protected Response performRemove(String id) {
        return performAction(VdcActionType.RemoveIscsiBond, new RemoveIscsiBondParameters(asGuid(id)));
    }

    @Override
    @SingleEntityResource
    public IscsiBondResource getIscsiBondSubResource(@PathParam("id") String id) {
        return inject(new BackendIscsiBondResource(id));
    }

    @Override
    protected IscsiBond doPopulate(IscsiBond model, org.ovirt.engine.core.common.businessentities.IscsiBond entity) {
        return model;
    }

    private IscsiBonds mapCollection(List<org.ovirt.engine.core.common.businessentities.IscsiBond> entities) {
        IscsiBonds iscsiBonds = new IscsiBonds();

        for (org.ovirt.engine.core.common.businessentities.IscsiBond entity : entities) {
            IscsiBond iscsiBond = IscsiBondMapper.map(entity, null);
            iscsiBonds.getIscsiBonds().add(addLinks(populate(iscsiBond, entity)));
        }

        return iscsiBonds;
    }
}

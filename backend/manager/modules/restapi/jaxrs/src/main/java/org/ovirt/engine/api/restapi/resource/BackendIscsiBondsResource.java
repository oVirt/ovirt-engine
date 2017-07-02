package org.ovirt.engine.api.restapi.resource;

import java.util.List;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.IscsiBond;
import org.ovirt.engine.api.model.IscsiBonds;
import org.ovirt.engine.api.resource.IscsiBondResource;
import org.ovirt.engine.api.resource.IscsiBondsResource;
import org.ovirt.engine.api.restapi.types.IscsiBondMapper;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.AddIscsiBondParameters;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendIscsiBondsResource extends AbstractBackendCollectionResource<IscsiBond, org.ovirt.engine.core.common.businessentities.IscsiBond>
        implements IscsiBondsResource {

    private Guid dataCenterId;

    protected BackendIscsiBondsResource(String datacenterId) {
        super(IscsiBond.class, org.ovirt.engine.core.common.businessentities.IscsiBond.class);
        this.dataCenterId = asGuid(datacenterId);
    }

    @Override
    public IscsiBonds list() {
        return mapCollection(
                getBackendCollection(QueryType.GetIscsiBondsByStoragePoolId, new IdQueryParameters(dataCenterId))
        );
    }

    @Override
    public Response add(IscsiBond iscsiBond) {
        validateParameters(iscsiBond, "name");
        org.ovirt.engine.core.common.businessentities.IscsiBond entity =
                getMapper(IscsiBond.class, org.ovirt.engine.core.common.businessentities.IscsiBond.class).map(iscsiBond, null);
        entity.setStoragePoolId(dataCenterId);

        return performCreate(ActionType.AddIscsiBond,
                new AddIscsiBondParameters(entity),
                new QueryIdResolver<Guid>(QueryType.GetIscsiBondById, IdQueryParameters.class));
    }

    @Override
    public IscsiBondResource getIscsiBondResource(String id) {
        return inject(new BackendIscsiBondResource(id));
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

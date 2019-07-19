package org.ovirt.engine.api.restapi.resource;

import java.util.function.Consumer;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.BaseResource;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.common.scheduling.AffinityGroup;
import org.ovirt.engine.core.common.scheduling.parameters.AffinityGroupCRUDParameters;
import org.ovirt.engine.core.compat.Guid;

public abstract class BackendAffinityGroupSubResource<R extends BaseResource, Q>
        extends AbstractBackendActionableResource<R, Q> {

    private final Guid affinityGroupId;

    public BackendAffinityGroupSubResource(Guid affinityGroupId, String id, Class<R> modelType, Class<Q> entityType) {
        super(id, modelType, entityType);
        this.affinityGroupId = affinityGroupId;
    }

    protected Response editAffinityGroup(Consumer<AffinityGroup> modifier) {
        AffinityGroup affinityGroup = getAffinityGroup();
        modifier.accept(affinityGroup);
        return performAction(ActionType.EditAffinityGroup,
                new AffinityGroupCRUDParameters(affinityGroup.getId(), affinityGroup));
    }

    private org.ovirt.engine.core.common.scheduling.AffinityGroup getAffinityGroup() {
        AffinityGroup affinityGroup = getEntity(AffinityGroup.class,
                QueryType.GetAffinityGroupById,
                new IdQueryParameters(affinityGroupId),
                affinityGroupId.toString());

        if (affinityGroup == null) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        return affinityGroup;
    }
}

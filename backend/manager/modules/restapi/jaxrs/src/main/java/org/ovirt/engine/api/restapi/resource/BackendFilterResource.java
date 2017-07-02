package org.ovirt.engine.api.restapi.resource;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Filter;
import org.ovirt.engine.api.resource.FilterResource;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.common.scheduling.ClusterPolicy;
import org.ovirt.engine.core.common.scheduling.parameters.ClusterPolicyCRUDParameters;
import org.ovirt.engine.core.compat.Guid;

public class BackendFilterResource
        extends AbstractBackendSubResource<Filter, ClusterPolicy>
        implements FilterResource {

    private Guid parentId;
    private final BackendFiltersResource parent;

    protected BackendFilterResource(String id, BackendFiltersResource parent) {
        super(id, Filter.class, ClusterPolicy.class);
        this.parent = parent;
        this.parentId = parent.schedulingPolicyId;
    }

    @Override
    public Filter get() {
        return performGet(QueryType.GetClusterPolicyById, new IdQueryParameters(parentId));
    }

    @Override
    public Response remove() {
        ClusterPolicy entity = parent.getClusterPolicy();
        updateEntityForRemove(entity, guid);
        return performAction(ActionType.EditClusterPolicy, new ClusterPolicyCRUDParameters(entity.getId(), entity));
    }

    private void updateEntityForRemove(ClusterPolicy entity, Guid id) {
        int i = 0;
        boolean found = false;
        if (entity.getFilters() == null) {
            return;
        }
        for (; i < entity.getFilters().size(); i++) {
            if (entity.getFilters().get(i).equals(id)) {
                found = true;
                break;
            }
        }
        if (found) {
            entity.getFilters().remove(i);
        }
    }
}

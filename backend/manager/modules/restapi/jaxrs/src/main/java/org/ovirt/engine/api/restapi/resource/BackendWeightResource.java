package org.ovirt.engine.api.restapi.resource;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Weight;
import org.ovirt.engine.api.resource.WeightResource;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.common.scheduling.ClusterPolicy;
import org.ovirt.engine.core.common.scheduling.parameters.ClusterPolicyCRUDParameters;
import org.ovirt.engine.core.compat.Guid;

public class BackendWeightResource
        extends AbstractBackendSubResource<Weight, ClusterPolicy>
        implements WeightResource {

    private Guid parentId;
    private final BackendWeightsResource parent;

    protected BackendWeightResource(String id, BackendWeightsResource parent) {
        super(id, Weight.class, ClusterPolicy.class);
        this.parent = parent;
        this.parentId = parent.schedulingPolicyId;
    }

    @Override
    public Weight get() {
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
        if (entity.getFunctions() == null) {
            return;
        }
        for (; i < entity.getFunctions().size(); i++) {
            if (entity.getFunctions().get(i).getFirst().equals(id)) {
                found = true;
                break;
            }
        }
        if (found) {
            entity.getFunctions().remove(i);
        }
    }
}

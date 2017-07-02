package org.ovirt.engine.api.restapi.resource;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Balance;
import org.ovirt.engine.api.resource.BalanceResource;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.common.scheduling.ClusterPolicy;
import org.ovirt.engine.core.common.scheduling.parameters.ClusterPolicyCRUDParameters;
import org.ovirt.engine.core.compat.Guid;

public class BackendBalanceResource
        extends AbstractBackendSubResource<Balance, ClusterPolicy>
        implements BalanceResource {

    private Guid parentId;
    private final BackendBalancesResource parent;

    protected BackendBalanceResource(String id, BackendBalancesResource parent) {
        super(id, Balance.class, ClusterPolicy.class);
        this.parent = parent;
        this.parentId = parent.schedulingPolicyId;
    }

    @Override
    public Response remove() {
        ClusterPolicy entity = parent.getClusterPolicy();
        updateEntityForRemove(entity, guid);
        return performAction(ActionType.EditClusterPolicy, new ClusterPolicyCRUDParameters(entity.getId(), entity));
    }

    @Override
    public Balance get() {
        return performGet(QueryType.GetClusterPolicyById, new IdQueryParameters(parentId));
    }

    private void updateEntityForRemove(ClusterPolicy entity, Guid id) {
        if (entity.getBalance() == null || !entity.getBalance().equals(id)) {
            return;
        }
        entity.setBalance(null);
    }
}

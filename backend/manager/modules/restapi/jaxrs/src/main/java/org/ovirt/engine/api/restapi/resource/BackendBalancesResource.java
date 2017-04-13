package org.ovirt.engine.api.restapi.resource;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Balance;
import org.ovirt.engine.api.model.Balances;
import org.ovirt.engine.api.resource.BalanceResource;
import org.ovirt.engine.api.resource.BalancesResource;
import org.ovirt.engine.api.restapi.resource.AbstractBackendSubResource.ParametersProvider;
import org.ovirt.engine.core.common.scheduling.ClusterPolicy;
import org.ovirt.engine.core.common.scheduling.parameters.ClusterPolicyCRUDParameters;
import org.ovirt.engine.core.compat.Guid;

public class BackendBalancesResource
        extends BackendPolicyUnitsResource<Balances, Balance>
        implements BalancesResource {

    protected BackendBalancesResource(Guid schedulingPolicyId) {
        super(schedulingPolicyId, Balance.class);
    }

    @Override
    public Balances list() {
        ClusterPolicy clusterPolicy = getClusterPolicy();
        Balances balances = new Balances();
        if (clusterPolicy.getBalance() != null) {
            Balance balance = new Balance();
            balance.setId(clusterPolicy.getBalance().toString());
            balances.getBalances().add(addLinks(map(clusterPolicy, balance)));
        }
        return balances;
    }

    @Override
    public BalanceResource getBalanceResource(String id) {
        return inject(new BackendBalanceResource(id, this));
    }

    @Override
    public Response add(Balance incoming) {
        return performAdd(incoming);
    }

    @Override
    protected ParametersProvider<Balance, ClusterPolicy> getAddParametersProvider() {
        return (model, entity) -> new ClusterPolicyCRUDParameters(entity.getId(), map(model, entity));
    }

    @Override
    protected void updateIncomingId(Balance incoming) {
        incoming.setId(incoming.getSchedulingPolicyUnit().getId());
    }
}

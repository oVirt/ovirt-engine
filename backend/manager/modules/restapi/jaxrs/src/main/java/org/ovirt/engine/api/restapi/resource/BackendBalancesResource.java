package org.ovirt.engine.api.restapi.resource;

import org.ovirt.engine.api.model.Balance;
import org.ovirt.engine.api.model.Balances;
import org.ovirt.engine.api.resource.BalanceResource;
import org.ovirt.engine.api.resource.BalancesResource;
import org.ovirt.engine.api.restapi.resource.AbstractBackendSubResource.ParametersProvider;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.scheduling.ClusterPolicy;
import org.ovirt.engine.core.common.scheduling.parameters.ClusterPolicyCRUDParameters;
import org.ovirt.engine.core.compat.Guid;

public class BackendBalancesResource extends BackendPolicyUnitsResource<Balances, Balance> implements BalancesResource {

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
            balances.setBalances(addLinks(map(clusterPolicy, balance)));
        }
        return balances;
    }

    @Override
    public BalanceResource getSubResource(String id) {
        return inject(new BackendBalanceResource(id, schedulingPolicyId));
    }

    @SingleEntityResource
    public BalanceResource getBalanceSubResource(String id) {
        return getSubResource(id);
    }

    @Override
    public Balance add(Balance incoming) {
        return performAdd(incoming);
    }

    @Override
    protected ParametersProvider<Balance, ClusterPolicy> getAddParametersProvider() {
        return new ParametersProvider<Balance, ClusterPolicy>() {
            @Override
            public VdcActionParametersBase getParameters(Balance model, ClusterPolicy entity) {
                return new ClusterPolicyCRUDParameters(entity.getId(), map(model, entity));
            }
        };
    }

    @Override
    protected void updateEntityForRemove(ClusterPolicy entity, Guid id) {
        if (entity.getBalance() == null || !entity.getBalance().equals(id)) {
            return;
        }
        entity.setBalance(null);
    }

    @Override
    protected void updateIncomingId(Balance incoming) {
        incoming.setId(incoming.getSchedulingPolicyUnit().getId());
    }

}

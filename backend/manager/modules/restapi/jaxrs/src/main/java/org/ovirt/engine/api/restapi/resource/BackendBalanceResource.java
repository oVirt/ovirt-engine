package org.ovirt.engine.api.restapi.resource;

import org.ovirt.engine.api.model.Balance;
import org.ovirt.engine.api.resource.BalanceResource;
import org.ovirt.engine.core.common.scheduling.ClusterPolicy;
import org.ovirt.engine.core.compat.Guid;

public class BackendBalanceResource extends BackendPolicyUnitResource<Balance> implements
        BalanceResource {

    protected BackendBalanceResource(String id, BackendBalancesResource parent) {
        super(id, parent, Balance.class);
    }

    @Override
    protected Balance createPolicyUnitByType() {
        Balance balance = new Balance();
        balance.setId(id);
        return balance;
    }

    @Override
    protected void updateEntityForRemove(ClusterPolicy entity, Guid id) {
        if (entity.getBalance() == null || !entity.getBalance().equals(id)) {
            return;
        }
        entity.setBalance(null);
    }
}

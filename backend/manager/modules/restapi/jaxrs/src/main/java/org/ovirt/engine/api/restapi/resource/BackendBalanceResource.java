package org.ovirt.engine.api.restapi.resource;

import org.ovirt.engine.api.model.Balance;
import org.ovirt.engine.api.resource.BalanceResource;
import org.ovirt.engine.core.compat.Guid;

public class BackendBalanceResource extends BackendPolicyUnitResource<Balance> implements
        BalanceResource {

    protected BackendBalanceResource(String id, Guid parentId) {
        super(id, parentId, Balance.class);
    }

    @Override
    protected Balance createPolicyUnitByType() {
        Balance balance = new Balance();
        balance.setId(id);
        return balance;
    }
}

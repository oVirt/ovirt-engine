package org.ovirt.engine.api.restapi.resource;

import org.ovirt.engine.api.model.Balance;
import org.ovirt.engine.api.resource.BalanceResource;

public class BackendBalanceResource extends BackendPolicyUnitResource<Balance> implements
        BalanceResource {

    protected BackendBalanceResource(String id) {
        super(id, Balance.class);
    }

}

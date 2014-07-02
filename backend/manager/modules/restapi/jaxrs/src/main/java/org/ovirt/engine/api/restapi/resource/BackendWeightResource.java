package org.ovirt.engine.api.restapi.resource;

import org.ovirt.engine.api.model.Weight;
import org.ovirt.engine.api.resource.WeightResource;
import org.ovirt.engine.core.compat.Guid;

public class BackendWeightResource extends BackendPolicyUnitResource<Weight> implements
        WeightResource {

    protected BackendWeightResource(String id, Guid parentId) {
        super(id, parentId, Weight.class);
    }

    @Override
    protected Weight createPolicyUnitByType() {
        Weight weight = new Weight();
        weight.setId(id);
        return weight;
    }
}

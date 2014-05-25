package org.ovirt.engine.api.restapi.resource;

import org.ovirt.engine.api.model.Weight;
import org.ovirt.engine.api.resource.WeightResource;

public class BackendWeightResource extends BackendPolicyUnitResource<Weight> implements
        WeightResource {

    protected BackendWeightResource(String id) {
        super(id, Weight.class);
    }

}

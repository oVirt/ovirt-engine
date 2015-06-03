package org.ovirt.engine.api.restapi.resource;

import org.ovirt.engine.api.model.Weight;
import org.ovirt.engine.api.resource.WeightResource;
import org.ovirt.engine.core.common.scheduling.ClusterPolicy;
import org.ovirt.engine.core.compat.Guid;

public class BackendWeightResource extends BackendPolicyUnitResource<Weight> implements
        WeightResource {

    protected BackendWeightResource(String id, BackendWeightsResource parent) {
        super(id, parent, Weight.class);
    }

    @Override
    protected Weight createPolicyUnitByType() {
        Weight weight = new Weight();
        weight.setId(id);
        return weight;
    }

    @Override
    protected void updateEntityForRemove(ClusterPolicy entity, Guid id) {
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

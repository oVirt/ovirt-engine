package org.ovirt.engine.api.restapi.resource;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Weight;
import org.ovirt.engine.api.model.Weights;
import org.ovirt.engine.api.resource.WeightResource;
import org.ovirt.engine.api.resource.WeightsResource;
import org.ovirt.engine.api.restapi.resource.AbstractBackendSubResource.ParametersProvider;
import org.ovirt.engine.core.common.scheduling.ClusterPolicy;
import org.ovirt.engine.core.common.scheduling.parameters.ClusterPolicyCRUDParameters;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;

public class BackendWeightsResource extends BackendPolicyUnitsResource<Weights, Weight> implements WeightsResource {

    protected BackendWeightsResource(Guid schedulingPolicyId) {
        super(schedulingPolicyId, Weight.class);
    }

    @Override
    public Weights list() {
        ClusterPolicy clusterPolicy = getClusterPolicy();
        Weights weights = new Weights();
        if (clusterPolicy.getFunctions() != null) {
            for (Pair<Guid, Integer> weightPair : clusterPolicy.getFunctions()) {
                Weight weight = new Weight();
                weight.setId(weightPair.getFirst().toString());
                weight.setFactor(weightPair.getSecond());
                weights.getWeights().add(addLinks(map(clusterPolicy, weight)));
            }
        }
        return weights;
    }

    @Override
    public WeightResource getWeightResource(String id) {
        return inject(new BackendWeightResource(id, this));
    }

    @Override
    public Response add(Weight incoming) {
        return performAdd(incoming);
    }

    @Override
    protected ParametersProvider<Weight, ClusterPolicy> getAddParametersProvider() {
        return (model, entity) -> new ClusterPolicyCRUDParameters(entity.getId(), map(model, entity));
    }

    @Override
    protected void updateIncomingId(Weight incoming) {
        incoming.setId(incoming.getSchedulingPolicyUnit().getId());
    }

}

package org.ovirt.engine.api.restapi.resource;

import java.util.List;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.SchedulingPolicyUnit;
import org.ovirt.engine.api.model.SchedulingPolicyUnits;
import org.ovirt.engine.api.resource.SchedulingPolicyUnitResource;
import org.ovirt.engine.api.resource.SchedulingPolicyUnitsResource;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.scheduling.PolicyUnit;
import org.ovirt.engine.core.common.scheduling.parameters.RemoveExternalPolicyUnitParameters;

public class BackendSchedulingPolicyUnitsResource extends AbstractBackendCollectionResource<SchedulingPolicyUnit, PolicyUnit> implements SchedulingPolicyUnitsResource {

    static final String[] SUB_COLLECTIONS = {};

    public BackendSchedulingPolicyUnitsResource() {
        super(SchedulingPolicyUnit.class, PolicyUnit.class, SUB_COLLECTIONS);
    }

    @Override
    public SchedulingPolicyUnits list() {
        SchedulingPolicyUnits schedulingPolicyUnits = new SchedulingPolicyUnits();
        for (PolicyUnit policyUnit : getCollection()) {
            schedulingPolicyUnits.getSchedulingPolicyUnits().add(addLinks(map(policyUnit)));
        }
        return schedulingPolicyUnits;
    }

    public List<PolicyUnit> getCollection() {
        return getBackendCollection(VdcQueryType.GetAllPolicyUnits, new VdcQueryParametersBase());
    }

    @Override
    protected Response performRemove(String id) {
        return performAction(VdcActionType.RemoveExternalPolicyUnit, new RemoveExternalPolicyUnitParameters(asGuid(id)));
    }

    @Override
    protected SchedulingPolicyUnit doPopulate(SchedulingPolicyUnit model, PolicyUnit entity) {
        return model;
    }

    @Override
    @SingleEntityResource
    public SchedulingPolicyUnitResource getSchedulingPolicyUnitSubResource(String id) {
        return inject(new BackendSchedulingPolicyUnitResource(id));
    }
}

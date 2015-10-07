package org.ovirt.engine.api.restapi.resource;

import java.util.List;

import org.ovirt.engine.api.model.SchedulingPolicyUnit;
import org.ovirt.engine.api.model.SchedulingPolicyUnits;
import org.ovirt.engine.api.resource.SchedulingPolicyUnitResource;
import org.ovirt.engine.api.resource.SchedulingPolicyUnitsResource;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.scheduling.PolicyUnit;

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
    public SchedulingPolicyUnitResource getUnitResource(String id) {
        return inject(new BackendSchedulingPolicyUnitResource(id));
    }
}

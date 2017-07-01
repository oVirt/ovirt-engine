package org.ovirt.engine.api.restapi.resource;

import java.util.List;

import org.ovirt.engine.api.model.SchedulingPolicyUnit;
import org.ovirt.engine.api.model.SchedulingPolicyUnits;
import org.ovirt.engine.api.resource.SchedulingPolicyUnitResource;
import org.ovirt.engine.api.resource.SchedulingPolicyUnitsResource;
import org.ovirt.engine.core.common.queries.QueryParametersBase;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.common.scheduling.PolicyUnit;

public class BackendSchedulingPolicyUnitsResource extends AbstractBackendCollectionResource<SchedulingPolicyUnit, PolicyUnit> implements SchedulingPolicyUnitsResource {

    public BackendSchedulingPolicyUnitsResource() {
        super(SchedulingPolicyUnit.class, PolicyUnit.class);
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
        return getBackendCollection(QueryType.GetAllPolicyUnits, new QueryParametersBase());
    }

    @Override
    public SchedulingPolicyUnitResource getUnitResource(String id) {
        return inject(new BackendSchedulingPolicyUnitResource(id));
    }
}

package org.ovirt.engine.api.restapi.resource;

import org.ovirt.engine.api.model.SchedulingPolicyUnit;
import org.ovirt.engine.api.resource.SchedulingPolicyUnitResource;
import org.ovirt.engine.core.common.scheduling.PolicyUnit;

public class BackendSchedulingPolicyUnitResource extends AbstractBackendSubResource<SchedulingPolicyUnit, PolicyUnit> implements
        SchedulingPolicyUnitResource {

    private static final String[] SUB_COLLECTIONS = {};
    private final PolicyUnit policyUnit;

    public BackendSchedulingPolicyUnitResource(String id, PolicyUnit policyUnit) {
        super(id, SchedulingPolicyUnit.class, PolicyUnit.class, SUB_COLLECTIONS);
        this.policyUnit = policyUnit;
    }

    @Override
    protected SchedulingPolicyUnit doPopulate(SchedulingPolicyUnit model, PolicyUnit entity) {
        return model;
    }

    @Override
    public SchedulingPolicyUnit get() {
        return map(policyUnit);
    }

}

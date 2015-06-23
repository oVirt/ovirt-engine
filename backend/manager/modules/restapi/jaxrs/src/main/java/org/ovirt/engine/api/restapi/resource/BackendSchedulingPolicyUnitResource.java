package org.ovirt.engine.api.restapi.resource;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.SchedulingPolicyUnit;
import org.ovirt.engine.api.resource.SchedulingPolicyUnitResource;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.scheduling.PolicyUnit;
import org.ovirt.engine.core.common.scheduling.parameters.RemoveExternalPolicyUnitParameters;

public class BackendSchedulingPolicyUnitResource extends AbstractBackendSubResource<SchedulingPolicyUnit, PolicyUnit> implements
        SchedulingPolicyUnitResource {

    private static final String[] SUB_COLLECTIONS = {};

    public BackendSchedulingPolicyUnitResource(String id) {
        super(id, SchedulingPolicyUnit.class, PolicyUnit.class, SUB_COLLECTIONS);
    }

    @Override
    public SchedulingPolicyUnit get() {
        return performGet(VdcQueryType.GetPolicyUnitById, new IdQueryParameters(guid));
    }

    @Override
    public Response remove() {
        get();
        return performAction(VdcActionType.RemoveExternalPolicyUnit, new RemoveExternalPolicyUnitParameters(guid));
    }

}

package org.ovirt.engine.core.bll.scheduling.queries;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.bll.scheduling.PolicyUnitImpl;
import org.ovirt.engine.core.bll.scheduling.SchedulingManager;
import org.ovirt.engine.core.common.queries.IdQueryParameters;

public class GetPolicyUnitByIdQuery extends QueriesCommandBase<IdQueryParameters> {
    public GetPolicyUnitByIdQuery(IdQueryParameters parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Inject
    private SchedulingManager schedulingManager;

    @Override
    protected void executeQueryCommand() {
        PolicyUnitImpl value = schedulingManager
                .getPolicyUnitsMap()
                .get(getParameters().getId());
        if (value != null) {
            getQueryReturnValue().setReturnValue(value.getPolicyUnit());
        }
    }
}

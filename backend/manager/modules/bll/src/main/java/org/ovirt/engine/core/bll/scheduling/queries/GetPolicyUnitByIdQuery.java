package org.ovirt.engine.core.bll.scheduling.queries;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.bll.scheduling.SchedulingManager;
import org.ovirt.engine.core.common.queries.IdQueryParameters;

public class GetPolicyUnitByIdQuery extends QueriesCommandBase<IdQueryParameters> {
    public GetPolicyUnitByIdQuery(IdQueryParameters parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(SchedulingManager.getInstance()
                .getPolicyUnitsMap()
                .get(getParameters().getId()));
    }
}

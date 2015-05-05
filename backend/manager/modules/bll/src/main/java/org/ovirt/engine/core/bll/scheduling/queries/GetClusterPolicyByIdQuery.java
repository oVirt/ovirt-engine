package org.ovirt.engine.core.bll.scheduling.queries;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.bll.scheduling.SchedulingManager;
import org.ovirt.engine.core.common.queries.IdQueryParameters;

public class GetClusterPolicyByIdQuery extends QueriesCommandBase<IdQueryParameters> {
    public GetClusterPolicyByIdQuery(IdQueryParameters parameters) {
        super(parameters);
    }

    @Inject
    private SchedulingManager schedulingManager;

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(schedulingManager.getClusterPolicy(getParameters().getId()));
    }
}

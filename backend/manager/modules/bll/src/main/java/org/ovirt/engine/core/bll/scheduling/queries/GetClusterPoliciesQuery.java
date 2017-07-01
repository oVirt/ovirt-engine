package org.ovirt.engine.core.bll.scheduling.queries;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.bll.scheduling.SchedulingManager;
import org.ovirt.engine.core.common.queries.QueryParametersBase;

public class GetClusterPoliciesQuery extends QueriesCommandBase<QueryParametersBase> {
    public GetClusterPoliciesQuery(QueryParametersBase parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Inject
    private SchedulingManager schedulingManager;

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(schedulingManager.getClusterPolicies());
    }
}

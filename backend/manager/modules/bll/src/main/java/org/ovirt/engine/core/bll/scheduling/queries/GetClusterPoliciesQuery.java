package org.ovirt.engine.core.bll.scheduling.queries;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.bll.scheduling.SchedulingManager;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;

public class GetClusterPoliciesQuery extends QueriesCommandBase<VdcQueryParametersBase> {
    public GetClusterPoliciesQuery(VdcQueryParametersBase parameters) {
        super(parameters);
    }

    @Inject
    private SchedulingManager schedulingManager;

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(schedulingManager.getClusterPolicies());
    }
}

package org.ovirt.engine.core.bll.scheduling.queries;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.bll.scheduling.PolicyUnitImpl;
import org.ovirt.engine.core.bll.scheduling.SchedulingManager;
import org.ovirt.engine.core.common.queries.QueryParametersBase;
import org.ovirt.engine.core.common.scheduling.PolicyUnit;
import org.ovirt.engine.core.compat.Guid;

public class GetAllPolicyUnitsQuery extends QueriesCommandBase<QueryParametersBase> {
    public GetAllPolicyUnitsQuery(QueryParametersBase parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Inject
    private SchedulingManager schedulingManager;

    @Override
    protected void executeQueryCommand() {
        Map<Guid, PolicyUnitImpl> map = schedulingManager.getPolicyUnitsMap();
        List<PolicyUnit> retList = new ArrayList<>();
        for (PolicyUnitImpl policyUnitImpl : map.values()) {
            retList.add(policyUnitImpl.getPolicyUnit());
        }
        getQueryReturnValue().setReturnValue(retList);
    }
}

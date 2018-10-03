package org.ovirt.engine.core.bll.scheduling.policyunits;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.bll.scheduling.PolicyUnitImpl;
import org.ovirt.engine.core.bll.scheduling.SchedulingUnit;
import org.ovirt.engine.core.bll.scheduling.external.BalanceResult;
import org.ovirt.engine.core.bll.scheduling.pending.PendingResourceManager;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.scheduling.PolicyUnit;
import org.ovirt.engine.core.common.scheduling.PolicyUnitType;

@SchedulingUnit(
        guid = "38440000-8cf0-14bd-c43e-10b96e4ef00a",
        name = "None",
        description = "No load balancing operation",
        type = PolicyUnitType.LOAD_BALANCING
)
public class NoneBalancePolicyUnit extends PolicyUnitImpl {

    public NoneBalancePolicyUnit(PolicyUnit policyUnit,
            PendingResourceManager pendingResourceManager) {
        super(policyUnit, pendingResourceManager);
    }

    @Override
    public List<BalanceResult> balance(Cluster cluster,
            List<VDS> hosts,
            Map<String, String> parameters) {
        return Collections.emptyList();
    }
}

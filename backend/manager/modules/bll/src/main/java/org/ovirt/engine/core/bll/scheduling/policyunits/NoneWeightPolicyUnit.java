package org.ovirt.engine.core.bll.scheduling.policyunits;

import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.bll.scheduling.SchedulingUnit;
import org.ovirt.engine.core.bll.scheduling.pending.PendingResourceManager;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.scheduling.PolicyUnit;
import org.ovirt.engine.core.common.scheduling.PolicyUnitType;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;

@SchedulingUnit(
        guid = "38440000-8cf0-14bd-c43e-10b96e4ef00b",
        name ="None",
        description = "Follows Even Distribution weight module",
        type = PolicyUnitType.WEIGHT
)
public class NoneWeightPolicyUnit extends EvenDistributionWeightPolicyUnit {

    public NoneWeightPolicyUnit(PolicyUnit policyUnit,
            PendingResourceManager pendingResourceManager) {
        super(policyUnit, pendingResourceManager);
    }

    @Override
    public List<Pair<Guid, Integer>> score(List<VDS> hosts, VM vm, Map<String, String> parameters) {
        return super.score(hosts, vm, parameters);
    }

}

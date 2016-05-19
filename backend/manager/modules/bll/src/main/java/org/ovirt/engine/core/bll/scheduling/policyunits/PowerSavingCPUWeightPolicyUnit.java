package org.ovirt.engine.core.bll.scheduling.policyunits;

import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.bll.scheduling.SchedulingUnit;
import org.ovirt.engine.core.bll.scheduling.pending.PendingResourceManager;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.scheduling.PolicyUnit;
import org.ovirt.engine.core.common.scheduling.PolicyUnitType;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;

@SchedulingUnit(
        guid = "736999d0-1023-46a4-9a75-1316ed50e15b",
        name = "OptimalForCpuPowerSaving",
        description = "Gives hosts with higher CPU usage, lower weight (means that hosts with higher CPU usage are"
                + " more likely to be selected)",
        type = PolicyUnitType.WEIGHT
)
public class PowerSavingCPUWeightPolicyUnit extends EvenDistributionCPUWeightPolicyUnit {

    public PowerSavingCPUWeightPolicyUnit(PolicyUnit policyUnit,
            PendingResourceManager pendingResourceManager) {
        super(policyUnit, pendingResourceManager);
    }

    @Override
    public List<Pair<Guid, Integer>> score(Cluster cluster, List<VDS> hosts, VM vm, Map<String, String> parameters) {
        return reverseEvenDistributionScore(cluster, hosts, vm, parameters);
    }
}

package org.ovirt.engine.core.bll.scheduling.policyunits;

import org.ovirt.engine.core.bll.scheduling.SchedulingUnit;
import org.ovirt.engine.core.bll.scheduling.pending.PendingResourceManager;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.scheduling.PolicyUnit;
import org.ovirt.engine.core.common.scheduling.PolicyUnitType;

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
    protected int calcHostScore(VDS vds, VM vm, boolean countThreadsAsCores) {
        int maxScore = MaxSchedulerWeight - 1;
        return maxScore - super.calcHostScore(vds, vm, countThreadsAsCores);
    }
}

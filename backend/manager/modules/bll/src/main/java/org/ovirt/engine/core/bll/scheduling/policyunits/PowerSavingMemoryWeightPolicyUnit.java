package org.ovirt.engine.core.bll.scheduling.policyunits;

import org.ovirt.engine.core.bll.scheduling.SchedulingUnit;
import org.ovirt.engine.core.bll.scheduling.pending.PendingResourceManager;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.scheduling.PolicyUnit;
import org.ovirt.engine.core.common.scheduling.PolicyUnitType;

@SchedulingUnit(
        guid = "9dfe6086-646d-43b8-8eef-4d94de8472c8",
        name = "OptimalForMemoryPowerSaving",
        description =
                "Gives hosts with lower available memory, lower weight (means that hosts with lower available memory are"
                        + " more likely to be selected)",
        type = PolicyUnitType.WEIGHT
)
public class PowerSavingMemoryWeightPolicyUnit extends EvenDistributionMemoryWeightPolicyUnit {

    public PowerSavingMemoryWeightPolicyUnit(PolicyUnit policyUnit,
            PendingResourceManager pendingResourceManager) {
        super(policyUnit, pendingResourceManager);
    }

    @Override
    protected int calcHostScore(float maxMemoryOfVdsInCluster, VDS vds) {
        int maxScore = MaxSchedulerWeight - 1;
        return maxScore - super.calcHostScore(maxMemoryOfVdsInCluster, vds);
    }
}

package org.ovirt.engine.core.bll.scheduling.policyunits;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.scheduling.PolicyUnitImpl;
import org.ovirt.engine.core.bll.scheduling.PolicyUnitParameter;
import org.ovirt.engine.core.bll.scheduling.SchedulingContext;
import org.ovirt.engine.core.bll.scheduling.SchedulingUnit;
import org.ovirt.engine.core.bll.scheduling.pending.PendingResourceManager;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.scheduling.PolicyUnit;
import org.ovirt.engine.core.common.scheduling.PolicyUnitType;
import org.ovirt.engine.core.common.scheduling.VmOverheadCalculator;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;

@SchedulingUnit(
        guid = "9dfe6086-646d-43b8-8eef-4d94de8472c8",
        name = "OptimalForMemoryPowerSaving",
        description =
                "Gives hosts with lower available memory, lower weight (means that hosts with lower available memory are"
                        + " more likely to be selected)",
        type = PolicyUnitType.WEIGHT,
        parameters = {
                PolicyUnitParameter.LOW_MEMORY_LIMIT_FOR_OVER_UTILIZED,
                PolicyUnitParameter.HIGH_MEMORY_LIMIT_FOR_UNDER_UTILIZED
        }
)
public class PowerSavingMemoryWeightPolicyUnit extends PolicyUnitImpl {

    @Inject
    private VmOverheadCalculator vmOverheadCalculator;

    public PowerSavingMemoryWeightPolicyUnit(PolicyUnit policyUnit,
            PendingResourceManager pendingResourceManager) {
        super(policyUnit, pendingResourceManager);
    }

    @Override
    public List<Pair<Guid, Integer>> score(SchedulingContext context, List<VDS> hosts, List<VM> vmGroup) {
        long lowMemoryLimit = context.getPolicyParameters().containsKey(PolicyUnitParameter.LOW_MEMORY_LIMIT_FOR_OVER_UTILIZED.getDbName()) ?
                Long.parseLong(context.getPolicyParameters().get(PolicyUnitParameter.LOW_MEMORY_LIMIT_FOR_OVER_UTILIZED.getDbName())) : 0L;

        long highMemoryLimit = context.getPolicyParameters().containsKey(PolicyUnitParameter.HIGH_MEMORY_LIMIT_FOR_UNDER_UTILIZED.getDbName()) ?
                Long.parseLong(context.getPolicyParameters().get(PolicyUnitParameter.HIGH_MEMORY_LIMIT_FOR_UNDER_UTILIZED.getDbName()))
                : Long.MAX_VALUE;

        /* The 'maxMemory' is set higher than the maximum of scheduling memory of all hosts.
           So overutilized hosts have worse score than the host with maximum free memory

           This is to handle for example the following case:
             - LOW_MEMORY_LIMIT_FOR_OVER_UTILIZED = 1 GB
             - HIGH_MEMORY_LIMIT_FOR_UNDER_UTILIZED = 4 GB
             - 3 hosts:
                A - MaxSchedulingMemory = 512 MB
                B - MaxSchedulingMemory = 2 GB
                C - MaxSchedulingMemory = 2 GB

             The scores are:
               A -> Is overutilized, so its score is computed as if: A.MaxSchedulingMemory == maxMemory
                    Score = MaxSchedulerWeight

               B -> Score = (1 / 1.1) * (MaxSchedulerWeight - 1) + 1
               C -> Score = (1 / 1.1) * (MaxSchedulerWeight - 1) + 1

           In case the 'maxMemory' was not higher than the maximum of scheduling memory of all hosts,
           the scores of hosts A, B, C would be identical. Which is not what we want.
           Host A has to be worse candidate than B or C.
        */
        float maxMemory = getMaxMemoryOfVdsInCluster(hosts) * 1.1f;

        List<Pair<Guid, Integer>> scores = new ArrayList<>();
        for (VDS vds : hosts) {
            int totalVmMemory = vmGroup.stream()
                    .filter(vm -> !vds.getId().equals(vm.getRunOnVds()))
                    .mapToInt(vm -> vmOverheadCalculator.getTotalRequiredMemMb(vm))
                    .sum();

            float hostSchedulingMem = vds.getMaxSchedulingMemory() - totalVmMemory;

            scores.add(new Pair<>(
                    vds.getId(),
                    calcHostScore(hostSchedulingMem, maxMemory, lowMemoryLimit, highMemoryLimit)
            ));
        }
        return scores;
    }

    private int calcHostScore(float hostSchedulingMem, float maxMemory, float lowMemoryLimit, float highMemoryLimit) {
        // If the free memory is lower than low limit, wrap it back to be higher than high limit
        // As a result hosts below low limit and above high limit have bad scores
        if (hostSchedulingMem < lowMemoryLimit) {
            float memUnderLimit = lowMemoryLimit - hostSchedulingMem;
            hostSchedulingMem = Math.min(maxMemory, highMemoryLimit + memUnderLimit);
        }

        // Scores are in the interval [1, MaxSchedulerWeight]
        return (int)((hostSchedulingMem / maxMemory) * (getMaxSchedulerWeight() - 1)) + 1;
    }

    private float getMaxMemoryOfVdsInCluster(List<VDS> hosts) {
        return hosts.stream().map(VDS::getMaxSchedulingMemory).max(Float::compareTo).orElse(1.0f);
    }
}

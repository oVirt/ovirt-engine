package org.ovirt.engine.core.bll.scheduling.policyunits;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.scheduling.PolicyUnitImpl;
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
        guid = "4134247a-9c58-4b9a-8593-530bb9e37c59",
        name = "OptimalForMemoryEvenDistribution",
        type = PolicyUnitType.WEIGHT,
        description =
                "Gives hosts with higher available memory, lower weight (means that hosts with more available memory are more"
                        + " likely to be selected)"
)
public class EvenDistributionMemoryWeightPolicyUnit extends PolicyUnitImpl {

    @Inject
    private VmOverheadCalculator vmOverheadCalculator;

    public EvenDistributionMemoryWeightPolicyUnit(PolicyUnit policyUnit,
            PendingResourceManager pendingResourceManager) {
        super(policyUnit, pendingResourceManager);
    }

    @Override
    public List<Pair<Guid, Integer>> score(SchedulingContext context, List<VDS> hosts, List<VM> vmGroup) {
        float maxMemoryOfVdsInCluster = getMaxMemoryOfVdsInCluster(hosts);
        List<Pair<Guid, Integer>> scores = new ArrayList<>();
        for (VDS vds : hosts) {
            scores.add(new Pair<>(vds.getId(), calcHostScore(maxMemoryOfVdsInCluster, vds, vmGroup)));
        }
        return scores;
    }

    /**
     * Calculate a single host weight score according to various parameters.
     *
     * @param vds                     host on which the score is calculated for
     * @param maxMemoryOfVdsInCluster maximum available memory for scheduling of a vds from all the available hosts
     * @return weight score for a single host
     */
    private int calcHostScore(float maxMemoryOfVdsInCluster, VDS vds, List<VM> vmGroup) {
        int totalVmMemory = vmGroup.stream()
                .filter(vm -> !vds.getId().equals(vm.getRunOnVds()))
                .mapToInt(vm -> vmOverheadCalculator.getTotalRequiredMemMb(vm))
                .sum();

        float hostSchedulingMem = vds.getMaxSchedulingMemory() - totalVmMemory;

        int score = Math.round(((hostSchedulingMem - 1) * (getMaxSchedulerWeight() - 1))
                / (maxMemoryOfVdsInCluster - 1));
        return getMaxSchedulerWeight() - score;
    }

    private float getMaxMemoryOfVdsInCluster(List<VDS> hosts) {
        return hosts.stream().map(VDS::getMaxSchedulingMemory).max(Float::compareTo).orElse(2.0f);
    }
}

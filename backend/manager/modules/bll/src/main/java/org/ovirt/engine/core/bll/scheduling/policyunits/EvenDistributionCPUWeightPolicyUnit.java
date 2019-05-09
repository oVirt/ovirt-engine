package org.ovirt.engine.core.bll.scheduling.policyunits;

import java.util.ArrayList;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.stream.Collectors;

import org.ovirt.engine.core.bll.scheduling.PolicyUnitImpl;
import org.ovirt.engine.core.bll.scheduling.SchedulingContext;
import org.ovirt.engine.core.bll.scheduling.SchedulingUnit;
import org.ovirt.engine.core.bll.scheduling.SlaValidator;
import org.ovirt.engine.core.bll.scheduling.pending.PendingCpuLoad;
import org.ovirt.engine.core.bll.scheduling.pending.PendingResourceManager;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VdsSpmStatus;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.scheduling.PolicyUnit;
import org.ovirt.engine.core.common.scheduling.PolicyUnitType;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;

@SchedulingUnit(
        guid = "7db4ab05-81ab-42e8-868a-aee2df483edb",
        name = "OptimalForCpuEvenDistribution",
        type = PolicyUnitType.WEIGHT,
        description = "Gives hosts with lower CPU usage, lower weight (means that hosts with lower CPU usage are more"
                + " likely to be selected)"
)
public class EvenDistributionCPUWeightPolicyUnit extends PolicyUnitImpl {

    public EvenDistributionCPUWeightPolicyUnit(PolicyUnit policyUnit,
            PendingResourceManager pendingResourceManager) {
        super(policyUnit, pendingResourceManager);
    }

    @Override
    public List<Pair<Guid, Integer>> score(SchedulingContext context, List<VDS> hosts, VM vm) {
        boolean countThreadsAsCores = context.getCluster().getCountThreadsAsCores();
        List<Pair<Guid, Integer>> scores = new ArrayList<>();
        List<Guid> hostsWithMaxScore = new ArrayList<>();
        for (VDS vds : hosts) {
            Integer effectiveCpuCores = SlaValidator.getEffectiveCpuCores(vds, countThreadsAsCores);
            if (effectiveCpuCores == null || vds.getUsageCpuPercent() == null) {
                hostsWithMaxScore.add(vds.getId());
                continue;
            }

            int score = (int)Math.round(calcHostLoadPerCore(vds, vm, effectiveCpuCores));
            scores.add(new Pair<>(vds.getId(), score));
        }

        stretchScores(scores);
        scores.addAll(hostsWithMaxScore.stream()
            .map(id -> new Pair<>(id, getMaxSchedulerWeight()))
            .collect(Collectors.toList()));

        return scores;
    }

    protected double calcHostLoadPerCore(VDS vds, VM vm, int hostCores) {
        return calcHostLoadPerCore(vds, vm, hostCores, null);
    }

    protected double calcHostLoadPerCore(VDS vds, VM vm, int hostCores, Integer hostLoad) {
        if (vds.getId().equals(vm.getRunOnVds())) {
            return vds.getUsageCpuPercent();
        }
        int vcpu = Config.<Integer>getValue(ConfigValues.VcpuConsumptionPercentage);
        hostLoad = hostLoad != null ? hostLoad : calcHostLoad(vds, hostCores, vcpu);

        // If the VM is running, use its current CPU load, otherwise use the config value
        double vmLoad = vm.getRunOnVds() != null && vm.getStatisticsData() != null  && vm.getUsageCpuPercent() != null ?
                vm.getUsageCpuPercent() * vm.getNumOfCpus() :
                vcpu * vm.getNumOfCpus();

        return (hostLoad + vmLoad) / hostCores;
    }

    protected int calcHostLoad(VDS host, int hostCores) {
        return calcHostLoad(host, hostCores, Config.<Integer>getValue(ConfigValues.VcpuConsumptionPercentage));
    }

    protected int calcHostLoad(VDS host, int hostCores, int vcpuLoadPerCore) {
        int spmCpu = (host.getSpmStatus() == VdsSpmStatus.None) ? 0 : Config
                .<Integer>getValue(ConfigValues.SpmVCpuConsumption);

        int hostLoad = host.getUsageCpuPercent() * hostCores;
        int pendingCpuLoad = PendingCpuLoad.collectForHost(getPendingResourceManager(), host.getId());

        return hostLoad + pendingCpuLoad + vcpuLoadPerCore * spmCpu;
    }

    protected void stretchScores(List<Pair<Guid, Integer>> scores) {
        if (scores.isEmpty()) {
            return;
        }

        IntSummaryStatistics stats = scores.stream().collect(Collectors.summarizingInt(Pair::getSecond));
        // Avoid division by 0
        if (stats.getMin() == stats.getMax()) {
            scores.forEach(p -> p.setSecond(1));
            return;
        }

        // Stretch the scores to fit to interval [1, maxSchedulerScore]
        for (Pair<Guid, Integer> pair : scores) {
            double coef = (double)(pair.getSecond() - stats.getMin()) / (double)(stats.getMax() - stats.getMin());
            int newScore = (int) Math.round(1 + coef * (getMaxSchedulerWeight() - 1));
            pair.setSecond(newScore);
        }
    }
}

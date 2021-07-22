package org.ovirt.engine.core.bll.scheduling.policyunits;

import java.util.ArrayList;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang.math.NumberUtils;
import org.ovirt.engine.core.bll.scheduling.PolicyUnitImpl;
import org.ovirt.engine.core.bll.scheduling.PolicyUnitParameter;
import org.ovirt.engine.core.bll.scheduling.SchedulingContext;
import org.ovirt.engine.core.bll.scheduling.SchedulingUnit;
import org.ovirt.engine.core.bll.scheduling.SlaValidator;
import org.ovirt.engine.core.bll.scheduling.pending.PendingCpuCores;
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
import org.ovirt.engine.core.common.utils.VmCpuCountHelper;
import org.ovirt.engine.core.compat.Guid;

@SchedulingUnit(
        guid = "7db4ab05-81ab-42e8-868a-aee2df483edb",
        name = "OptimalForCpuEvenDistribution",
        type = PolicyUnitType.WEIGHT,
        description = "Gives hosts with lower CPU usage, lower weight (means that hosts with lower CPU usage are more"
                + " likely to be selected)",
        parameters = PolicyUnitParameter.VCPU_TO_PHYSICAL_CPU_RATIO
)
public class EvenDistributionCPUWeightPolicyUnit extends PolicyUnitImpl {

    public EvenDistributionCPUWeightPolicyUnit(PolicyUnit policyUnit,
            PendingResourceManager pendingResourceManager) {
        super(policyUnit, pendingResourceManager);
    }

    @Override
    public List<Pair<Guid, Integer>> score(SchedulingContext context, List<VDS> hosts, List<VM> vmGroup) {
        double configuredVcpuRatio = NumberUtils.toDouble(context.getPolicyParameters().get(PolicyUnitParameter.VCPU_TO_PHYSICAL_CPU_RATIO.getDbName()), 0);
        boolean countThreadsAsCores = context.getCluster().getCountThreadsAsCores();
        List<Pair<Guid, Integer>> scores = new ArrayList<>();
        List<Guid> hostsWithMaxScore = new ArrayList<>();
        for (VDS vds : hosts) {
            Integer effectiveCpuCores = SlaValidator.getEffectiveCpuCores(vds, countThreadsAsCores);
            if (effectiveCpuCores == null || vds.getUsageCpuPercent() == null) {
                hostsWithMaxScore.add(vds.getId());
                continue;
            }

            int score = (int)Math.round(calcHostLoadPerCore(vds, vmGroup, effectiveCpuCores, configuredVcpuRatio));
            scores.add(new Pair<>(vds.getId(), score));
        }

        stretchScores(scores);
        scores.addAll(hostsWithMaxScore.stream()
            .map(id -> new Pair<>(id, getMaxSchedulerWeight()))
            .collect(Collectors.toList()));

        return scores;
    }

    protected double calcHostLoadPerCore(VDS vds, List<VM> vmGroup, int hostCores, double configuredVcpuRatio) {
        return calcHostLoadPerCore(vds, vmGroup, hostCores, null, configuredVcpuRatio);
    }

    protected double calcHostLoadPerCore(VDS vds, List<VM> vmGroup, int hostCores, Integer hostLoad, double configuredVcpuRatio) {
        int vcpu = Config.<Integer>getValue(ConfigValues.VcpuConsumptionPercentage);
        hostLoad = hostLoad != null ? hostLoad : calcHostLoad(vds, hostCores, vcpu);

        int addedVmLoad = vmGroup.stream()
                .filter(vm -> !vds.getId().equals(vm.getRunOnVds()))
                // If the VM is running, use its current CPU load, otherwise use the config value
                .mapToInt(vm -> vm.getRunOnVds() != null && vm.getStatisticsData() != null  && vm.getUsageCpuPercent() != null ?
                        vm.getUsageCpuPercent() * VmCpuCountHelper.getRuntimeNumOfCpu(vm, vds) :
                        vcpu * VmCpuCountHelper.getRuntimeNumOfCpu(vm, vds))
                .sum();

        double loadScore = (double)(hostLoad + addedVmLoad) / (double)hostCores;
        double vcpuPenalty = calculateVCpuPenalty(vds, vmGroup, hostCores, configuredVcpuRatio);
        return vcpuPenalty * loadScore;
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

    /**
     * If the virtual / physical threshold was specified and adding the VM on a host would
     * exceeded the threshold, punish the host by multiplying its score with the penalty > 0.
     *
     * @return 1 if the threshold is not specified or reached, otherwise 1000.
     */
    protected int calculateVCpuPenalty(VDS vds, List<VM> vmGroup, int hostCores, double configuredVcpuRatio) {
        if (configuredVcpuRatio == 0) {
            return 1;
        }

        int hostVCpuCount = vds.getVmsCoresCount() + PendingCpuCores.collectForHost(getPendingResourceManager(), vds.getId());
        int addedVCpuCount = vmGroup.stream()
                .filter(vm -> !vds.getId().equals(vm.getRunOnVds()))
                .mapToInt(vm -> vm.getNumOfCpus())
                .sum();
        double vcpuRatio = (double) (addedVCpuCount + hostVCpuCount) / (double) hostCores;

        return vcpuRatio < configuredVcpuRatio ? 1 : 1000;
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

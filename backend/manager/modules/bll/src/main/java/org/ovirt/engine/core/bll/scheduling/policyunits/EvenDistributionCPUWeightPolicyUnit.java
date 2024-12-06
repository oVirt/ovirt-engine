package org.ovirt.engine.core.bll.scheduling.policyunits;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.commons.lang.math.NumberUtils;
import org.ovirt.engine.core.bll.scheduling.PolicyUnitImpl;
import org.ovirt.engine.core.bll.scheduling.PolicyUnitParameter;
import org.ovirt.engine.core.bll.scheduling.SchedulingContext;
import org.ovirt.engine.core.bll.scheduling.SchedulingUnit;
import org.ovirt.engine.core.bll.scheduling.pending.PendingResourceManager;
import org.ovirt.engine.core.bll.scheduling.utils.HostCpuLoadHelper;
import org.ovirt.engine.core.bll.scheduling.utils.VdsCpuUnitPinningHelper;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.scheduling.PolicyUnit;
import org.ovirt.engine.core.common.scheduling.PolicyUnitType;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.utils.VmCpuCountHelper;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.vdsbroker.ResourceManager;

@SchedulingUnit(
        guid = "7db4ab05-81ab-42e8-868a-aee2df483edb",
        name = "OptimalForCpuEvenDistribution",
        type = PolicyUnitType.WEIGHT,
        description = "Gives hosts with lower CPU usage, lower weight (means that hosts with lower CPU usage are more"
                + " likely to be selected)",
        parameters = PolicyUnitParameter.VCPU_TO_PHYSICAL_CPU_RATIO
)
public class EvenDistributionCPUWeightPolicyUnit extends PolicyUnitImpl {

    @Inject
    protected ResourceManager resourceManager;

    @Inject
    protected VdsCpuUnitPinningHelper vdsCpuUnitPinningHelper;

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
            HostCpuLoadHelper cpuLoadHelper = new HostCpuLoadHelper(vds,
                    resourceManager,
                    vdsCpuUnitPinningHelper,
                    pendingResourceManager,
                    countThreadsAsCores);

            if (!cpuLoadHelper.hostStatisticsPresent()) {
                hostsWithMaxScore.add(vds.getId());
                continue;
            }

            int score = (int) Math.round(calcHostScore(vmGroup, cpuLoadHelper, configuredVcpuRatio));
            scores.add(new Pair<>(vds.getId(), score));
        }

        stretchScores(scores);
        scores.addAll(hostsWithMaxScore.stream()
            .map(id -> new Pair<>(id, getMaxSchedulerWeight()))
            .collect(Collectors.toList()));

        return scores;
    }

    protected double calcHostScore(List<VM> vmGroup, HostCpuLoadHelper cpuLoadHelper, double configuredVcpuRatio) {
        int hostSharedLoad = cpuLoadHelper.getEffectiveSharedCpuTotalLoad();
        int addedSharedVmLoad = calcAddedSharedVmsLoad(cpuLoadHelper.getHost(), vmGroup);
        int totalHostLoad = hostSharedLoad + addedSharedVmLoad;

        long hostSharedCpuCount = cpuLoadHelper.getEffectiveSharedPCpusCount();
        int addedExclusiveCpuCount = calcAddedVmsExclusiveCpusCount(vmGroup, cpuLoadHelper.getHost());
        long totalHostSharedCpus = hostSharedCpuCount - addedExclusiveCpuCount;

        double loadScore = totalHostLoad / (double) totalHostSharedCpus;
        double vcpuPenalty = calculateVCpuPenalty(vmGroup, cpuLoadHelper, configuredVcpuRatio);
        return vcpuPenalty * loadScore;
    }

    private int calcAddedSharedVmsLoad(VDS vds, List<VM> vmGroup) {
        int vcpuLoadPerCore = Config.<Integer>getValue(ConfigValues.VcpuConsumptionPercentage);
        int addedVmLoad = vmGroup.stream()
                .filter(vm -> !vds.getId().equals(vm.getRunOnVds()))
                .filter(vm -> !vm.getCpuPinningPolicy().isExclusive())
                // If the VM is running, use its current CPU load, otherwise use the config value
                .mapToInt(vm -> vm.getRunOnVds() != null && vm.getStatisticsData() != null && vm.getUsageCpuPercent() != null ?
                        vm.getUsageCpuPercent() * VmCpuCountHelper.getRuntimeNumOfCpu(vm, vds) :
                        vcpuLoadPerCore * VmCpuCountHelper.getRuntimeNumOfCpu(vm, vds))
                .sum();
        return addedVmLoad;
    }

    private int calcAddedVmsExclusiveCpusCount(List<VM> vmGroup, VDS host) {
        return vmGroup.stream()
                .filter(vm -> !host.getId().equals(vm.getRunOnVds()))
                .filter(vm -> vm.getCpuPinningPolicy().isExclusive())
                .mapToInt(vm -> vm.getNumOfCpus())
                .sum();
    }

    private int calcAddedVmsSharedCpusCount(List<VM> vmGroup, VDS host) {
        return vmGroup.stream()
                .filter(vm -> !host.getId().equals(vm.getRunOnVds()))
                .filter(vm -> !vm.getCpuPinningPolicy().isExclusive())
                .mapToInt(vm -> vm.getNumOfCpus())
                .sum();
    }

    /**
     * If the virtual / physical threshold was specified and adding the VM on a host would
     * exceeded the threshold, punish the host by multiplying its score with the penalty > 0.
     *
     * @return 1 if the threshold is not specified or not reached, otherwise 1000.
     */
    protected int calculateVCpuPenalty(List<VM> vmGroup, HostCpuLoadHelper cpuLoadHelper, double configuredVcpuRatio) {
        if (configuredVcpuRatio == 0) {
            return 1;
        }

        long hostSharedPCpuCount = cpuLoadHelper.getEffectiveSharedPCpusCount();
        int addedExclusivelyPinnedPCpuCount = calcAddedVmsExclusiveCpusCount(vmGroup, cpuLoadHelper.getHost());

        int hostSharedVCpuCount = cpuLoadHelper.getEffectiveVmsSharedCpusCount();
        int addedSharedVCpuCount = calcAddedVmsSharedCpusCount(vmGroup, cpuLoadHelper.getHost());

        double vcpuRatio = (double) (addedSharedVCpuCount + hostSharedVCpuCount)
                / (double) (hostSharedPCpuCount - addedExclusivelyPinnedPCpuCount);

        return vcpuRatio < configuredVcpuRatio ? 1 : 1000;
    }
}

package org.ovirt.engine.core.bll.scheduling.policyunits;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.scheduling.PolicyUnitImpl;
import org.ovirt.engine.core.bll.scheduling.SchedulingContext;
import org.ovirt.engine.core.bll.scheduling.SchedulingUnit;
import org.ovirt.engine.core.bll.scheduling.pending.PendingResourceManager;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmType;
import org.ovirt.engine.core.common.scheduling.PolicyUnit;
import org.ovirt.engine.core.common.scheduling.PolicyUnitType;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.utils.VmCpuCountHelper;
import org.ovirt.engine.core.compat.Guid;

@SchedulingUnit(
        guid = "71931e14-f0e6-4f91-a7c8-d494a26e3a09",
        name = "CPU for high performance VMs",
        type = PolicyUnitType.WEIGHT,
        description = "Prefers hosts that have more or equal number of sockets, cores and threads."
)
public class HighPerformanceCpuPolicyUnit extends PolicyUnitImpl {

    public HighPerformanceCpuPolicyUnit(PolicyUnit policyUnit, PendingResourceManager pendingResourceManager) {
        super(policyUnit, pendingResourceManager);
    }

    @Override
    public List<Pair<Guid, Integer>> score(SchedulingContext context, List<VDS> hosts, VM vm) {
        return hosts.stream()
                .map(host -> new Pair<>(host.getId(), hostScore(vm, host)))
                .collect(Collectors.toList());
    }

    private int hostScore(VM vm, VDS host) {
        if (!policyUnitEnabled(vm)) {
            return 1;
        }

        int hostCoresPerSocket = host.getCpuCores() / host.getCpuSockets();
        int hostThreadsPerCore = host.getCpuThreads() / host.getCpuCores();

        if (vm.getNumOfSockets() <= host.getCpuSockets() &&
                vm.getCpuPerSocket() <= hostCoresPerSocket &&
                vm.getThreadsPerCpu() <= hostThreadsPerCore) {
            return 1;
        }

        return getMaxSchedulerWeight();
    }

    /**
     * This policy unit should be enabled for VMs that have host-specific configuration.
     * This includes CPU pinning, CPU pass-through and NUMA node pinning.
     */
    private boolean policyUnitEnabled(VM vm) {
        if (vm.getVmType() == VmType.HighPerformance ||
                vm.isUsingCpuPassthrough() ||
                StringUtils.isNotEmpty(VmCpuCountHelper.isAutoPinning(vm) ? vm.getCurrentCpuPinning() : vm.getCpuPinning())) {
            return true;
        }

        // Test if VM uses NUMA pinning
        return vm.getvNumaNodeList().stream()
                .anyMatch(node -> !node.getVdsNumaNodeList().isEmpty());
    }
}

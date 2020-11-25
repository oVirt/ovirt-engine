package org.ovirt.engine.core.bll.scheduling.policyunits;

import java.util.List;
import java.util.stream.Collectors;

import org.ovirt.engine.core.bll.scheduling.PolicyUnitImpl;
import org.ovirt.engine.core.bll.scheduling.SchedulingContext;
import org.ovirt.engine.core.bll.scheduling.SchedulingUnit;
import org.ovirt.engine.core.bll.scheduling.pending.PendingResourceManager;
import org.ovirt.engine.core.bll.scheduling.utils.NumaPinningHelper;
import org.ovirt.engine.core.common.businessentities.NumaTuneMode;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VdsNumaNode;
import org.ovirt.engine.core.common.businessentities.VmNumaNode;
import org.ovirt.engine.core.common.scheduling.PolicyUnit;
import org.ovirt.engine.core.common.scheduling.PolicyUnitType;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;

@SchedulingUnit(
        guid = "1b14ac11-20e9-4593-a149-2eb83c60a330",
        name = "CPU and NUMA pinning compatibility",
        description = "Prefers hosts where CPU pinning is compatible with NUMA node pinning",
        type = PolicyUnitType.WEIGHT
)
public class CpuAndNumaPinningWeightPolicyUnit extends PolicyUnitImpl {

    public CpuAndNumaPinningWeightPolicyUnit(PolicyUnit policyUnit,
            PendingResourceManager pendingResourceManager) {
        super(policyUnit, pendingResourceManager);
    }

    @Override
    public List<Pair<Guid, Integer>> score(SchedulingContext context, List<VDS> hosts, List<VM> vmGroup) {
        List<VM> vmsToCheck = vmGroup.stream()
                // If the NUMA mode is PREFERRED, a host with any NUMA configuration is accepted.
                .filter(vm -> vm.getvNumaNodeList().stream().map(VmNumaNode::getNumaTuneMode)
                        .allMatch(tune -> tune != NumaTuneMode.PREFERRED))
                .filter(vm -> !vm.getvNumaNodeList().isEmpty())
                .collect(Collectors.toList());

        boolean vmNumaPinned = vmsToCheck.stream()
                .flatMap(vm -> vm.getvNumaNodeList().stream())
                .anyMatch(node -> !node.getVdsNumaNodeList().isEmpty());

        // If no VM numa node is pinned, all hosts have the same score
        // The condition is also true if no VMs use NUMA
        if (!vmNumaPinned) {
            return hosts.stream()
                    .map(host -> new Pair<>(host.getId(), 1))
                    .collect(Collectors.toList());
        }

        return hosts.stream()
                .map(h -> new Pair<>(h.getId(), hostScore(h, vmsToCheck)))
                .collect(Collectors.toList());
    }

    private Integer hostScore(VDS host, List<VM> vms) {
        if (!host.isNumaSupport()) {
            return getMaxSchedulerWeight();
        }

        List<VM> vmsToCheckOnHost = vms.stream()
                .filter(vm -> !host.getId().equals(vm.getRunOnVds()))
                .collect(Collectors.toList());

        if (vmsToCheckOnHost.isEmpty()) {
            return 1;
        }

        List<VdsNumaNode> hostNodes = host.getNumaNodeList();
        return NumaPinningHelper.findAssignment(vmsToCheckOnHost, hostNodes, true).isPresent() ?
                1 :
                getMaxSchedulerWeight();
    }
}

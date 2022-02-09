package org.ovirt.engine.core.bll.scheduling.policyunits;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.ovirt.engine.core.bll.scheduling.PolicyUnitImpl;
import org.ovirt.engine.core.bll.scheduling.SchedulingContext;
import org.ovirt.engine.core.bll.scheduling.SchedulingUnit;
import org.ovirt.engine.core.bll.scheduling.pending.PendingResourceManager;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.scheduling.PolicyUnit;
import org.ovirt.engine.core.common.scheduling.PolicyUnitType;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.utils.VmCpuCountHelper;
import org.ovirt.engine.core.compat.Guid;

@SchedulingUnit(
        guid = "f58c1cb9-d91f-48a6-a196-c6d22fb10c4e",
        name = "Fit VM to single host NUMA node",
        description = "Prefers hosts where a VM without vNUMA can fit in a single host NUMA node.",
        type = PolicyUnitType.WEIGHT
)
public class NumaWeightPolicyUnit extends PolicyUnitImpl {

    public NumaWeightPolicyUnit(PolicyUnit policyUnit,
            PendingResourceManager pendingResourceManager) {
        super(policyUnit, pendingResourceManager);
    }

    /**
     * Assigns score for each host. The score is only binary, either it is 1 or maxSchedulerWeight.
     *
     * This binary scoring was chosen, because using score that depends on the number of host NUMA nodes
     * where the VM can fit would cause undesired behavior where hosts with less NUMA nodes would not be
     * chosen. For examle, if there are 10 hosts, one has 8 NUMA nodes and the rest have 16 nodes,
     * the VM would not be scheduled on the host with 8 nodes, unless all the hosts with 16 nodes are filtered out
     * by filter policy units.
     */
    @Override
    public List<Pair<Guid, Integer>> score(SchedulingContext context, List<VDS> hosts, VM vm) {
        // This unit only applies to VMs without NUMA nodes.
        // VMs with NUMA nodes should use NUMA pinning to have good performance.
        if (!vm.getvNumaNodeList().isEmpty()) {
            return hosts.stream()
                    .map(host -> new Pair<>(host.getId(), 1))
                    .collect(Collectors.toList());
        }

        List<Pair<Guid, Integer>> scores = new ArrayList<>();
        for (VDS host: hosts) {
            int score = 1;
            if (host.isNumaSupport() && !doesVmFitSomeNumaNode(vm, host)) {
                score = getMaxSchedulerWeight();
            }

            scores.add(new Pair<>(host.getId(), score));
        }

        return scores;
    }

    private boolean doesVmFitSomeNumaNode(VM vm, VDS host) {
        int numOfCpus = VmCpuCountHelper.getRuntimeNumOfCpu(vm, host);
        return host.getNumaNodeList().stream()
                .filter(node -> vm.getMemSizeMb() <= node.getMemTotal())
                .anyMatch(node -> numOfCpus <= node.getCpuIds().size());
    }
}

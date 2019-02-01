package org.ovirt.engine.core.bll.scheduling.policyunits;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.scheduling.PolicyUnitImpl;
import org.ovirt.engine.core.bll.scheduling.SchedulingContext;
import org.ovirt.engine.core.bll.scheduling.SchedulingUnit;
import org.ovirt.engine.core.bll.scheduling.pending.PendingResourceManager;
import org.ovirt.engine.core.bll.scheduling.utils.CpuPinningHelper;
import org.ovirt.engine.core.bll.scheduling.utils.NumaPinningHelper;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VdsNumaNode;
import org.ovirt.engine.core.common.businessentities.VmNumaNode;
import org.ovirt.engine.core.common.scheduling.PolicyUnit;
import org.ovirt.engine.core.common.scheduling.PolicyUnitType;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VdsNumaNodeDao;
import org.ovirt.engine.core.dao.VmNumaNodeDao;

@SchedulingUnit(
        guid = "1b14ac11-20e9-4593-a149-2eb83c60a330",
        name = "CPU and NUMA pinning compatibility",
        description = "Prefers hosts where CPU pinning is compatible with NUMA node pinning",
        type = PolicyUnitType.WEIGHT
)
public class CpuAndNumaPinningWeightPolicyUnit extends PolicyUnitImpl {

    @Inject
    private VmNumaNodeDao vmNumaNodeDao;
    @Inject
    private VdsNumaNodeDao vdsNumaNodeDao;

    public CpuAndNumaPinningWeightPolicyUnit(PolicyUnit policyUnit,
            PendingResourceManager pendingResourceManager) {
        super(policyUnit, pendingResourceManager);
    }

    @Override
    public List<Pair<Guid, Integer>> score(SchedulingContext context, List<VDS> hosts, VM vm) {
        List<VmNumaNode> vmNumaNodes = vmNumaNodeDao.getAllVmNumaNodeByVmId(vm.getId());

        boolean vmNumaPinned = vmNumaNodes.stream()
                .anyMatch(node -> !node.getVdsNumaNodeList().isEmpty());

        // If no VM numa node is pinned, all hosts have the same score
        if (!vmNumaPinned) {
            return hosts.stream()
                    .map(host -> new Pair<>(host.getId(), 1))
                    .collect(Collectors.toList());
        }

        Map<Integer, Collection<Integer>> cpuPinning = CpuPinningHelper.parseCpuPinning(vm.getCpuPinning()).stream()
                .collect(Collectors.toMap(p -> p.getvCpu(), p -> p.getpCpus()));

        if (cpuPinning.isEmpty()) {
            return hosts.stream()
                    .map(host -> new Pair<>(host.getId(), 1))
                    .collect(Collectors.toList());
        }

        return hosts.stream()
                .map(h -> new Pair<>(h.getId(), hostScore(h, vmNumaNodes, cpuPinning)))
                .collect(Collectors.toList());
    }

    private Integer hostScore(VDS host, List<VmNumaNode> vmNumaNodes, Map<Integer, Collection<Integer>> cpuPinning) {
        List<VdsNumaNode> hostNodes = vdsNumaNodeDao.getAllVdsNumaNodeByVdsId(host.getId());
        return NumaPinningHelper.findAssignment(vmNumaNodes, hostNodes, cpuPinning).isPresent() ?
                1 :
                getMaxSchedulerWeight();
    }
}

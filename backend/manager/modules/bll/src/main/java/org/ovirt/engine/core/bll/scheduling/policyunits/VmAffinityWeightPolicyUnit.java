package org.ovirt.engine.core.bll.scheduling.policyunits;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.scheduling.PolicyUnitImpl;
import org.ovirt.engine.core.bll.scheduling.SchedulingContext;
import org.ovirt.engine.core.bll.scheduling.SchedulingUnit;
import org.ovirt.engine.core.bll.scheduling.pending.PendingResourceManager;
import org.ovirt.engine.core.bll.scheduling.pending.PendingVM;
import org.ovirt.engine.core.common.businessentities.MigrationSupport;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.scheduling.AffinityGroup;
import org.ovirt.engine.core.common.scheduling.PolicyUnit;
import org.ovirt.engine.core.common.scheduling.PolicyUnitType;
import org.ovirt.engine.core.common.utils.ListUtils;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.dao.scheduling.AffinityGroupDao;

@SchedulingUnit(
        guid = "84e6ddee-ab0d-42dd-82f0-c297779db567",
        name = "VmAffinityGroups",
        description = "Enables Affinity Groups soft enforcement for VMs; VMs in group are most likely to run either"
                + " on the same hypervisor host (positive) or on independent hypervisor hosts (negative)",
        type = PolicyUnitType.WEIGHT
)
public class VmAffinityWeightPolicyUnit extends PolicyUnitImpl {
    private static final int DEFAULT_SCORE = 1;

    @Inject
    private AffinityGroupDao affinityGroupDao;
    @Inject
    private VmDao vmDao;

    public VmAffinityWeightPolicyUnit(PolicyUnit policyUnit,
            PendingResourceManager pendingResourceManager) {
        super(policyUnit, pendingResourceManager);

    }

    @Override
    public List<Pair<Guid, Integer>> score(SchedulingContext context, List<VDS> hosts, List<VM> vmGroup) {
        List<HostInfo> hostInfos = getHostInfos(vmGroup, hosts);

        // Using lexicographicListComparator to sort priorities of broken groups.
        // So that a host that breaks any number of affinity groups with low priority will
        // be sorted before a host that breaks one high priority group.
        //
        // For example:
        // - Host A - breaks groups with priority: 1, 1, 1
        // - Host B - breaks group with priority: 2
        //
        // Host A will be sorted before Host B.

        Comparator<HostInfo> hostInfoComparator = Comparator.comparing(HostInfo::getPrioritiesOfBrokenGroups,
                ListUtils.lexicographicListComparator())
                .thenComparing(HostInfo::getNonmigratableVmCount, Comparator.reverseOrder())
                .thenComparing(HostInfo::getVmCount, Comparator.reverseOrder());

        hostInfos.sort(hostInfoComparator);

        List<Pair<Guid, Integer>> retList = new ArrayList<>(hostInfos.size());
        ListUtils.forEachWithRanks(hostInfos, hostInfoComparator, (hostInfo, rank) -> {
            retList.add(new Pair<>(hostInfo.getHostId(), DEFAULT_SCORE + rank));
        });

        return retList;
    }

    private List<HostInfo> getHostInfos(List<VM> vmGroup, List<VDS> hosts) {
        Set<AffinityGroup> affinityGroups = new HashSet<>();
        // TODO - get all affinity gorups in 1 DB call
        vmGroup.forEach(vm -> affinityGroups.addAll(affinityGroupDao.getAllAffinityGroupsWithFlatLabelsByVmId(vm.getId()).stream()
                .filter(AffinityGroup::isVmAffinityEnabled)
                .collect(Collectors.toList())));

        if (affinityGroups.isEmpty()) {
            return hosts.stream()
                    .map(h -> new HostInfo(h.getId(), Collections.emptyList(), 0, 0))
                    .collect(Collectors.toList());
        }

        // Get all running VMs in cluster
        Map<Guid, VM> runningVMsMap = vmDao.getAllRunningByCluster(vmGroup.get(0).getClusterId()).stream()
                .collect(Collectors.toMap(VM::getId, vm -> vm));

        // Update the VM list with pending VMs
        for (PendingVM resource: pendingResourceManager.pendingResources(PendingVM.class)) {
            VM pendingVm = new VM();
            pendingVm.setId(resource.getVm());
            pendingVm.setRunOnVds(resource.getHost());
            runningVMsMap.put(pendingVm.getId(), pendingVm);
        }

        Set<Guid> vmIdSet = vmGroup.stream()
                .map(VM::getId)
                .collect(Collectors.toSet());

        Map<Guid, Set<VM>> vmsInPositiveAffinityOnHosts = new HashMap<>();
        Map<Guid, List<Long>> prioritiesOfBrokenGroupsForHost = new HashMap<>();

        for (AffinityGroup group : affinityGroups) {
            Map<Guid, List<VM>> hostsWithVmsInAffinityGroup = group.getVmIds().stream()
                    .filter(id -> !vmIdSet.contains(id))
                    .map(runningVMsMap::get)
                    .filter(Objects::nonNull)
                    .collect(Collectors.groupingBy(VM::getRunOnVds));

            if (group.isVmPositive()) {
                hostsWithVmsInAffinityGroup.forEach((hostId, vmList) ->
                        vmsInPositiveAffinityOnHosts.computeIfAbsent(hostId, k -> new HashSet<>()).addAll(vmList)
                );

                // Hosts not in the set break this affinity group
                List<Guid> hostsBreakingAffinity = hosts.stream()
                        .map(VDS::getId)
                        .filter(id -> !hostsWithVmsInAffinityGroup.containsKey(id))
                        .collect(Collectors.toList());

                hostsBreakingAffinity.forEach(id ->
                        prioritiesOfBrokenGroupsForHost.computeIfAbsent(id, k -> new ArrayList<>()).add(group.getPriority())
                );
            } else {
                // Hosts in the set break this affinity group
                hostsWithVmsInAffinityGroup.keySet().forEach(id ->
                        prioritiesOfBrokenGroupsForHost.computeIfAbsent(id, k -> new ArrayList<>()).add(group.getPriority())
                );
            }
        }

        List<HostInfo> result = new ArrayList<>();
        for (VDS host : hosts) {
            Guid hostId = host.getId();
            Set<VM> vmsForHost = vmsInPositiveAffinityOnHosts.getOrDefault(hostId, Collections.emptySet());

            long vmCount = vmsForHost.size();
            long nonmigratableVmCount = vmsForHost.stream()
                    .filter(vm -> !isVmMigratable(vm))
                    .count();

            List<Long> priorities = prioritiesOfBrokenGroupsForHost.getOrDefault(hostId, Collections.emptyList());
            priorities.sort(Comparator.reverseOrder());

            result.add(new HostInfo(hostId, priorities, (int) vmCount, (int) nonmigratableVmCount));
        }

        return result;
    }

    private static boolean isVmMigratable(VM vm) {
        return (vm.getMigrationSupport() == MigrationSupport.MIGRATABLE) && !vm.isHostedEngine();
    }

    private static class HostInfo {
        private final Guid hostId;
        private final List<Long> prioritiesOfBrokenGroups;
        private final int vmCount;
        private final int nonmigratableVmCount;

        public HostInfo(Guid hostId, List<Long> prioritiesOfBrokenGroups, int vmCount, int nonmigratableVmCount) {
            this.hostId = hostId;
            this.prioritiesOfBrokenGroups = prioritiesOfBrokenGroups;
            this.vmCount = vmCount;
            this.nonmigratableVmCount = nonmigratableVmCount;
        }

        public Guid getHostId() {
            return hostId;
        }

        public List<Long> getPrioritiesOfBrokenGroups() {
            return prioritiesOfBrokenGroups;
        }

        public int getVmCount() {
            return vmCount;
        }

        public int getNonmigratableVmCount() {
            return nonmigratableVmCount;
        }
    }
}

package org.ovirt.engine.core.bll.scheduling.policyunits;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.bll.scheduling.SchedulingContext;
import org.ovirt.engine.core.bll.scheduling.SchedulingUnit;
import org.ovirt.engine.core.bll.scheduling.pending.PendingResourceManager;
import org.ovirt.engine.core.common.businessentities.MigrationSupport;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.scheduling.PerHostMessages;
import org.ovirt.engine.core.common.scheduling.PolicyUnit;
import org.ovirt.engine.core.common.scheduling.PolicyUnitType;
import org.ovirt.engine.core.common.utils.ListUtils;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;

@SchedulingUnit(
        guid = "84e6ddee-ab0d-42dd-82f0-c297779db567",
        name = "VmAffinityGroups",
        description = "Enables Affinity Groups soft enforcement for VMs; VMs in group are most likely to run either"
                + " on the same hypervisor host (positive) or on independent hypervisor hosts (negative)",
        type = PolicyUnitType.WEIGHT
)
public class VmAffinityWeightPolicyUnit extends VmAffinityPolicyUnit {
    private static final int DEFAULT_SCORE = 1;

    public VmAffinityWeightPolicyUnit(PolicyUnit policyUnit,
            PendingResourceManager pendingResourceManager) {
        super(policyUnit, pendingResourceManager);

    }

    @Override
    public List<Pair<Guid, Integer>> score(SchedulingContext context, List<VDS> hosts, List<VM> vmGroup) {
        Map<Guid, List<VM>> acceptableHostsWithVms = getAcceptableHostsWithVms(false,
                hosts,
                vmGroup,
                new PerHostMessages());

        Map<Guid, Long> nonMigratebleVmsPerHost = new HashMap<>();
        acceptableHostsWithVms.forEach((hostId, vms) -> {
            nonMigratebleVmsPerHost.put(hostId, vms.stream()
                    .filter(v -> !isVmMigratable(v))
                    .count());
        });

        List<Pair<Guid, Integer>> retList = new ArrayList<>();

        List<HostWithVmCounts> hostsWithVmCounts = new ArrayList<>();
        for (VDS host : hosts) {
            if (!acceptableHostsWithVms.containsKey(host.getId())) {
                retList.add(new Pair<>(host.getId(), getMaxSchedulerWeight()));
                continue;
            }

            long vmCount = acceptableHostsWithVms.get(host.getId()).size();
            long nonmigratableVmCount = nonMigratebleVmsPerHost.get(host.getId());
            hostsWithVmCounts.add(new HostWithVmCounts(host.getId(), (int) vmCount, (int) nonmigratableVmCount));
        }

        Comparator<HostWithVmCounts> comparator = Comparator.comparingInt(HostWithVmCounts::getNonmigratableVmCount)
                .thenComparingInt(HostWithVmCounts::getVmCount)
                .reversed();

        hostsWithVmCounts.sort(comparator);
        List<Integer> ranks = ListUtils.rankSorted(hostsWithVmCounts, comparator);

        for (int i = 0; i < hostsWithVmCounts.size(); i++) {
            Guid hostId = hostsWithVmCounts.get(i).getHostId();
            Integer rank = ranks.get(i);
            retList.add(new Pair<>(hostId, DEFAULT_SCORE + rank));
        }

        return retList;
    }

    private static boolean isVmMigratable(VM vm) {
        return (vm.getMigrationSupport() == MigrationSupport.MIGRATABLE) && !vm.isHostedEngine();
    }

    private static class HostWithVmCounts {
        private final Guid hostId;
        private final int vmCount;
        private final int nonmigratableVmCount;

        public HostWithVmCounts(Guid hostId, int vmCount, int nonmigratableVmCount) {
            this.hostId = hostId;
            this.vmCount = vmCount;
            this.nonmigratableVmCount = nonmigratableVmCount;
        }

        public Guid getHostId() {
            return hostId;
        }

        public int getVmCount() {
            return vmCount;
        }

        public int getNonmigratableVmCount() {
            return nonmigratableVmCount;
        }
    }
}

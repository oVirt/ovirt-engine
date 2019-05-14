package org.ovirt.engine.core.bll.scheduling.policyunits;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.scheduling.PolicyUnitImpl;
import org.ovirt.engine.core.bll.scheduling.SchedulingContext;
import org.ovirt.engine.core.bll.scheduling.SchedulingUnit;
import org.ovirt.engine.core.bll.scheduling.pending.PendingResourceManager;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.scheduling.AffinityGroup;
import org.ovirt.engine.core.common.scheduling.PolicyUnit;
import org.ovirt.engine.core.common.scheduling.PolicyUnitType;
import org.ovirt.engine.core.common.utils.ListUtils;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.scheduling.AffinityGroupDao;

@SchedulingUnit(
        guid = "427aed70-dae3-48ba-8fe9-a902a9d563c8",
        name = "VmToHostsAffinityGroups",
        description = "Enables Affinity Groups soft enforcement for VMs to hosts;"
                + " VMs in group are most likely to run either on one of the hosts in group (positive)"
                + " or on independent hosts which are excluded from the hosts in group (negative)",
        type = PolicyUnitType.WEIGHT
)
public class VmToHostAffinityWeightPolicyUnit extends PolicyUnitImpl {

    private static final int DEFAULT_SCORE = 1;

    @Inject
    private AffinityGroupDao affinityGroupDao;

    public VmToHostAffinityWeightPolicyUnit(PolicyUnit policyUnit,
            PendingResourceManager pendingResourceManager) {
        super(policyUnit, pendingResourceManager);
    }

    @Override
    public List<Pair<Guid, Integer>> score(SchedulingContext context, List<VDS> hosts, VM vm) {
        // TODO - cache affinity groups in scheduling context
        List<AffinityGroup> affinityGroups = affinityGroupDao.getAllAffinityGroupsWithFlatLabelsByVmId(vm.getId()).stream()
                .filter(ag -> ag.isVdsAffinityEnabled() && !ag.isVdsEnforcing())
                .collect(Collectors.toList());

        if (affinityGroups.isEmpty()) {
            return hosts.stream().map(host -> new Pair<>(host.getId(), DEFAULT_SCORE)).collect(Collectors.toList());
        }

        List<Pair<Guid, List<Long>>> hostViolations = new ArrayList<>(hosts.size());
        for (VDS host : hosts) {
            List<Long> brokenGroupPriorities = affinityGroups.stream()
                    .filter(ag -> ag.getVdsIds().contains(host.getId()) != ag.isVdsPositive())
                    .map(AffinityGroup::getPriority)
                    .sorted(Comparator.reverseOrder())
                    .collect(Collectors.toList());

            hostViolations.add(new Pair<>(host.getId(), brokenGroupPriorities));
        }

        // Using lexicographicListComparator to sort priorities of broken groups.
        // So that a host that breaks any number of affinity groups with low priority will
        // be sorted before a host that breaks one high priority group.
        //
        // For example:
        // - Host A - breaks groups with priority: 1, 1, 1
        // - Host B - breaks group with priority: 2
        //
        // Host A will be sorted before Host B.

        Comparator<Pair<Guid, List<Long>>> hostPairComparator = Comparator.comparing(Pair::getSecond,
                ListUtils.lexicographicListComparator());

        hostViolations.sort(hostPairComparator);

        List<Pair<Guid, Integer>> retList = new ArrayList<>(hostViolations.size());
        ListUtils.forEachWithRanks(hostViolations, hostPairComparator, (hostPair, rank) -> {
            retList.add(new Pair<>(hostPair.getFirst(), DEFAULT_SCORE + rank));
        });

        return retList;
    }
}

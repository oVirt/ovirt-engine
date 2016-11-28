package org.ovirt.engine.core.bll.scheduling.policyunits;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.scheduling.PolicyUnitImpl;
import org.ovirt.engine.core.bll.scheduling.external.AffinityHostsResult;
import org.ovirt.engine.core.bll.scheduling.pending.PendingResourceManager;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.scheduling.AffinityGroup;
import org.ovirt.engine.core.common.scheduling.PerHostMessages;
import org.ovirt.engine.core.common.scheduling.PolicyUnit;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.scheduling.AffinityGroupDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VmToHostAffinityPolicyUnit extends PolicyUnitImpl {

    private static final Logger log = LoggerFactory.getLogger(VmToHostAffinityPolicyUnit.class);
    private static final int INITIAL_HOST_SCORE = 1;

    @Inject
    AffinityGroupDao affinityGroupDao;

    public VmToHostAffinityPolicyUnit(PolicyUnit policyUnit,
            PendingResourceManager pendingResourceManager) {
        super(policyUnit, pendingResourceManager);
    }

    public AffinityHostsResult getAffinityHostsResult(boolean enforcing,
            List<VDS> hosts,
            VM vm,
            PerHostMessages messages) {

        Map<Guid, Integer> hostViolations = new HashMap<>();
        List<AffinityGroup> affinityGroups = affinityGroupDao.getAllAffinityGroupsByVmId(vm.getId());
        // no affinity groups found for VM return all hosts with no violations
        if (affinityGroups.isEmpty()) {
            return new AffinityHostsResult(hosts);
        }

        Set<Guid> positiveVdsIds = new HashSet<>();
        Set<Guid> negativeVdsIds = new HashSet<>();

        // Group by all hosts in affinity groups per positive or negative
        for (AffinityGroup affinityGroup : affinityGroups) {
            if (affinityGroup.isVdsEnforcing() == enforcing) {
                for (Guid entityId : affinityGroup.getVdsIds()) {
                    if (affinityGroup.isVdsPositive()) {
                        positiveVdsIds.add(entityId);
                    } else {
                        negativeVdsIds.add(entityId);
                    }
                }
            }
        }

        Map<Guid, VDS> hostMap = hosts.stream().collect(Collectors.toMap(VDS::getId, h -> h));

        //Filter out all hosts that do not belong to the cluster
        Set<Guid> positiveVdsIdsInCluster = positiveVdsIds.stream()
                .filter(hostMap::containsKey)
                .collect(Collectors.toSet());
        Set<Guid> negativeVdsIdsInCluster = negativeVdsIds.stream()
                .filter(hostMap::containsKey)
                .collect(Collectors.toSet());

        // No entities, all hosts are valid
        if (positiveVdsIds.isEmpty() && negativeVdsIds.isEmpty()) {
            return new AffinityHostsResult(hosts);
        }

        // log hosts found in both positive and negative affinity groups
        positiveVdsIdsInCluster.stream()
                .filter(v -> negativeVdsIdsInCluster.contains(v))
                .forEach(id -> log.warn("Host '{}' ({}) belongs to both positive and negative affinity list" +
                                " while scheduling VM '{}' ({})",
                        hostMap.get(id).getName(), id,
                        vm.getName(), vm.getId()));

        // log hosts that violate the positive affinity rules
        if (!positiveVdsIdsInCluster.isEmpty()) {
            hostMap.keySet().stream().filter(v -> !positiveVdsIdsInCluster.contains(v))
                    .forEach(id -> {
                        // TODO compute the affinity rule names
                        messages.addMessage(id, String.format("$affinityRules %1$s", ""));
                        messages.addMessage(id, EngineMessage.VAR__DETAIL__AFFINITY_FAILED_POSITIVE.toString());
                        hostViolations.put(id, 1 + hostViolations.getOrDefault(id, INITIAL_HOST_SCORE));
                    });
        }

        // log hosts that violate the negative affinity rules
        if (!negativeVdsIdsInCluster.isEmpty()) {
            hostMap.keySet().stream().
                    filter(v -> negativeVdsIdsInCluster.contains(v))
                    .forEach(id -> {
                        // TODO compute the affinity rule names
                        messages.addMessage(id, String.format("$affinityRules %1$s", ""));
                        messages.addMessage(id, EngineMessage.VAR__DETAIL__AFFINITY_FAILED_NEGATIVE.toString());
                        hostViolations.put(id, 1 + hostViolations.getOrDefault(id, INITIAL_HOST_SCORE));
                    });
        }

        List<VDS> acceptableHosts = hosts.stream()
                .filter(v -> (positiveVdsIdsInCluster.isEmpty()) ||
                        positiveVdsIdsInCluster.contains(v.getId()))
                .filter(v -> !negativeVdsIdsInCluster.contains(v.getId()))
                .collect(Collectors.toList());

        return new AffinityHostsResult(acceptableHosts, hostViolations);
    }
}

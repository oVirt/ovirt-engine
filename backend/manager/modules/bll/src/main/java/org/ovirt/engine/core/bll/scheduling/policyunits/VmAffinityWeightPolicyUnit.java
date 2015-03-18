package org.ovirt.engine.core.bll.scheduling.policyunits;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.bll.scheduling.PolicyUnitImpl;
import org.ovirt.engine.core.bll.scheduling.SchedulingUnit;
import org.ovirt.engine.core.bll.scheduling.pending.PendingResourceManager;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.scheduling.PerHostMessages;
import org.ovirt.engine.core.common.scheduling.PolicyUnit;
import org.ovirt.engine.core.common.scheduling.PolicyUnitType;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;

@SchedulingUnit(
        guid = "84e6ddee-ab0d-42dd-82f0-c297779db567",
        name = "VmAffinityGroups",
        description = "Enables Affinity Groups soft enforcement for VMs; VMs in group are most likely to run either"
                + " on the same hypervisor host (positive) or on independent hypervisor hosts (negative)",
        type = PolicyUnitType.WEIGHT
)
public class VmAffinityWeightPolicyUnit extends PolicyUnitImpl {
    private static final int DEFAULT_SCORE = 1;

    public VmAffinityWeightPolicyUnit(PolicyUnit policyUnit,
            PendingResourceManager pendingResourceManager) {
        super(policyUnit, pendingResourceManager);

    }

    @Override
    public List<Pair<Guid, Integer>> score(Cluster cluster, List<VDS> hosts, VM vm, Map<String, String> parameters) {
        // reuse filter functionality with soft constraint
        List<VDS> acceptableHostsList =
                VmAffinityFilterPolicyUnit.getAcceptableHosts(false,
                        hosts,
                        vm,
                        new PerHostMessages(),
                        getPendingResourceManager());
        Map<Guid, VDS> acceptableHostsMap = new HashMap<>();
        if (acceptableHostsList != null) {
            for (VDS acceptableHost : acceptableHostsList) {
                acceptableHostsMap.put(acceptableHost.getId(), acceptableHost);
            }
        }

        List<Pair<Guid, Integer>> retList = new ArrayList<>();
        int score;
        for (VDS host : hosts) {
            score = DEFAULT_SCORE;
            if (!acceptableHostsMap.containsKey(host.getId())) {
                score = MaxSchedulerWeight;
            }
            retList.add(new Pair<>(host.getId(), score));
        }

        return retList;
    }
}

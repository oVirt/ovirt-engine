package org.ovirt.engine.core.bll.scheduling.policyunits;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.bll.scheduling.PolicyUnitImpl;
import org.ovirt.engine.core.bll.scheduling.pending.PendingResourceManager;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.scheduling.PerHostMessages;
import org.ovirt.engine.core.common.scheduling.PolicyUnit;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;

public class VmAffinityWeightPolicyUnit extends PolicyUnitImpl {
    public VmAffinityWeightPolicyUnit(PolicyUnit policyUnit,
            PendingResourceManager pendingResourceManager) {
        super(policyUnit, pendingResourceManager);
    }

    @Override
    public List<Pair<Guid, Integer>> score(VDSGroup cluster, List<VDS> hosts, VM vm, Map<String, String> parameters) {
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
            score = 1;
            if (!acceptableHostsMap.containsKey(host.getId())) {
                score = MaxSchedulerWeight;
            }
            retList.add(new Pair<>(host.getId(), score));
        }

        return retList;
    }
}

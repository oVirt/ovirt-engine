package org.ovirt.engine.core.bll.scheduling.policyunits;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.ovirt.engine.core.bll.scheduling.SchedulingUnit;
import org.ovirt.engine.core.bll.scheduling.pending.PendingResourceManager;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.scheduling.PerHostMessages;
import org.ovirt.engine.core.common.scheduling.PolicyUnit;
import org.ovirt.engine.core.common.scheduling.PolicyUnitType;
import org.ovirt.engine.core.compat.Guid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SchedulingUnit(
        guid = "84e6ddee-ab0d-42dd-82f0-c297779db566",
        name = "VmAffinityGroups",
        description = "Enables Affinity Groups hard enforcement for VMs; VMs in group are required to run either on"
                + " the same hypervisor host (positive) or on independent hypervisor hosts (negative)",
        type = PolicyUnitType.FILTER
)
public class VmAffinityFilterPolicyUnit extends VmAffinityPolicyUnit {
    private static final Logger log = LoggerFactory.getLogger(VmAffinityFilterPolicyUnit.class);

    public VmAffinityFilterPolicyUnit(PolicyUnit policyUnit,
            PendingResourceManager pendingResourceManager) {
        super(policyUnit, pendingResourceManager);
    }

    @Override
    public List<VDS> filter(Cluster cluster, List<VDS> hosts, VM vm, Map<String, String> parameters, PerHostMessages messages) {
        Map<Guid, Integer> acceptableHosts = getAcceptableHostsWithPriorities(true, hosts, vm, messages);

        return hosts.stream()
                .filter(h -> acceptableHosts.containsKey(h.getId()))
                .collect(Collectors.toList());
    }
}

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

@SchedulingUnit(
        guid = "e69808a9-8a41-40f1-94ba-dd5d385d82d8",
        name = "VmToHostsAffinityGroups",
        description = "Enables Affinity Groups hard enforcement for VMs to hosts;"
                + " VMs in group are required to run either on one of the hosts in group (positive) "
                + "or on independent hosts which are excluded from the hosts in group (negative).",
        type = PolicyUnitType.FILTER
)
public class VmToHostAffinityFilterPolicyUnit extends VmToHostAffinityPolicyUnit {

    public VmToHostAffinityFilterPolicyUnit(PolicyUnit policyUnit,
            PendingResourceManager pendingResourceManager) {
        super(policyUnit, pendingResourceManager);
    }

    @Override
    public List<VDS> filter(Cluster cluster,
            List<VDS> hosts,
            VM vm,
            Map<String, String> parameters,
            PerHostMessages messages) {

        Map<Guid, Integer> hostViolations = getHostViolationCount(true, hosts, vm, messages);

        return hosts.stream()
                .filter(host -> !hostViolations.containsKey(host.getId()))
                .collect(Collectors.toList());
    }
}

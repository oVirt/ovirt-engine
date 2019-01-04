package org.ovirt.engine.core.bll.scheduling.policyunits;

import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.bll.scheduling.PolicyUnitImpl;
import org.ovirt.engine.core.bll.scheduling.SchedulingContext;
import org.ovirt.engine.core.bll.scheduling.SchedulingUnit;
import org.ovirt.engine.core.bll.scheduling.pending.PendingResourceManager;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.scheduling.PerHostMessages;
import org.ovirt.engine.core.common.scheduling.PolicyUnit;
import org.ovirt.engine.core.common.scheduling.PolicyUnitType;

@SchedulingUnit(
        guid = "0bd8fef9-bcb4-4250-9998-410f01608d9b",
        name = "ClusterInMaintenance",
        type = PolicyUnitType.FILTER,
        description = "Prevents any VM start except for highly available VMs"
)
public class ClusterInMaintenanceFilterPolicyUnit extends PolicyUnitImpl {
    public ClusterInMaintenanceFilterPolicyUnit(PolicyUnit policyUnit,
            PendingResourceManager pendingResourceManager) {
        super(policyUnit, pendingResourceManager);
    }

    @Override
    public List<VDS> filter(SchedulingContext context,
            List<VDS> hosts,
            VM vm,
            PerHostMessages messages) {
        // Highly available VMs are allowed to start
        if (vm.isAutoStartup()) {
            return hosts;
        }

        // Already running VMs are allowed to migrate
        if (vm.getRunOnVds() != null) {
            return hosts;
        }

        hosts.forEach(h -> messages.addMessage(h.getId(), EngineMessage.VAR__DETAIL__CLUSTER_IN_MAINTENANCE.name()));
        return Collections.emptyList();
    }
}

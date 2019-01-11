package org.ovirt.engine.core.bll.scheduling.policyunits;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.bll.scheduling.PolicyUnitImpl;
import org.ovirt.engine.core.bll.scheduling.SchedulingContext;
import org.ovirt.engine.core.bll.scheduling.SchedulingUnit;
import org.ovirt.engine.core.bll.scheduling.pending.PendingResourceManager;
import org.ovirt.engine.core.bll.scheduling.utils.NumaPinningHelper;
import org.ovirt.engine.core.common.businessentities.NumaTuneMode;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmNumaNode;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.scheduling.PerHostMessages;
import org.ovirt.engine.core.common.scheduling.PolicyUnit;
import org.ovirt.engine.core.common.scheduling.PolicyUnitType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SchedulingUnit(
        guid = "fcbfe4b1-b83e-4428-b9d3-b3d348b93be6",
        name = "NUMA",
        description = "Filters out hosts that have incompatible NUMA nodes.",
        type = PolicyUnitType.FILTER
)
public class NumaPolicyUnit extends PolicyUnitImpl {
    private static final Logger log = LoggerFactory.getLogger(NumaPolicyUnit.class);

    public NumaPolicyUnit(PolicyUnit policyUnit,
            PendingResourceManager pendingResourceManager) {
        super(policyUnit, pendingResourceManager);
    }

    @Override
    public List<VDS> filter(SchedulingContext context, List<VDS> hosts, VM vm, PerHostMessages messages) {
        List<VmNumaNode> vmNumaNodes = vm.getvNumaNodeList();
        boolean vmNumaPinned = vmNumaNodes.stream()
                .anyMatch(node -> !node.getVdsNumaNodeList().isEmpty());

        // If no VM numa node is pinned, all hosts are accepted.
        //
        // A VM with unpinned NUMA nodes can run on a host without NUMA support.
        if (!vmNumaPinned) {
            return hosts;
        }

        List<VDS> result = new ArrayList<>();
        for (VDS host: hosts) {
            // Skip checks if the VM is currently running on the host
            if (host.getId().equals(vm.getRunOnVds())) {
                result.add(host);
                continue;
            }

            if (!host.isNumaSupport()) {
                log.debug("Host '{}' does not support NUMA", host.getName());
                messages.addMessage(host.getId(), EngineMessage.VAR__DETAIL__NUMA_NOT_SUPPORTED.toString());
                continue;
            }

            // If the NUMA mode is PREFERRED, a host with any NUMA configuration is accepted.
            if (vm.getNumaTuneMode() == NumaTuneMode.PREFERRED) {
                result.add(host);
                continue;
            }

            // TODO - INTERLEAVE mode should use different algorithm to check if VM nodes fit host nodes.
            //        For now, we use the same algorithm as for STRICT mode.
            //        This will cause the host to be filtered out even in some cases when INTERLEAVE nodes could fit.

            if (!NumaPinningHelper.findAssignment(vmNumaNodes, host.getNumaNodeList()).isPresent()) {
                log.debug("Host '{}' cannot accommodate memory of VM's pinned virtual NUMA nodes within host's physical NUMA nodes",
                        host.getName());
                messages.addMessage(host.getId(), EngineMessage.VAR__DETAIL__NOT_MEMORY_PINNED_NUMA.toString());
                continue;
            }
            result.add(host);
        }

        return result;
    }
}

package org.ovirt.engine.core.bll.scheduling.policyunits;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import org.apache.commons.lang.math.NumberUtils;
import org.ovirt.engine.core.bll.scheduling.PolicyUnitImpl;
import org.ovirt.engine.core.bll.scheduling.PolicyUnitParameter;
import org.ovirt.engine.core.bll.scheduling.SchedulingContext;
import org.ovirt.engine.core.bll.scheduling.SchedulingUnit;
import org.ovirt.engine.core.bll.scheduling.pending.PendingResourceManager;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.scheduling.PerHostMessages;
import org.ovirt.engine.core.common.scheduling.PolicyUnit;
import org.ovirt.engine.core.common.scheduling.PolicyUnitType;
import org.ovirt.engine.core.dao.VmDao;

/**
 * Make sure there is at least a configured amount of HE enabled
 * hosts that have enough memory to run the engine VM in case the
 * current host dies.
 *
 * The logic
 * - does nothing when this is not an hosted engine deployment
 * - does nothing when the engine VM is (re-)scheduled
 * - does not touch any non-HE hosts
 * - does nothing when there are enough HE spares
 * - removes all HE hosts when there are not enough spares
 */
@SchedulingUnit(
        guid = "53bff075-8306-446f-a53b-9c872a29d197",
        name = "HostedEngineSpares",
        description = "Reserve space for starting the hosted engine VM on different hosts in case "
                + "the current one crashes.",
        type = PolicyUnitType.FILTER,
        parameters = PolicyUnitParameter.HE_SPARES_COUNT
)
public class HostedEngineMemoryReservationFilterPolicyUnit extends PolicyUnitImpl {

    @Inject
    private VmDao vmDao;

    public HostedEngineMemoryReservationFilterPolicyUnit(PolicyUnit policyUnit,
            PendingResourceManager pendingResourceManager) {
        super(policyUnit, pendingResourceManager);
    }

    @Override
    public List<VDS> filter(SchedulingContext context, List<VDS> hosts, List<VM> vmGroup, PerHostMessages messages) {
        // Hosts available for running the `vm`
        Set<VDS> candidateHosts = new HashSet<>();
        // Hosts needed as spares for the hosted engine
        Set<VDS> spares = new HashSet<>();

        final int requiredSpares = NumberUtils.toInt(context.getPolicyParameters().get(PolicyUnitParameter.HE_SPARES_COUNT.getDbName()), 0);

        // There are no hosts, skip this unit
        if (hosts.isEmpty()) {
            return hosts;
        }

        // No spares requested, skip this unit
        if (requiredSpares == 0) {
            return hosts;
        }

        // Get the instance of hosted engine VM so we can get the amount of memory that is needed
        VM hostedEngine = vmDao.getHostedEngineVm();

        // Not a hosted engine deployment, ignore this unit
        if (hostedEngine == null) {
            return hosts;
        }

        // The hosted engine VM is not part of the currently scheduled cluster
        if (!hostedEngine.getClusterId().equals(vmGroup.get(0).getClusterId())) {
            return hosts;
        }

        // Scheduling the hosted engine VM, do nothing
        if (vmGroup.stream().anyMatch(vm -> hostedEngine.getId().equals(vm.getId()))) {
            return hosts;
        }

        // Count the number of hosted engine spares
        for (VDS host: hosts) {
            int vmMemoryNeeded = vmGroup.stream()
                    // If the VM is currently running on the host,
                    // it does not require any additional memory
                    .filter(vm -> !host.getId().equals(vm.getRunOnVds()))
                    .mapToInt(vm -> vm.getMemSizeMb() + host.getGuestOverhead())
                    .sum();

            // Not a HE host
            if (!host.getHighlyAvailableIsActive()) {
                candidateHosts.add(host);
            // HE host that can't run the engine at this moment because of score
            } else if (host.getHighlyAvailableScore() == 0) {
                candidateHosts.add(host);
            // HE host in local maintenance can't be used as a spare
            } else if (host.getHighlyAvailableLocalMaintenance()) {
                candidateHosts.add(host);
            // HE host where the engine is currently running
            } else if (host.getId().equals(hostedEngine.getRunOnVds())) {
                candidateHosts.add(host);
            // HE host that has enough memory to run both the hosted engine VM
            // and the scheduled VM at the same time -- count as candidate and spare!
            } else if (host.getMaxSchedulingMemory()
                    > vmMemoryNeeded + host.getGuestOverhead() + hostedEngine.getMemSizeMb()) {
                spares.add(host);
                candidateHosts.add(host);
            // HE host that has enough memory to run the hosted engine VM -- count as spare only!
            } else if (host.getMaxSchedulingMemory() > hostedEngine.getMemSizeMb() + host.getGuestOverhead()) {
                spares.add(host);
            } else {
                candidateHosts.add(host);
            }
        }

        if (spares.size() <= requiredSpares) {
            // We have the right amount of spares (or less) and the spares have to be kept reserved
            // for the hosted engine
            for (VDS host: spares) {
                messages.addMessage(host.getId(), EngineMessage.VAR__DETAIL__NOT_ENOUGH_HE_SPARES.name());
            }
        } else {
            // There are more spares than necessary, one less won't hurt anything
            candidateHosts.addAll(spares);
        }

        return new ArrayList<>(candidateHosts);
    }
}

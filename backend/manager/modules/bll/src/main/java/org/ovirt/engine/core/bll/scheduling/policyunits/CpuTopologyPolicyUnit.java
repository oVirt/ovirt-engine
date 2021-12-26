package org.ovirt.engine.core.bll.scheduling.policyunits;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.bll.scheduling.PolicyUnitImpl;
import org.ovirt.engine.core.bll.scheduling.SchedulingContext;
import org.ovirt.engine.core.bll.scheduling.SchedulingUnit;
import org.ovirt.engine.core.bll.scheduling.pending.PendingResourceManager;
import org.ovirt.engine.core.common.businessentities.CpuPinningPolicy;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.scheduling.PerHostMessages;
import org.ovirt.engine.core.common.scheduling.PolicyUnit;
import org.ovirt.engine.core.common.scheduling.PolicyUnitType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SchedulingUnit(
        guid = "35c2f1a5-8928-48e9-81ac-4c49eb49d60e",
        name = "CPUTopology",
        type = PolicyUnitType.FILTER,
        description = "Runs VMs only on hosts with a proper CPU topology")
public class CpuTopologyPolicyUnit extends PolicyUnitImpl {

    private static final Logger log = LoggerFactory.getLogger(CpuTopologyPolicyUnit.class);

    public CpuTopologyPolicyUnit(PolicyUnit policyUnit, PendingResourceManager pendingResourceManager) {
        super(policyUnit, pendingResourceManager);
    }

    @Override
    public List<VDS> filter(SchedulingContext context, List<VDS> hosts, VM vm, PerHostMessages messages) {
        List<VDS> candidates = new ArrayList<>();

        for (VDS host : hosts) {

            if (host.getCpuSockets() == null || host.getCpuCores() == null || host.getCpuThreads() == null) {
                log.warn("Unknown number of cores for host {}.", host.getName());
                continue;
            }

            // when the VM uses Resize and PIN CPU pinning policy the host needs to have
            // more than one core per socket
            if (vm.getCpuPinningPolicy() == CpuPinningPolicy.RESIZE_AND_PIN_NUMA) {
                int coresPerSocket = host.getCpuCores() / host.getCpuSockets();

                if (coresPerSocket <= 1) {
                    messages.addMessage(host.getId(), EngineMessage.VAR__DETAIL__NOT_ENOUGH_CORES_PER_SOCKET_FOR_RESIZE_AND_PIN.toString());
                    log.debug(
                            "Host '{}' has only one core per socket. Resize and pin requires more than one core per socket",
                            host.getName());
                    continue;
                }

                if (host.getCpuSockets() != host.getNumaNodeList().size()) {
                    log.debug("Host '{}' is not qualified for the 'Resize and Pin' CPU pinning policy," +
                            " number of sockets is not equal to number of NUMA nodes", host.getName());
                    messages.addMessage(host.getId(), EngineMessage.VAR__DETAIL__SOCKETS_UNEQUAL_NUMA.toString());
                    continue;
                }
            } else if (vm.getCpuPinningPolicy() == CpuPinningPolicy.DEDICATED) {
                // the dedicated pinning requires that all vThreads of the same vCore are pinned
                // to the same physical core. That is why the number of threads per core for the VM
                // cannot exceed the number of threads per core on the host
                int threadsPerCore = host.getCpuThreads() / host.getCpuCores();

                if (threadsPerCore < vm.getThreadsPerCpu()) {
                    messages.addMessage(host.getId(), EngineMessage.VAR__DETAIL__NOT_ENOUGH_THREADS_PER_CORE_FOR_DEDICATED.toString());
                    log.debug(
                            "Host '{}' has not enough CPU threads per core to run the VM with Dedicated CPU pinning policy",
                            host.getName());
                    continue;
                }
            }
            candidates.add(host);
        }
        return candidates;
    }
}

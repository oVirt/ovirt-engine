package org.ovirt.engine.core.bll.scheduling.policyunits;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.bll.scheduling.PolicyUnitImpl;
import org.ovirt.engine.core.bll.scheduling.SchedulingContext;
import org.ovirt.engine.core.bll.scheduling.SchedulingUnit;
import org.ovirt.engine.core.bll.scheduling.SlaValidator;
import org.ovirt.engine.core.bll.scheduling.pending.PendingResourceManager;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.scheduling.PerHostMessages;
import org.ovirt.engine.core.common.scheduling.PolicyUnit;
import org.ovirt.engine.core.common.scheduling.PolicyUnitType;
import org.ovirt.engine.core.common.utils.VmCpuCountHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SchedulingUnit(
        guid = "6d636bf6-a35c-4f9d-b68d-0731f720cddc",
        name = "CPU",
        type = PolicyUnitType.FILTER,
        description = "Filters out hosts with less CPUs than VM's CPUs"
)
public class CPUPolicyUnit extends PolicyUnitImpl {
    private static final Logger log = LoggerFactory.getLogger(CPUPolicyUnit.class);

    public CPUPolicyUnit(PolicyUnit policyUnit,
            PendingResourceManager pendingResourceManager) {
        super(policyUnit, pendingResourceManager);
    }

    @Override
    public List<VDS> filter(SchedulingContext context, List<VDS> hosts, VM vm, PerHostMessages messages) {
        List<VDS> list = new ArrayList<>();

        for (VDS vds : hosts) {
            if (VmCpuCountHelper.isAutoPinning(vm) && !VmCpuCountHelper.isDynamicCpuTopologySet(vm)) {
                if (vds.getCpuCores() / vds.getCpuSockets() > 1) {
                    list.add(vds);
                } else {
                    messages.addMessage(vds.getId(), EngineMessage.VAR__DETAIL__NOT_ENOUGH_CORES.toString());
                    log.debug("Host '{}' has only one core per socket. Resize and pin requires more than one core per socket", vds.getName());
                }
                continue;
            }
            Integer cores = SlaValidator.getEffectiveCpuCores(vds, context.getCluster().getCountThreadsAsCores());
            if (cores != null && ((VmCpuCountHelper.isDynamicCpuTopologySet(vm) ?
                    vm.getCurrentNumOfCpus(false) : vm.getNumOfCpus(false)) > cores)) {
                messages.addMessage(vds.getId(), EngineMessage.VAR__DETAIL__NOT_ENOUGH_CORES.toString());
                log.debug("Host '{}' has less cores ({}) than vm cores ({})",
                        vds.getName(),
                        cores,
                        VmCpuCountHelper.getDynamicNumOfCpu(vm));
                continue;
            }

            list.add(vds);
        }
        return list;
    }
}

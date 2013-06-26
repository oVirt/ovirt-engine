package org.ovirt.engine.core.bll.scheduling.policyunits;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.bll.scheduling.PolicyUnitImpl;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.scheduling.PolicyUnit;

public class MemoryPolicyUnit extends PolicyUnitImpl {

    public MemoryPolicyUnit(PolicyUnit policyUnit) {
        super(policyUnit);
    }

    @Override
    public List<VDS> filter(List<VDS> hosts, Map<String, Object> parameters, List<String> messages) {
        VM vm = (VM) parameters.get(PolicyUnitImpl.VM);
        List<VDS> list = new ArrayList<>();
        // If Vm in Paused mode - no additional memory allocation needed
        if (vm.getStatus() == VMStatus.Paused) {
            return hosts;
        }
        for (VDS vds : hosts) {
            if (!isVMSwapValueLegal(vds)) {
                log.debugFormat("host '{0}' swap value is illegal", vds.getName());
                messages.add(VdcBllMessages.ACTION_TYPE_FAILED_VDS_VM_SWAP.toString());
                continue;
            }
            if (!memoryChecker.evaluate(vds, vm)) {
                log.debugFormat("host '{0}' has insufficient memory to run the VM", vds.getName());
                messages.add(VdcBllMessages.ACTION_TYPE_FAILED_VDS_VM_MEMORY.toString());
                continue;
            }
            list.add(vds);
        }
        return list;
    }

    /**
     * Determines whether [is VM swap value legal] [the specified VDS].
     * @param host
     *            The VDS.
     * @return <c>true</c> if [is VM swap value legal] [the specified VDS]; otherwise, <c>false</c>.
     */
    private boolean isVMSwapValueLegal(VDS host) {
        if (!Config.<Boolean> GetValue(ConfigValues.EnableSwapCheck)) {
            return true;
        }

        if (host.getSwapTotal() == null || host.getSwapFree() == null || host.getMemAvailable() == null
                || host.getMemAvailable() <= 0 || host.getPhysicalMemMb() == null || host.getPhysicalMemMb() <= 0) {
            return true;
        }

        long swap_total = host.getSwapTotal();
        long swap_free = host.getSwapFree();
        long mem_available = host.getMemAvailable();
        long physical_mem_mb = host.getPhysicalMemMb();

        return ((swap_total - swap_free - mem_available) * 100 / physical_mem_mb) <= Config
                .<Integer> GetValue(ConfigValues.BlockMigrationOnSwapUsagePercentage);
    }
}

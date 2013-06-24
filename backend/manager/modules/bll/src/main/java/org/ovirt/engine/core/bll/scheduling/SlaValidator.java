package org.ovirt.engine.core.bll.scheduling;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;

public class SlaValidator {
    public static Log log = LogFactory.getLog(SlaValidator.class);

    private static final SlaValidator instance = new SlaValidator();

    public static SlaValidator getInstance() {
        return instance;
    }

    public boolean hasMemoryToRunVM(VDS curVds, VM vm) {
        boolean retVal = false;
        if (curVds.getMemCommited() != null && curVds.getPhysicalMemMb() != null && curVds.getReservedMem() != null) {
            double vdsCurrentMem =
                    curVds.getMemCommited() + curVds.getPendingVmemSize() + curVds.getGuestOverhead() + curVds
                            .getReservedMem() + vm.getMinAllocatedMem();
            double vdsMemLimit = curVds.getMaxVdsMemoryOverCommit() * curVds.getPhysicalMemMb() / 100.0;
            if (log.isDebugEnabled()) {
                log.debugFormat("hasMemoryToRunVM: host {0} pending vmem size is : {1} MB",
                        curVds.getName(),
                        curVds.getPendingVmemSize());
                log.debugFormat("Host Mem Conmmitted: {0}, Host Reserved Mem: {1}, Host Guest Overhead {2}, VM Min Allocated Mem {3}",
                        curVds.getMemCommited(),
                        curVds.getReservedMem(),
                        curVds.getGuestOverhead(),
                        vm.getMinAllocatedMem());
                log.debugFormat("{0} <= ???  {1}", vdsCurrentMem, vdsMemLimit);
            }
            retVal = (vdsCurrentMem <= vdsMemLimit);
        }
        return retVal;
    }

    /**
     * Check if the given host has enough CPU to run the VM, without exceeding the high utilization threshold.
     *
     * @param vds
     *            The host to check.
     * @param vm
     *            The VM to check.
     * @return Does this host has enough CPU (without exceeding the threshold) to run the VM.
     */
    public boolean hasCpuToRunVM(VDS vds, VM vm) {
        if (vds.getUsageCpuPercent() == null || vm.getUsageCpuPercent() == null) {
            return false;
        }
        // The predicted CPU is actually the CPU that the VM will take considering how many cores it has and now many
        // cores the host has. This is why we take both parameters into consideration.
        int predictedVmCpu = (vm.getUsageCpuPercent() * vm.getNumOfCpus()) / getEffectiveCpuCores(vds);
        boolean result = vds.getUsageCpuPercent() + predictedVmCpu <= vds.getHighUtilization();
        if (log.isDebugEnabled()) {
            log.debugFormat("Host {0} has {1}% CPU load; VM {2} is predicted to have {3}% CPU load; " +
                    "High threshold is {4}%. Host is {5}suitable in terms of CPU.",
                    vds.getName(),
                    vds.getUsageCpuPercent(),
                    vm.getName(),
                    predictedVmCpu,
                    vds.getHighUtilization(),
                    (result ? "" : "not "));
        }
        return result;
    }

    public static Integer getEffectiveCpuCores(VDS vds) {
        VDSGroup vdsGroup = DbFacade.getInstance().getVdsGroupDao().get(vds.getVdsGroupId());

        if (vds.getCpuThreads() != null
                && vdsGroup != null
                && Boolean.TRUE.equals(vdsGroup.getCountThreadsAsCores())) {
            return vds.getCpuThreads();
        } else {
            return vds.getCpuCores();
        }
    }

}

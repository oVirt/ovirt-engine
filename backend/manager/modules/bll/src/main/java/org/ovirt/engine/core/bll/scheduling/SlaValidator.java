package org.ovirt.engine.core.bll.scheduling;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;

public class SlaValidator {
    private static final Log log = LogFactory.getLog(SlaValidator.class);

    private static final SlaValidator instance = new SlaValidator();

    public static SlaValidator getInstance() {
        return instance;
    }

    public boolean hasMemoryToRunVM(VDS curVds, VM vm) {
        boolean retVal = false;
        if (curVds.getMemCommited() != null && curVds.getPhysicalMemMb() != null && curVds.getReservedMem() != null) {
            double vdsCurrentMem = getVdsCurrentMemoryInUse(curVds) + vm.getMinAllocatedMem();
            double vdsMemLimit = getVdsMemLimit(curVds);
            log.debugFormat("hasMemoryToRunVM: host '{0}' physical vmem size is : {1} MB",
                    curVds.getName(),
                    curVds.getPhysicalMemMb());
            log.debugFormat("Host Mem Conmmitted: '{0}', pending vmem size is : {1}, Host Guest Overhead {2}, Host Reserved Mem: {3}, VM Min Allocated Mem {4}",
                    curVds.getMemCommited(),
                    curVds.getPendingVmemSize(),
                    curVds.getGuestOverhead(),
                    curVds.getReservedMem(),
                    vm.getMinAllocatedMem());
            log.debugFormat("{0} <= ???  {1}", vdsCurrentMem, vdsMemLimit);
            retVal = (vdsCurrentMem <= vdsMemLimit);
        }
        return retVal;
    }

    public int getHostAvailableMemoryLimit(VDS curVds) {
        if (curVds.getMemCommited() != null && curVds.getPhysicalMemMb() != null && curVds.getReservedMem() != null) {
            double vdsCurrentMem = getVdsCurrentMemoryInUse(curVds);
            double vdsMemLimit = getVdsMemLimit(curVds);
            return (int) (vdsMemLimit - vdsCurrentMem);
        }
        return 0;
    }

    private double getVdsMemLimit(VDS curVds) {
        // if single vm on host. Disregard memory over commitment
        int computedMemoryOverCommit = (curVds.getVmCount() == 0) ? 100 : curVds.getMaxVdsMemoryOverCommit();
        return (computedMemoryOverCommit * curVds.getPhysicalMemMb() / 100.0);
    }

    private double getVdsCurrentMemoryInUse(VDS curVds) {
        return curVds.getMemCommited() + curVds.getPendingVmemSize() + curVds.getGuestOverhead()
                        + curVds.getReservedMem();
    }

    public static Integer getEffectiveCpuCores(VDS vds) {
        VDSGroup vdsGroup = DbFacade.getInstance().getVdsGroupDao().get(vds.getVdsGroupId());
        return getEffectiveCpuCores(vds, vdsGroup != null ? vdsGroup.getCountThreadsAsCores() : false);
    }

    public static Integer getEffectiveCpuCores(VDS vds, boolean countThreadsAsCores) {
        if (vds.getCpuThreads() != null
                && countThreadsAsCores) {
            return vds.getCpuThreads();
        } else {
            return vds.getCpuCores();
        }
    }
}

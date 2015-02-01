package org.ovirt.engine.core.bll.scheduling;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SlaValidator {
    private static final Logger log = LoggerFactory.getLogger(SlaValidator.class);

    private static final SlaValidator instance = new SlaValidator();

    public static SlaValidator getInstance() {
        return instance;
    }

    public boolean hasMemoryToRunVM(VDS curVds, VM vm) {
        boolean retVal = false;
        if (curVds.getMemCommited() != null && curVds.getPhysicalMemMb() != null && curVds.getReservedMem() != null) {
            double vdsCurrentMem = getVdsCurrentMemoryInUse(curVds) + vm.getMinAllocatedMem();
            double vdsMemLimit = getVdsMemLimit(curVds);
            log.debug("hasMemoryToRunVM: host '{}' physical vmem size is : {} MB",
                    curVds.getName(),
                    curVds.getPhysicalMemMb());
            log.debug("Host Mem Conmmitted: '{}', pending vmem size is : {}, Host Guest Overhead {}, Host Reserved Mem: {}, VM Min Allocated Mem {}",
                    curVds.getMemCommited(),
                    curVds.getPendingVmemSize(),
                    curVds.getGuestOverhead(),
                    curVds.getReservedMem(),
                    vm.getMinAllocatedMem());
            log.debug("{} <= ???  {}", vdsCurrentMem, vdsMemLimit);
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

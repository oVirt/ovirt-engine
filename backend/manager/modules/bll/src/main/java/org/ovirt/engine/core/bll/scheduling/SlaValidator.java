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
            double vdsCurrentMem =
                    curVds.getMemCommited() + curVds.getPendingVmemSize() + curVds.getGuestOverhead() + curVds
                            .getReservedMem() + vm.getMinAllocatedMem();
            double vdsMemLimit = curVds.getMaxVdsMemoryOverCommit() * curVds.getPhysicalMemMb() / 100.0;
            log.debug("hasMemoryToRunVM: host '{}' pending vmem size is : {} MB",
                    curVds.getName(),
                    curVds.getPendingVmemSize());
            log.debug("Host Mem Conmmitted: '{}', Host Reserved Mem: {}, Host Guest Overhead {}, VM Min Allocated Mem {}",
                    curVds.getMemCommited(),
                    curVds.getReservedMem(),
                    curVds.getGuestOverhead(),
                    vm.getMinAllocatedMem());
            log.debug("{} <= ???  {}", vdsCurrentMem, vdsMemLimit);
            retVal = (vdsCurrentMem <= vdsMemLimit);
        }
        return retVal;
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

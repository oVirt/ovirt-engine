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

package org.ovirt.engine.core.bll;

import java.util.List;

import org.ovirt.engine.core.common.action.RunVmParams;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmType;
import org.ovirt.engine.core.common.businessentities.vm_pool_map;
import org.ovirt.engine.core.common.businessentities.vm_pools;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;
import org.ovirt.engine.core.utils.timer.OnTimerMethodAnnotation;

public class VmPoolMonitor {

    /**
     * Goes over each Vmpool, and makes sure there are at least as much prestarted Vms as defined in the prestarted_vms
     * field
     */
    @OnTimerMethodAnnotation("managePrestartedVmsInAllVmPools")
    public void managePrestartedVmsInAllVmPools() {
        List<vm_pools> vmPools = DbFacade.getInstance().getVmPoolDAO().getAll();
        for (vm_pools vmPool : vmPools) {
            managePrestartedVmsInPool(vmPool);
        }
    }

    /**
     * Checks how many prestarted vms are missing in the pool, and attempts to prestart either that amount or BATCH_SIZE
     * (the minimum between the two).
     * @param vmPool
     */
    private void managePrestartedVmsInPool(vm_pools vmPool) {
        int prestartedVms;
        int missingPrestartedVms;
        int numOfVmsToPrestart;
        prestartedVms = VmPoolCommandBase.getNumOfPrestartedVmsInPool(vmPool.getvm_pool_id());
        missingPrestartedVms = vmPool.getPrestartedVms() - prestartedVms;
        if (missingPrestartedVms > 0) {
            // We do not want to start too many vms at once
            int batchSize = Config.<Integer> GetValue(ConfigValues.VmPoolMonitorBatchSize);
            if (missingPrestartedVms > batchSize) {
                numOfVmsToPrestart = batchSize;
            } else {
                numOfVmsToPrestart = missingPrestartedVms;
            }
            log.infoFormat("VmPool {0} is missing {1} prestarted Vms, attempting to prestart {2} Vms",
                    vmPool.getvm_pool_id(),
                    missingPrestartedVms,
                    numOfVmsToPrestart);
            prestartVms(vmPool, numOfVmsToPrestart);
        }
    }

    /***
     * Prestarts the given amount of vmsToPrestart, in the given Vm Pool
     * @param vmPool
     * @param numOfVmsToPrestart
     */
    private void prestartVms(vm_pools vmPool, int numOfVmsToPrestart) {
        // Fetch all vms that are in status down
        List<vm_pool_map> vmPoolMaps = DbFacade.getInstance().getVmPoolDAO()
                .getVmMapsInVmPoolByVmPoolIdAndStatus(vmPool.getvm_pool_id(), VMStatus.Down);
        int prestartedVmsCounter = 0;
        if (vmPoolMaps != null && vmPoolMaps.size() > 0) {
            for (vm_pool_map map : vmPoolMaps) {
                if (prestartedVmsCounter < numOfVmsToPrestart) {
                    if (VmPoolCommandBase.CanAttachNonPrestartedVmToUser(map.getvm_guid())) {
                        VM vmToPrestart = DbFacade.getInstance().getVmDAO().get(map.getvm_guid());
                        if (prestartVm(vmToPrestart)) {
                            prestartedVmsCounter++;
                        }
                    }
                } else {
                    // If we reached the required amount, no need to continue
                    break;
                }
            }
        } else {
            log.infoFormat("No Vms avaialable for prestarting");
        }
        if (prestartedVmsCounter > 0) {
            log.infoFormat("Successfully Prestarted {0} Vms, in VmPool {1}",
                    prestartedVmsCounter,
                    vmPool.getvm_pool_id());
        } else {
            log.infoFormat("Failed to prestart Vms for VmPool {0}",
                    vmPool.getvm_pool_id());
        }
    }

    /**
     * Prestarts the given VM
     * @param vmToPrestart
     * @return
     */
    private boolean prestartVm(VM vmToPrestart) {
        log.infoFormat("Prestarting Vm {0}", vmToPrestart);
        boolean prestartingVmSucceeded = false;
        RunVmParams runVmParams = new RunVmParams(vmToPrestart.getId());
        runVmParams.setUseVnc(vmToPrestart.getvm_type() == VmType.Server);
        runVmParams.setEntityId(vmToPrestart);
        runVmParams.setRunAsStateless(true);
        VdcReturnValueBase vdcReturnValue = Backend.getInstance().runInternalAction(VdcActionType.RunVm,
                runVmParams);
        prestartingVmSucceeded = vdcReturnValue.getSucceeded();
        if (prestartingVmSucceeded) {
            log.infoFormat("Prestarting Vm {0} succeeded", vmToPrestart);
        } else {
            log.infoFormat("Prestarting Vm {0} failed", vmToPrestart);
        }
        return prestartingVmSucceeded;
    }

    private static Log log = LogFactory.getLog(VmPoolMonitor.class);
}

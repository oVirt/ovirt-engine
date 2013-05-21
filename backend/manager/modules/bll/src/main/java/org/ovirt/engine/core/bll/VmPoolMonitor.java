package org.ovirt.engine.core.bll;

import java.util.List;

import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.common.action.RunVmParams;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmPool;
import org.ovirt.engine.core.common.businessentities.VmPoolMap;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.Guid;
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
        List<VmPool> vmPools = DbFacade.getInstance().getVmPoolDao().getAll();
        for (VmPool vmPool : vmPools) {
            managePrestartedVmsInPool(vmPool);
        }
    }

    /**
     * Checks how many prestarted vms are missing in the pool, and attempts to prestart either that amount or BATCH_SIZE
     * (the minimum between the two).
     * @param vmPool
     */
    private void managePrestartedVmsInPool(VmPool vmPool) {
        Guid vmPoolId = vmPool.getVmPoolId();
        int prestartedVms = VmPoolCommandBase.getNumOfPrestartedVmsInPool(vmPoolId);
        int missingPrestartedVms = vmPool.getPrestartedVms() - prestartedVms;
        if (missingPrestartedVms > 0) {
            // We do not want to start too many vms at once
            int numOfVmsToPrestart =
                    Math.min(missingPrestartedVms, Config.<Integer> GetValue(ConfigValues.VmPoolMonitorBatchSize));

            log.infoFormat("VmPool {0} is missing {1} prestarted Vms, attempting to prestart {2} Vms",
                    vmPoolId,
                    missingPrestartedVms,
                    numOfVmsToPrestart);
            prestartVms(vmPoolId, numOfVmsToPrestart);
        }
    }

    /***
     * Prestarts the given amount of vmsToPrestart, in the given Vm Pool
     * @param vmPoolId
     * @param numOfVmsToPrestart
     */
    private void prestartVms(Guid vmPoolId, int numOfVmsToPrestart) {
        // Fetch all vms that are in status down
        List<VmPoolMap> vmPoolMaps = DbFacade.getInstance().getVmPoolDao()
                .getVmMapsInVmPoolByVmPoolIdAndStatus(vmPoolId, VMStatus.Down);
        int failedAttempts = 0;
        int prestartedVmsCounter = 0;
        final int maxFailedAttempts = Config.<Integer> GetValue(ConfigValues.VmPoolMonitorMaxAttempts);
        if (vmPoolMaps != null && vmPoolMaps.size() > 0) {
            for (VmPoolMap map : vmPoolMaps) {
                if (failedAttempts < maxFailedAttempts && prestartedVmsCounter < numOfVmsToPrestart) {
                    if (prestartVm(map.getvm_guid())) {
                        prestartedVmsCounter++;
                        failedAttempts = 0;
                    } else {
                        failedAttempts++;
                    }
                } else {
                    // If we reached the required amount or we exceeded the number of allowed failures, stop
                    logResultOfPrestartVms(prestartedVmsCounter, numOfVmsToPrestart, vmPoolId);
                    break;
                }
            }
        } else {
            log.infoFormat("No Vms avaialable for prestarting");
        }
    }

    /**
     * Logs the results of the attempt to prestart Vms in a Vm Pool
     * @param prestartedVmsCounter
     * @param numOfVmsToPrestart
     * @param vmPoolId
     */
    private void logResultOfPrestartVms(int prestartedVmsCounter, int numOfVmsToPrestart, Guid vmPoolId) {
        if (prestartedVmsCounter > 0) {
            log.infoFormat("Prestarted {0} Vms out of the {1} required, in VmPool {2}",
                    prestartedVmsCounter,
                    numOfVmsToPrestart,
                    vmPoolId);
        } else {
            log.infoFormat("Failed to prestart any Vms for VmPool {0}",
                    vmPoolId);
        }
    }

    /**
     * Prestarts the given Vm
     * @param vmGuid
     * @return whether or not succeeded to prestart the Vm
     */
    private boolean prestartVm(Guid vmGuid) {
        boolean prestartVmSucceeded = false;
        if (VmPoolCommandBase.canAttachNonPrestartedVmToUser(vmGuid)) {
            VM vmToPrestart = DbFacade.getInstance().getVmDao().get(vmGuid);
            if (runVmAsStateless(vmToPrestart)) {
                prestartVmSucceeded = true;
            }
        }
        return prestartVmSucceeded;
    }

    /**
     * Run the given VM as stateless
     * @param vm
     * @return
     */
    private boolean runVmAsStateless(VM vmToRunAsStateless) {
        log.infoFormat("Running Vm {0} as stateless", vmToRunAsStateless);
        boolean prestartingVmSucceeded = false;
        RunVmParams runVmParams = new RunVmParams(vmToRunAsStateless.getId());
        runVmParams.setEntityId(vmToRunAsStateless);
        runVmParams.setRunAsStateless(true);
        VdcReturnValueBase vdcReturnValue = Backend.getInstance().runInternalAction(VdcActionType.RunVm,
                runVmParams, ExecutionHandler.createInternalJobContext());
        prestartingVmSucceeded = vdcReturnValue.getSucceeded();
        if (prestartingVmSucceeded) {
            log.infoFormat("Running Vm {0} as stateless succeeded", vmToRunAsStateless);
        } else {
            log.infoFormat("Running Vm {0} as stateless failed", vmToRunAsStateless);
        }
        return prestartingVmSucceeded;
    }

    private static Log log = LogFactory.getLog(VmPoolMonitor.class);
}

package org.ovirt.engine.core.bll;

import java.util.List;

import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.RunVmParams;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.asynctasks.EntityInfo;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmPool;
import org.ovirt.engine.core.common.businessentities.VmPoolMap;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
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
                    Math.min(missingPrestartedVms, Config.<Integer> getValue(ConfigValues.VmPoolMonitorBatchSize));

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
        final int maxFailedAttempts = Config.<Integer> getValue(ConfigValues.VmPoolMonitorMaxAttempts);
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
        if (VmPoolCommandBase.canAttachNonPrestartedVmToUser(vmGuid)) {
            VM vmToPrestart = DbFacade.getInstance().getVmDao().get(vmGuid);
            return runVmAsStateless(vmToPrestart);
        }
        return false;
    }

    /**
     * Run the given VM as stateless
     * @param vm
     * @return
     */
    private boolean runVmAsStateless(VM vmToRunAsStateless) {
        log.infoFormat("Running Vm {0} as stateless", vmToRunAsStateless);
        RunVmParams runVmParams = new RunVmParams(vmToRunAsStateless.getId());
        runVmParams.setEntityInfo(new EntityInfo(VdcObjectType.VM, vmToRunAsStateless.getId()));
        runVmParams.setRunAsStateless(true);
        VdcReturnValueBase vdcReturnValue = Backend.getInstance().runInternalAction(VdcActionType.RunVm,
                runVmParams, ExecutionHandler.createInternalJobContext());
        boolean prestartingVmSucceeded = vdcReturnValue.getSucceeded();

        if (!prestartingVmSucceeded) {
            AuditLogableBase log = new AuditLogableBase();
            log.addCustomValue("VmPoolName", vmToRunAsStateless.getVmPoolName());
            AuditLogDirector.log(log, AuditLogType.VM_FAILED_TO_PRESTART_IN_POOL);
        }

        log.infoFormat("Running Vm {0} as stateless {1}",
                vmToRunAsStateless, prestartingVmSucceeded ? "succeeded" : "failed");
        return prestartingVmSucceeded;
    }

    private static final Log log = LogFactory.getLog(VmPoolMonitor.class);
}

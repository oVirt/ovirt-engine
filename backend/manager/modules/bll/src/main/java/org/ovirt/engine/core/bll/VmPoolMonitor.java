package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.BackendService;
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
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.utils.timer.OnTimerMethodAnnotation;
import org.ovirt.engine.core.utils.timer.SchedulerUtilQuartzImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class VmPoolMonitor implements BackendService {

    private static final Logger log = LoggerFactory.getLogger(VmPoolMonitor.class);

    private String poolMonitoringJobId;
    @Inject
    private SchedulerUtilQuartzImpl schedulerUtil;

    @PostConstruct
    private void init() {
        int vmPoolMonitorIntervalInMinutes = Config.<Integer>getValue(ConfigValues.VmPoolMonitorIntervalInMinutes);
        poolMonitoringJobId =
                schedulerUtil.scheduleAFixedDelayJob(
                        this,
                        "managePrestartedVmsInAllVmPools",
                        new Class[] {},
                        new Object[] {},
                        vmPoolMonitorIntervalInMinutes,
                        vmPoolMonitorIntervalInMinutes,
                        TimeUnit.MINUTES);
    }

    /**
     * Goes over each Vmpool, and makes sure there are at least as much prestarted Vms as defined in the prestarted_vms
     * field
     */
    @OnTimerMethodAnnotation("managePrestartedVmsInAllVmPools")
    public void managePrestartedVmsInAllVmPools() {
        getAllVmPools().stream()
        .filter(pool -> pool.getPrestartedVms() > 0)
        .forEach(this::managePrestartedVmsInPool);
    }

    private List<VmPool> getAllVmPools() {
        return DbFacade.getInstance().getVmPoolDao().getAll();
    }

    public void triggerPoolMonitoringJob() {
        schedulerUtil.triggerJob(poolMonitoringJobId);
    }

    /**
     * Checks how many prestarted vms are missing in the pool, and attempts to prestart either that amount or BATCH_SIZE
     * (the minimum between the two).
     */
    private void managePrestartedVmsInPool(VmPool vmPool) {
        int prestartedVms = VmPoolCommandBase.getNumOfPrestartedVmsInPool(vmPool, new ArrayList<>());
        int missingPrestartedVms = vmPool.getPrestartedVms() - prestartedVms;
        if (missingPrestartedVms > 0) {
            // We do not want to start too many vms at once
            int numOfVmsToPrestart =
                    Math.min(missingPrestartedVms, Config.<Integer> getValue(ConfigValues.VmPoolMonitorBatchSize));

            log.info("VmPool '{}' is missing {} prestarted VMs, attempting to prestart {} VMs",
                    vmPool.getVmPoolId(),
                    missingPrestartedVms,
                    numOfVmsToPrestart);
            prestartVms(vmPool, numOfVmsToPrestart);
        }
    }

    /***
     * Prestarts the given amount of vmsToPrestart, in the given Vm Pool
     */
    private void prestartVms(VmPool vmPool, int numOfVmsToPrestart) {
        // Fetch all vms that are in status down
        List<VmPoolMap> vmPoolMaps = DbFacade.getInstance().getVmPoolDao()
                .getVmMapsInVmPoolByVmPoolIdAndStatus(vmPool.getVmPoolId(), VMStatus.Down);
        int failedAttempts = 0;
        int prestartedVmsCounter = 0;
        final int maxFailedAttempts = Config.<Integer> getValue(ConfigValues.VmPoolMonitorMaxAttempts);
        Map<String, Set<Guid>> failureReasonsForVms = new HashMap<>();
        if (vmPoolMaps != null && vmPoolMaps.size() > 0) {
            for (VmPoolMap map : vmPoolMaps) {
                if (failedAttempts < maxFailedAttempts && prestartedVmsCounter < numOfVmsToPrestart) {
                    List<String> messages = new ArrayList<>();
                    if (prestartVm(map.getVmId(), !vmPool.isStateful(), messages)) {
                        prestartedVmsCounter++;
                        failedAttempts = 0;
                    } else {
                        failedAttempts++;
                        collectVmPrestartFailureReasons(map.getVmId(), failureReasonsForVms, messages);
                    }
                } else {
                    // If we reached the required amount or we exceeded the number of allowed failures, stop
                    break;
                }
            }
            logResultOfPrestartVms(prestartedVmsCounter,
                    numOfVmsToPrestart,
                    vmPool.getVmPoolId(),
                    failureReasonsForVms);
        } else {
            log.info("No VMs available for prestarting");
        }
    }

    private void collectVmPrestartFailureReasons(Guid vmId, Map<String, Set<Guid>> failureReasons, List<String> messages) {
        String reason = messages.stream()
                .filter(EngineMessage::contains)
                .collect(Collectors.joining(", "));
        failureReasons.computeIfAbsent(reason, key -> new HashSet<>()).add(vmId);
    }

    /**
     * Logs the results of the attempt to prestart Vms in a Vm Pool
     */
    private void logResultOfPrestartVms(int prestartedVmsCounter,
            int numOfVmsToPrestart,
            Guid vmPoolId,
            Map<String, Set<Guid>> failureReasonsForVms) {
        if (prestartedVmsCounter > 0) {
            log.info("Prestarted {} VMs out of the {} required, in VmPool '{}'",
                    prestartedVmsCounter,
                    numOfVmsToPrestart,
                    vmPoolId);
        } else {
            log.warn("Failed to prestart any VMs for VmPool '{}'",
                    vmPoolId);
        }

        if (prestartedVmsCounter < numOfVmsToPrestart) {
            for (Map.Entry<String, Set<Guid>> entry : failureReasonsForVms.entrySet()) {
                log.warn("Failed to prestart VMs {} with reason {}",
                        entry.getValue(),
                        entry.getKey());
            }
        }
    }

    /**
     * Prestarts the given Vm
     * @return whether or not succeeded to prestart the Vm
     */
    private boolean prestartVm(Guid vmGuid, boolean runAsStateless, List<String> messages) {
        if (VmPoolCommandBase.canAttachNonPrestartedVmToUser(vmGuid, messages)) {
            VM vmToPrestart = DbFacade.getInstance().getVmDao().get(vmGuid);
            return runVmFromPool(vmToPrestart, runAsStateless);
        }
        return false;
    }

    /**
     * Run the given VM as stateless
     */
    private boolean runVmFromPool(VM vmToRun, boolean runAsStateless) {
        log.info("Running VM '{}' as {}", vmToRun, runAsStateless ? "stateless" : "stateful");
        RunVmParams runVmParams = new RunVmParams(vmToRun.getId());
        runVmParams.setEntityInfo(new EntityInfo(VdcObjectType.VM, vmToRun.getId()));
        runVmParams.setRunAsStateless(runAsStateless);
        VdcReturnValueBase vdcReturnValue = Backend.getInstance().runInternalAction(VdcActionType.RunVm,
                runVmParams, ExecutionHandler.createInternalJobContext());
        boolean prestartingVmSucceeded = vdcReturnValue.getSucceeded();

        if (!prestartingVmSucceeded) {
            AuditLogableBase log = new AuditLogableBase();
            log.addCustomValue("VmPoolName", vmToRun.getVmPoolName());
            new AuditLogDirector().log(log, AuditLogType.VM_FAILED_TO_PRESTART_IN_POOL);
        }

        log.info("Running VM '{}' as {} {}",
                vmToRun,
                runAsStateless ? "stateless" : "stateful",
                prestartingVmSucceeded ? "succeeded" : "failed");
        return prestartingVmSucceeded;
    }

}

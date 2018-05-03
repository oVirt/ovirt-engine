package org.ovirt.engine.core.bll;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.enterprise.concurrent.ManagedScheduledExecutorService;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.BackendService;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.RunVmParams;
import org.ovirt.engine.core.common.asynctasks.EntityInfo;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmPool;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogable;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableImpl;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.dao.VmPoolDao;
import org.ovirt.engine.core.dao.VmStaticDao;
import org.ovirt.engine.core.di.Injector;
import org.ovirt.engine.core.utils.threadpool.ThreadPools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class VmPoolMonitor implements BackendService {

    private static final Logger log = LoggerFactory.getLogger(VmPoolMonitor.class);

    private ScheduledFuture poolMonitoringJob;

    private long vmPoolMonitorIntervalInMinutes;

    private Set<Guid> startingVms = ConcurrentHashMap.newKeySet();

    @Inject
    private VmPoolHandler vmPoolHandler;
    @Inject
    private VmPoolDao vmPoolDao;
    @Inject
    private VmDao vmDao;
    @Inject
    private VmStaticDao vmStaticDao;
    @Inject
    protected BackendInternal backend;
    @Inject
    @ThreadPools(ThreadPools.ThreadPoolType.EngineScheduledThreadPool)
    private ManagedScheduledExecutorService schedulerService;
    private final ReentrantLock lock = new ReentrantLock();

    @PostConstruct
    private void init() {
        vmPoolMonitorIntervalInMinutes = Config.<Long>getValue(ConfigValues.VmPoolMonitorIntervalInMinutes);
        poolMonitoringJob =
                schedulerService.scheduleWithFixedDelay(
                        this::managePrestartedVmsInAllVmPools,
                        vmPoolMonitorIntervalInMinutes,
                        vmPoolMonitorIntervalInMinutes,
                        TimeUnit.MINUTES);
    }

    public void triggerPoolMonitoringJob() {
        try {
            poolMonitoringJob.cancel(false);
        } catch (Throwable t) {
            log.debug("Exception cancelling existing job: {}", ExceptionUtils.getRootCauseMessage(t));
        }
        poolMonitoringJob =
                schedulerService.scheduleWithFixedDelay(
                        this::managePrestartedVmsInAllVmPools,
                        0,
                        vmPoolMonitorIntervalInMinutes,
                        TimeUnit.MINUTES);
    }

    /**
     * Goes over each VM Pool and makes sure there are at least as much prestarted VMs as defined in the prestartedVms
     * field.
     */
    private void managePrestartedVmsInAllVmPools() {
        lock.lock();
        try {
            vmPoolDao.getAll()
                    .stream()
                    .filter(pool -> pool.getPrestartedVms() > 0)
                    .forEach(this::managePrestartedVmsInPool);
        } catch (Throwable t) {
            log.error("Exception managing prestarted VMs in all VM pools: {}", ExceptionUtils.getRootCauseMessage(t));
            log.debug("Exception", t);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Checks how many prestarted VMs are missing in the pool, and attempts to prestart either that amount or BATCH_SIZE
     * (the minimum between the two).
     */
    private void managePrestartedVmsInPool(VmPool vmPool) {
        int prestartedVms = getNumOfPrestartedVmsInPool(vmPool);
        int missingPrestartedVms = vmPool.getPrestartedVms() - prestartedVms;
        if (missingPrestartedVms > 0) {
            // We do not want to start too many VMs at once
            int numOfVmsToPrestart =
                    Math.min(missingPrestartedVms, Config.<Integer> getValue(ConfigValues.VmPoolMonitorBatchSize));

            log.info("VmPool '{}' is missing {} prestarted VMs, attempting to prestart {} VMs",
                    vmPool.getVmPoolId(),
                    missingPrestartedVms,
                    numOfVmsToPrestart);
            prestartVms(vmPool, numOfVmsToPrestart);
        }
    }

    private int getNumOfPrestartedVmsInPool(VmPool pool) {
        // TODO move to VmPoolHandler and rewrite. Worth to consider using a query that uses vms_monitoring_view
        List<VM> vmsInPool = vmDao.getAllForVmPool(pool.getVmPoolId());
        return vmsInPool == null ? 0
                : (int) vmsInPool.stream()
                        .filter(vm -> startingVms.contains(vm.getId())
                                || (vm.isStartingOrUp()
                                        && vmPoolHandler.isPrestartedVmFree(vm.getId(), pool.isStateful(), null)))
                        .count();
    }

    /***
     * Prestarts the given amount of VMs in the given VM Pool.
     */
    private void prestartVms(VmPool vmPool, int numOfVmsToPrestart) {
        int failedAttempts = 0;
        int prestartedVms = 0;
        int maxFailedAttempts = Config.<Integer> getValue(ConfigValues.VmPoolMonitorMaxAttempts);
        Map<String, Set<Guid>> failureReasons = new HashMap<>();

        Iterator<Guid> iterator =
                vmPoolHandler
                        .selectNonPrestartedVms(vmPool.getVmPoolId(),
                                (vmId, messages) -> collectVmPrestartFailureReasons(vmId, failureReasons, messages))
                        .iterator();
        while (failedAttempts < maxFailedAttempts && prestartedVms < numOfVmsToPrestart
                && iterator.hasNext()) {
            Guid vmId = iterator.next();
            if (prestartVm(vmId, !vmPool.isStateful(), vmPool.getName())) {
                prestartedVms++;
                failedAttempts = 0;
            } else {
                failedAttempts++;
            }
        }

        logResultOfPrestartVms(prestartedVms,
                numOfVmsToPrestart,
                vmPool.getVmPoolId(),
                failureReasons);

        if (prestartedVms == 0) {
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
     * Logs the results of the attempt to prestart VMs in a VM Pool.
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
     * Prestarts the given VM.
     * @return whether or not succeeded to prestart the VM
     */
    private boolean prestartVm(Guid vmGuid, boolean runAsStateless, String poolName) {
        VmStatic vmToPrestart = vmStaticDao.get(vmGuid);
        return runVmFromPool(vmToPrestart, runAsStateless, poolName);
    }

    /**
     * Run the given VM as stateless.
     */
    private boolean runVmFromPool(VmStatic vmToRun, boolean runAsStateless, String poolName) {
        log.info("Running VM '{}' as {}", vmToRun.getName(), runAsStateless ? "stateless" : "stateful");
        startingVm(vmToRun.getId());

        RunVmParams runVmParams = new RunVmParams(vmToRun.getId());
        runVmParams.setEntityInfo(new EntityInfo(VdcObjectType.VM, vmToRun.getId()));
        runVmParams.setRunAsStateless(runAsStateless);
        ActionReturnValue actionReturnValue = backend.runInternalAction(ActionType.RunVm,
                runVmParams,
                ExecutionHandler.createInternalJobContext().withLock(vmPoolHandler.createLock(vmToRun.getId())));
        boolean prestartingVmSucceeded = actionReturnValue.getSucceeded();

        if (!prestartingVmSucceeded) {
            startingVmCompleted(vmToRun.getId(), "RunVmCommand execution failed");

            AuditLogable log = new AuditLogableImpl();
            log.addCustomValue("VmPoolName", poolName);
            Injector.get(AuditLogDirector.class).log(log, AuditLogType.VM_FAILED_TO_PRESTART_IN_POOL);
        }

        log.info("Running VM '{}' as {} {}",
                vmToRun.getName(),
                runAsStateless ? "stateless" : "stateful",
                prestartingVmSucceeded ? "succeeded" : "failed");

        return prestartingVmSucceeded;
    }

    private void startingVm(Guid vmId) {
        startingVms.add(vmId);
    }


    public void startingVmCompleted(Guid vmId, String cause) {
        if (startingVms.remove(vmId)) {
            log.debug("Startup of VM {} completed ({})", vmId, cause);
        }
    }

}

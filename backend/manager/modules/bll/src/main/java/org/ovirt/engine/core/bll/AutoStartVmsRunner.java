package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.enterprise.concurrent.ManagedScheduledExecutorService;
import javax.inject.Inject;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.BackendService;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.RunVmParams;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.DateTime;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogable;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableImpl;
import org.ovirt.engine.core.dao.SnapshotDao;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.utils.lock.EngineLock;
import org.ovirt.engine.core.utils.lock.LockManager;
import org.ovirt.engine.core.utils.threadpool.ThreadPools;
import org.ovirt.engine.core.vdsbroker.monitoring.VmsMonitoring;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class acts as a service that automatically starts VMs.
 *
 * Every {@link ConfigValues#AutoStartVmsRunnerIntervalInSeconds} sec, this service wakes up, iterates over the VMs that were
 * registered for being automatically restarted (e.g., highly-available VMs that went down unexpectedly
 * are registered by the monitoring) and process each VM according to the following logic:
 *
 * - If the VM contains next-run configuration then we check every {@link ConfigValues#DelayToRunAutoStartVmIntervalInSeconds} sec
 * whether or not that configuration was applied (by {@link ProcessDownVmCommand}). If we detect that this
 * configuration does not exist anymore, i.e., was applied, we proceed with the restart procedure. Otherwise,
 * after {@link ConfigValues#MaxNumOfSkipsBeforeAutoStartVm} checks, we proceed with the restart procedure that would try to start
 * the VM using its current configuration.
 * - If the VM is locked by other module, we skip it in the current cycle and try again in the next cycle.
 * - If we managed to lock the VM, we check if it still needs to be automatically started. If not, we remove
 * it from the list of VMs to start and skip it (the VM will not be automatically started).
 * - Otherwise, we try to start the VM. If we immediately fail, we retry every {@link ConfigValues#RetryToRunAutoStartVmShortIntervalInSeconds}
 * sec to start it for {@link ConfigValues#NumOfTriesToRunFailedAutoStartVmInShortIntervals} times. When all those attempts immediately fail,
 * we remove the VM from the list of VMs to start and skip it (the VM will not be automatically started).
 * - Otherwise, we successfully scheduled an attempt to start the VM. From this point on, it is the monitoring
 * module ({@link VmsMonitoring}) that will track the VM and re-register it to this service in case of a failure.
 */
public abstract class AutoStartVmsRunner implements BackendService {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    @Inject
    private AuditLogDirector auditLogDirector;

    @Inject
    private LockManager lockManager;

    @Inject
    private BackendInternal backend;

    @Inject
    protected VmDao vmDao;

    @Inject
    private SnapshotDao snapshotDao;

    @Inject
    @ThreadPools(ThreadPools.ThreadPoolType.EngineScheduledThreadPool)
    private ManagedScheduledExecutorService executor;

    private Map<Guid, AutoStartVmToRestart> autoStartVmsToRestart;

    private Set<Guid> vmsToAdd;
    private final Object vmsToAddLock = new Object();

    @PostConstruct
    private void init() {
        autoStartVmsToRestart = getInitialVmsToStart().stream()
            .collect(Collectors.toMap(AutoStartVmToRestart::getVmId, a -> a));

        long autoStartVmsRunnerIntervalInSeconds =
                Config.<Long>getValue(ConfigValues.AutoStartVmsRunnerIntervalInSeconds);
        executor.scheduleWithFixedDelay(
                this::startFailedAutoStartVms,
                autoStartVmsRunnerIntervalInSeconds,
                autoStartVmsRunnerIntervalInSeconds,
                TimeUnit.SECONDS);
    }

    protected abstract Collection<AutoStartVmToRestart> getInitialVmsToStart();

    /**
     * Add the given VM IDs to the set of VMs which will be started in the next iteration.
     *
     * @param vmIds
     *              List of VM IDs to start in the next iteration of the job
     */
    public void addVmsToRun(List<Guid> vmIds) {
        if (vmIds.isEmpty()) {
            return;
        }

        synchronized (vmsToAddLock) {
            if (vmsToAdd == null) {
                vmsToAdd = new HashSet<>();
            }
            vmsToAdd.addAll(vmIds);
        }
    }

    private void startFailedAutoStartVms() {
        try {
            startFailedAutoStartVmsImpl();
        } catch (Throwable t) {
            log.error("Exception in startFailedAutoStartVms: {}", ExceptionUtils.getRootCauseMessage(t));
            log.debug("Exception", t);
        }
    }

    private void startFailedAutoStartVmsImpl() {
        processVmsToAdd();
        if (autoStartVmsToRestart.isEmpty()) {
            return;
        }

        final DateTime iterationStartTime = DateTime.getNow();

        Collection<Guid> vmIds = autoStartVmsToRestart.keySet();
        List<AutoStartVmToRestart> vmsToRestart = new ArrayList<>(autoStartVmsToRestart.values());

        Map<Guid, VM> vms = vmDao.getVmsByIds(vmIds).stream()
                .collect(Collectors.toMap(VM::getId, vm -> vm));

        for (AutoStartVmToRestart autoStartVmToRestart : vmsToRestart) {
            // if it is not the time to try to run the VM yet, skip it for now
            // (we'll try again in the next iteration)
            if (!autoStartVmToRestart.isTimeToRun(iterationStartTime)) {
                continue;
            }

            Guid vmId = autoStartVmToRestart.getVmId();
            VM vm = vms.get(vmId);

            if (!isVmNeedsToBeAutoStarted(vm)) {
                autoStartVmsToRestart.remove(vmId);
                continue;
            }

            if (isNextRunConfiguration(vmId)) {
                // if the NextRun config exists then give the ProcessDownVmCommand time to apply it
                log.debug("NextRun config found for '{}' vm, the RunVm will be delayed", vm.getName());
                if (autoStartVmToRestart.delayNextTimeToRun(iterationStartTime)) {
                    // Skip attempt to run the VM for now.
                    // The priority is to run the VM even if the NextRun fails to be applied
                    continue;
                }
                // Waiting for NextRun config is over, let's run the VM even with the non-applied Next-Run
                log.warn("Failed to wait for the NextRun config to be applied on vm '{}', trying to run the VM anyway", vm.getName());
            }

            EngineLock runVmLock = createEngineLockForRunVm(vmId);

            // try to acquire the required lock for running the VM, if the lock cannot be
            // acquired, skip for now  and we'll try again in the next iteration
            if (!acquireLock(runVmLock)) {
                log.debug("Could not acquire lock for auto starting VM '{}'", vm.getName());
                continue;
            }

            // Test again, after acquiring the lock
            if (!isVmNeedsToBeAutoStarted(vm)) {
                // if the VM doesn't need to be auto started anymore, release the lock and
                // remove the VM from the collection of VMs that should be auto started
                releaseLock(runVmLock);
                autoStartVmsToRestart.remove(vmId);
                continue;
            }

            if (runVm(vm.getId(), runVmLock)) {
                // the VM reached WaitForLunch, so from now on this job is not responsible
                // to auto start it, future failures will be detected by the monitoring
                autoStartVmsToRestart.remove(vmId);
                continue;
            }

            logFailedAttemptToRestartVm(vm);
            if (!autoStartVmToRestart.scheduleNextTimeToRun(iterationStartTime)) {
                // if we could not schedule the next time to run the VM, it means
                // that we reached the maximum number of tried so don't try anymore
                autoStartVmsToRestart.remove(vmId);
                logFailureToRestartVm(vm);
            }
        }
    }

    private void processVmsToAdd() {
        Set<Guid> vms = vmsToAdd;
        synchronized (vmsToAddLock) {
            vmsToAdd = null;
        }

        if (vms == null) {
            return;
        }

        // The VMs are added even if they are already there, this resets the counters
        vms.forEach(vmId -> autoStartVmsToRestart.put(vmId, createAutoStartVmToRestart(vmId)));
    }

    /**
     * @return True if the VM has a next-run configuration to be applied
     */
    private boolean isNextRunConfiguration(Guid vmId) {
        return snapshotDao.exists(vmId, Snapshot.SnapshotType.NEXT_RUN);
    }

    private boolean acquireLock(EngineLock lock) {
        return lockManager.acquireLock(lock).getFirst();
    }

    private void releaseLock(EngineLock lock) {
        lockManager.releaseLock(lock);
    }

    protected abstract AutoStartVmToRestart createAutoStartVmToRestart(Guid vmId);

    protected abstract boolean isVmNeedsToBeAutoStarted(VM vm);

    private void logFailedAttemptToRestartVm(VM vm) {
        logVmEvent(vm, getRestartFailedAuditLogType());
    }

    protected abstract AuditLogType getRestartFailedAuditLogType();

    private void logFailureToRestartVm(VM vm) {
        logVmEvent(vm, getExceededMaxNumOfRestartsAuditLogType());
    }

    protected abstract AuditLogType getExceededMaxNumOfRestartsAuditLogType();

    private void logVmEvent(VM vm, AuditLogType restartFailedAuditLogType) {
        AuditLogable event = createVmEvent(vm);
        auditLogDirector.log(event, restartFailedAuditLogType);
    }

    private AuditLogable createVmEvent(VM vm) {
        AuditLogable event = new AuditLogableImpl();
        event.setVmId(vm.getId());
        event.setVmName(vm.getName());
        return event;
    }

    private EngineLock createEngineLockForRunVm(Guid vmId) {
        return new EngineLock(
                RunVmCommandBase.getExclusiveLocksForRunVm(vmId, getLockMessage()),
                RunVmCommandBase.getSharedLocksForRunVm());
    }

    private String getLockMessage() {
        return EngineMessage.ACTION_TYPE_FAILED_OBJECT_LOCKED.name();
    }

    private boolean runVm(Guid vmId, EngineLock lock) {
        return backend.runInternalAction(
                ActionType.RunVm,
                new RunVmParams(vmId),
                ExecutionHandler.createInternalJobContext(lock)).getSucceeded();
    }

    protected static class AutoStartVmToRestart {
        /** The earliest date in Java */
        private static final Date MIN_DATE = DateTime.getMinValue();
        /** How long to wait before rerun HA VM that failed to start (not because of lock acquisition) */
        protected static final int RETRY_TO_RUN_AUTO_START_VM_SHORT_INTERVAL =
                Config.<Integer> getValue(ConfigValues.RetryToRunAutoStartVmShortIntervalInSeconds);
        /** How many times to try to restart highly available VM that went down */
        protected static final int MAXIMUM_NUM_OF_TRIES_TO_AUTO_START_VM_IN_SHORT_INTERVALS =
                Config.<Integer> getValue(ConfigValues.NumOfTriesToRunFailedAutoStartVmInShortIntervals);
        private static final int MAXIMUM_NUM_OF_SKIPS_BEFORE_AUTO_START_VM =
                Config.<Integer> getValue(ConfigValues.MaxNumOfSkipsBeforeAutoStartVm);
        /** How long to wait before next check whether the NextRun configuration is applied */
        private static final int DELAY_TO_RUN_AUTO_START_VM_INTERVAL =
                Config.<Integer> getValue(ConfigValues.DelayToRunAutoStartVmIntervalInSeconds);

        /** The next time we should try to run the VM */
        protected Date timeToRunTheVm;
        /** Number of tries that were made so far to run the VM */
        protected int numOfRuns;
        /** Number of skips that were made so far before attempt to run the VM */
        private int numOfSkips;
        /** The ID of the VM */
        private Guid vmId;

        AutoStartVmToRestart(Guid vmId) {
            this.vmId = vmId;
            timeToRunTheVm = MIN_DATE;
        }

        /**
         * Set the next time we should try to rerun the VM.
         * If we reached the maximum number of tries, the method returns false.
         */
        public boolean scheduleNextTimeToRun(DateTime currentTime) {
            this.timeToRunTheVm = currentTime.addSeconds(RETRY_TO_RUN_AUTO_START_VM_SHORT_INTERVAL);
            return ++numOfRuns < MAXIMUM_NUM_OF_TRIES_TO_AUTO_START_VM_IN_SHORT_INTERVALS;
        }

        /**
         * Skip this attempt to run the VM.
         * Return false if count of skips reached thresh-hold.
         * Do not increase the attempt-counter 'numOfRuns'.
         */
        boolean delayNextTimeToRun(DateTime currentTime) {
            this.timeToRunTheVm = currentTime.addSeconds(DELAY_TO_RUN_AUTO_START_VM_INTERVAL);
            numOfSkips++;
            numOfSkips %= MAXIMUM_NUM_OF_SKIPS_BEFORE_AUTO_START_VM;
            return numOfSkips != 0;
        }

        boolean isTimeToRun(Date time) {
            return timeToRunTheVm == MIN_DATE || time.compareTo(timeToRunTheVm) >= 0;
        }

        Guid getVmId() {
            return vmId;
        }
    }
}

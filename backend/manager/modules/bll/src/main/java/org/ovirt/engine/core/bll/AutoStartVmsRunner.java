package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.BackendService;
import org.ovirt.engine.core.common.action.RunVmParams;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.DateTime;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.dao.VmDynamicDao;
import org.ovirt.engine.core.utils.lock.EngineLock;
import org.ovirt.engine.core.utils.lock.LockManager;
import org.ovirt.engine.core.utils.timer.OnTimerMethodAnnotation;
import org.ovirt.engine.core.utils.timer.SchedulerUtilQuartzImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AutoStartVmsRunner implements BackendService {

    /** How long to wait before rerun HA VM that failed to start (not because of lock acquisition) */
    private static final int RETRY_TO_RUN_AUTO_START_VM_INTERVAL =
            Config.<Integer> getValue(ConfigValues.RetryToRunAutoStartVmIntervalInSeconds);

    protected final Logger log = LoggerFactory.getLogger(getClass());

    @Inject
    private AuditLogDirector auditLogDirector;

    @Inject
    private SchedulerUtilQuartzImpl schedulerUtil;

    @Inject
    private LockManager lockManager;

    @Inject
    private BackendInternal backend;

    @Inject
    private VmDynamicDao vmDynamicDao;

    @Inject
    private VmDao vmDao;

    /** Records of VMs that need to be started */
    private CopyOnWriteArraySet<AutoStartVmToRestart> autoStartVmsToRestart;

    @PostConstruct
    private void init() {
        autoStartVmsToRestart = new CopyOnWriteArraySet<>(getInitialVmsToStart());

        int autoStartVmsRunnerIntervalInSeconds =
                Config.<Integer>getValue(ConfigValues.AutoStartVmsRunnerIntervalInSeconds);
        schedulerUtil.scheduleAFixedDelayJob(
                this,
                "startFailedAutoStartVms",
                new Class[] {},
                new Object[] {},
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
        ArrayList<AutoStartVmToRestart> vmsToAdd = new ArrayList<>(vmIds.size());
        for (Guid vmId: vmIds) {
            vmsToAdd.add(new AutoStartVmToRestart(vmId));
        }
        autoStartVmsToRestart.addAll(vmsToAdd);
    }

    @OnTimerMethodAnnotation("startFailedAutoStartVms")
    public void startFailedAutoStartVms() {
        LinkedList<AutoStartVmToRestart> vmsToRemove = new LinkedList<>();
        final DateTime iterationStartTime = DateTime.getNow();
        final Date nextTimeOfRetryToRun = iterationStartTime.addSeconds(RETRY_TO_RUN_AUTO_START_VM_INTERVAL);

        for (AutoStartVmToRestart autoStartVmToRestart : autoStartVmsToRestart) {
            // if it is not the time to try to run the VM yet, skip it for now
            // (we'll try again in the next iteration)
            if (!autoStartVmToRestart.isTimeToRun(iterationStartTime)) {
                continue;
            }

            Guid vmId = autoStartVmToRestart.getVmId();
            EngineLock runVmLock = createEngineLockForRunVm(vmId);

            // try to acquire the required lock for running the VM, if the lock cannot be
            // acquired, skip for now  and we'll try again in the next iteration
            if (!acquireLock(runVmLock)) {
                log.debug("Could not acquire lock for auto starting VM '{}'", vmId);
                continue;
            }

            if (!isVmNeedsToBeAutoStarted(vmId)) {
                // if the VM doesn't need to be auto started anymore, release the lock and
                // remove the VM from the collection of VMs that should be auto started
                releaseLock(runVmLock);
                vmsToRemove.add(autoStartVmToRestart);
                continue;
            }

            if (runVm(vmId, runVmLock)) {
                // the VM reached WaitForLunch, so from now on this job is not responsible
                // to auto start it, future failures will be detected by the monitoring
                vmsToRemove.add(autoStartVmToRestart);
            }
            else {
                logFailedAttemptToRestartVm(vmId);

                if (!autoStartVmToRestart.scheduleNextTimeToRun(nextTimeOfRetryToRun)) {
                    // if we could not schedule the next time to run the VM, it means
                    // that we reached the maximum number of tried so don't try anymore
                    vmsToRemove.add(autoStartVmToRestart);
                    logFailureToRestartVm(vmId);
                }
            }
        }

        autoStartVmsToRestart.removeAll(vmsToRemove);
    }

    private boolean acquireLock(EngineLock lock) {
        return lockManager.acquireLock(lock).getFirst();
    }

    private void releaseLock(EngineLock lock) {
        lockManager.releaseLock(lock);
    }

    protected abstract boolean isVmNeedsToBeAutoStarted(Guid vmId);

    private void logFailedAttemptToRestartVm(Guid vmId) {
        AuditLogableBase event = new AuditLogableBase();
        event.setVmId(vmId);
        auditLogDirector.log(event, getRestartFailedAuditLogType());
    }

    protected abstract AuditLogType getRestartFailedAuditLogType();

    private void logFailureToRestartVm(Guid vmId) {
        AuditLogableBase event = new AuditLogableBase();
        event.setVmId(vmId);
        auditLogDirector.log(event, getExceededMaxNumOfRestartsAuditLogType());
    }

    protected abstract AuditLogType getExceededMaxNumOfRestartsAuditLogType();

    private EngineLock createEngineLockForRunVm(Guid vmId) {
        return new EngineLock(
                RunVmCommandBase.getExclusiveLocksForRunVm(vmId, getLockMessage()),
                RunVmCommandBase.getSharedLocksForRunVm());
    }

    private String getLockMessage() {
        return EngineMessage.ACTION_TYPE_FAILED_OBJECT_LOCKED.name();
    }

    protected VmDynamicDao getVmDynamicDao() {
        return vmDynamicDao;
    }

    protected VmDao getVmDao() {
        return vmDao;
    }

    private boolean runVm(Guid vmId, EngineLock lock) {
        return backend.runInternalAction(
                VdcActionType.RunVm,
                new RunVmParams(vmId),
                ExecutionHandler.createInternalJobContext(lock)).getSucceeded();
    }

    protected static class AutoStartVmToRestart {
        /** The earliest date in Java */
        private static final Date MIN_DATE = DateTime.getMinValue();
        /** How many times to try to restart highly available VM that went down */
        private static final int MAXIMUM_NUM_OF_TRIES_TO_AUTO_START_VM =
                Config.<Integer> getValue(ConfigValues.MaxNumOfTriesToRunFailedAutoStartVm);

        /** The next time we should try to run the VM */
        private Date timeToRunTheVm;
        /** Number of tries that were made so far to run the VM */
        private int numOfRuns;
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
        boolean scheduleNextTimeToRun(Date timeToRunTheVm) {
            this.timeToRunTheVm = timeToRunTheVm;
            return ++numOfRuns < MAXIMUM_NUM_OF_TRIES_TO_AUTO_START_VM;
        }

        boolean isTimeToRun(Date time) {
            return timeToRunTheVm == MIN_DATE || time.compareTo(timeToRunTheVm) >= 0;
        }

        Guid getVmId() {
            return vmId;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof AutoStartVmToRestart)) {
                return false;
            }
            AutoStartVmToRestart other = (AutoStartVmToRestart) obj;
            return Objects.equals(vmId, other.vmId);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(vmId);
        }
    }
}

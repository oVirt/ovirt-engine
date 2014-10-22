package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArraySet;

import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.RunVmParams;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDynamic;
import org.ovirt.engine.core.common.businessentities.VmExitStatus;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.compat.DateTime;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.dao.VmDAO;
import org.ovirt.engine.core.dao.VmDynamicDAO;
import org.ovirt.engine.core.utils.lock.EngineLock;
import org.ovirt.engine.core.utils.lock.LockManagerFactory;
import org.ovirt.engine.core.utils.timer.OnTimerMethodAnnotation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class represent a job which is responsible for running HA VMs
 */
public class AutoStartVmsRunner {

    private static final Logger log = LoggerFactory.getLogger(AutoStartVmsRunner.class);
    /** How long to wait before rerun HA VM that failed to start (not because of lock acquisition) */
    private static final int RETRY_TO_RUN_HA_VM_INTERVAL =
            Config.<Integer> getValue(ConfigValues.RetryToRunAutoStartVmIntervalInSeconds);
    private static AutoStartVmsRunner instance = new AutoStartVmsRunner();

    /** Records of HA VMs that need to be restarted */
    private CopyOnWriteArraySet<AutoStartVmToRestart> autoStartVmsToRestart;

    public static AutoStartVmsRunner getInstance() {
        return instance;
    }

    private AutoStartVmsRunner() {
        // There might be HA VMs which went down just before the engine stopped, we detected
        // the failure and updated the DB but didn't made it to rerun the VM. So here we'll
        // take all the HA VMs which are down because of an error and add them to the set
        List<VM> failedAutoStartVms = getVmDao().getAllFailedAutoStartVms();
        ArrayList<AutoStartVmToRestart> initialFailedVms = new ArrayList<>(failedAutoStartVms.size());
        for (VM vm: failedAutoStartVms) {
            log.info("Found HA VM which is down because of an error, trying to restart it, VM '{}' ({})",
                    vm.getName(), vm.getId());
            initialFailedVms.add(new AutoStartVmToRestart(vm.getId()));
        }
        autoStartVmsToRestart = new CopyOnWriteArraySet<>(initialFailedVms);
    }

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
        final Date nextTimeOfRetryToRun = iterationStartTime.addSeconds(RETRY_TO_RUN_HA_VM_INTERVAL);

        for(AutoStartVmToRestart autoStartVmToRestart: autoStartVmsToRestart) {
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
                log.debug("Could not acquire lock for running HA VM '{}'", vmId);
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
                logFailedAttemptToRestartHighlyAvailableVm(vmId);

                if (!autoStartVmToRestart.scheduleNextTimeToRun(nextTimeOfRetryToRun)) {
                    // if we could not schedule the next time to run the VM, it means
                    // that we reached the maximum number of tried so don't try anymore
                    vmsToRemove.add(autoStartVmToRestart);
                    logFailureToRestartHighlyAvailableVm(vmId);
                }
            }
        }

        autoStartVmsToRestart.removeAll(vmsToRemove);
    }

    private boolean acquireLock(EngineLock lock) {
        return LockManagerFactory.getLockManager().acquireLock(lock).getFirst();
    }

    private void releaseLock(EngineLock lock) {
        LockManagerFactory.getLockManager().releaseLock(lock);
    }

    private void logFailedAttemptToRestartHighlyAvailableVm(Guid vmId) {
        AuditLogableBase event = new AuditLogableBase();
        event.setVmId(vmId);
        AuditLogDirector.log(event, AuditLogType.HA_VM_RESTART_FAILED);
    }

    private void logFailureToRestartHighlyAvailableVm(Guid vmId) {
        AuditLogableBase event = new AuditLogableBase();
        event.setVmId(vmId);
        AuditLogDirector.log(event, AuditLogType.EXCEEDED_MAXIMUM_NUM_OF_RESTART_HA_VM_ATTEMPTS);
    }

    private boolean isVmNeedsToBeAutoStarted(Guid vmId) {
        VmDynamic vmDynamic = getVmDynamicDao().get(vmId);
        return vmDynamic.getStatus() == VMStatus.Down &&
                vmDynamic.getExitStatus() == VmExitStatus.Error;
    }

    private EngineLock createEngineLockForRunVm(Guid vmId) {
        return new EngineLock(
                RunVmCommandBase.getExclusiveLocksForRunVm(vmId, getLockMessage()),
                RunVmCommandBase.getSharedLocksForRunVm());
    }

    protected String getLockMessage() {
        return VdcBllMessages.ACTION_TYPE_FAILED_OBJECT_LOCKED.name();
    }

    protected VmDynamicDAO getVmDynamicDao() {
        return DbFacade.getInstance().getVmDynamicDao();
    }

    protected VmDAO getVmDao() {
        return DbFacade.getInstance().getVmDao();
    }

    private boolean runVm(Guid vmId, EngineLock lock) {
        return Backend.getInstance().runInternalAction(
                VdcActionType.RunVm,
                new RunVmParams(vmId),
                ExecutionHandler.createInternalJobContext(lock)).getSucceeded();
    }

    private static class AutoStartVmToRestart {
        /** The earliest date in Java */
        private static final Date MIN_DATE = DateTime.getMinValue();
        /** How many times to try to restart highly available VM that went down */
        private static final int MAXIMUM_NUM_OF_TRIES_TO_RESTART_HA_VM =
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
            return ++numOfRuns < MAXIMUM_NUM_OF_TRIES_TO_RESTART_HA_VM;
        }

        boolean isTimeToRun(Date time) {
            return timeToRunTheVm == MIN_DATE || time.compareTo(timeToRunTheVm) >= 0;
        }

        Guid getVmId() {
            return vmId;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof AutoStartVmToRestart) {
                return vmId.equals(((AutoStartVmToRestart) obj).vmId);
            }
            return super.equals(obj);
        }

        @Override
        public int hashCode() {
            return vmId.hashCode();
        }
    }
}

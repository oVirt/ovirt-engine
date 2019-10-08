package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.inject.Singleton;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmExitStatus;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.DateTime;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.TimeSpan;

/**
 * This class represent a job which is responsible for running HA VMs
 *
 * Detailed behavior is documented in the base class.
 *
 * This class overrides the restart behavior to the following:
 * - The VM start is retried every {@link ConfigValues#RetryToRunAutoStartVmShortIntervalInSeconds} seconds
 * for {@link ConfigValues#NumOfTriesToRunFailedAutoStartVmInShortIntervals} times.
 * - When all those attempts fail, we retry every {@link ConfigValues#RetryToRunAutoStartVmLongIntervalInSeconds} seconds.
 */
@Singleton
public class HaAutoStartVmsRunner extends AutoStartVmsRunner {

    public HaAutoStartVmsRunner() {
        super(true);
    }

    @Override
    protected Collection<AutoStartVmToRestart> getInitialVmsToStart() {
        // There might be HA VMs which went down just before the engine stopped, we detected
        // the failure and updated the DB but didn't made it to rerun the VM. So here we'll
        // take all the HA VMs which are down because of an error and add them to the set
        List<VM> failedAutoStartVms = vmDao.getAllFailedAutoStartVms();
        ArrayList<AutoStartVmToRestart> initialFailedVms = new ArrayList<>(failedAutoStartVms.size());
        for (VM vm: failedAutoStartVms) {
            log.info("Found HA VM which is down because of an error, trying to restart it, VM '{}' ({})",
                    vm.getName(), vm.getId());
            initialFailedVms.add(createAutoStartVmToRestart(vm.getId()));
        }
        return initialFailedVms;
    }

    @Override
    protected boolean vmNeedsToBeAutoStarted(VM vm) {
        return vm.isAutoStartup() &&
                vm.getStatus() == VMStatus.Down &&
                vm.getExitStatus() == VmExitStatus.Error;
    }

    @Override
    protected boolean shouldWaitForVmToStart(VM vm) {
        return vm.isAutoStartup();
    }

    @Override
    protected AutoStartVmToRestart createAutoStartVmToRestart(Guid vmId) {
        return new HaVmToRestart(vmId);
    }

    @Override
    protected AuditLogType getRestartFailedAuditLogType() {
        return AuditLogType.HA_VM_RESTART_FAILED;
    }

    @Override
    protected AuditLogType getExceededMaxNumOfRestartsAuditLogType() {
        return AuditLogType.EXCEEDED_MAXIMUM_NUM_OF_RESTART_HA_VM_ATTEMPTS;
    }

    private static class  HaVmToRestart extends AutoStartVmToRestart {
        private static final int RETRY_TO_RUN_AUTO_START_VM_LONG_INTERVAL =
                Config.<Integer> getValue(ConfigValues.RetryToRunAutoStartVmLongIntervalInSeconds);
        private static final long MAXIMUM_TIME_AUTO_START_BLOCKED_ON_PRIORITY =
                Config.<Integer> getValue(ConfigValues.MaxTimeAutoStartBlockedOnPriority);

        private long totalBlockedMs;
        private Date lastBlockedTime;

        public HaVmToRestart(Guid vmId) {
            super(vmId);
            totalBlockedMs = 0;
        }

        @Override
        public boolean scheduleNextTimeToRun(DateTime currentTime) {
            ++numOfRuns;
            int delay = numOfRuns < MAXIMUM_NUM_OF_TRIES_TO_AUTO_START_VM_IN_SHORT_INTERVALS ?
                    RETRY_TO_RUN_AUTO_START_VM_SHORT_INTERVAL :
                    RETRY_TO_RUN_AUTO_START_VM_LONG_INTERVAL;

            timeToRunTheVm = currentTime.addSeconds(delay);
            return true;
        }

        @Override
        public boolean isBlockedOnPriority(int priority, DateTime currentTime) {
            if (lastBlockedTime != null) {
                totalBlockedMs += currentTime.getTime() - lastBlockedTime.getTime();
                lastBlockedTime = null;
            }

            if (totalBlockedMs > MAXIMUM_TIME_AUTO_START_BLOCKED_ON_PRIORITY * TimeSpan.MS_PER_SECOND) {
                return false;
            }

            if (getVm().getPriority() >= priority) {
                return false;
            }

            lastBlockedTime = currentTime;
            return true;
        }
    }
}

package org.ovirt.engine.core.bll;

import java.util.LinkedList;
import java.util.concurrent.CopyOnWriteArraySet;

import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.RunVmParams;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.utils.lock.EngineLock;
import org.ovirt.engine.core.utils.lock.LockManager;
import org.ovirt.engine.core.utils.lock.LockManagerFactory;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;
import org.ovirt.engine.core.utils.timer.OnTimerMethodAnnotation;

public class AutoStartVmsRunner {

    private static Log log = LogFactory.getLog(AutoStartVmsRunner.class);
    private static AutoStartVmsRunner instance = new AutoStartVmsRunner();
    private CopyOnWriteArraySet<Guid> autoStartVmsToRun = new CopyOnWriteArraySet<>();

    public static AutoStartVmsRunner getInstance() {
        return instance;
    }

    private AutoStartVmsRunner() {
    }

    @OnTimerMethodAnnotation("startFailedAutoStartVms")
    public void startFailedAutoStartVms() {
        LinkedList<Guid> idsToRemove = new LinkedList<>();

        for (Guid vmId : autoStartVmsToRun) {
            EngineLock runVmLock = createEngineLockForRunVm(vmId, getLockMessage());

            if (!getLockManager().acquireLock(runVmLock).getFirst()) {
                continue;
            }

            runVm(vmId, runVmLock);

            idsToRemove.add(vmId);
        }

        autoStartVmsToRun.removeAll(idsToRemove);
    }

    private EngineLock createEngineLockForRunVm(Guid vmId, String lockMessage) {
        return new EngineLock(
                RunVmCommandBase.getExclusiveLocksForRunVm(vmId, lockMessage),
                RunVmCommandBase.getSharedLocksForRunVm());
    }

    protected String getLockMessage() {
        return VdcBllMessages.ACTION_TYPE_FAILED_OBJECT_LOCKED.name();
    }

    protected LockManager getLockManager() {
        return LockManagerFactory.getLockManager();
    }

    public void addVmToRun(Guid vmId) {
        autoStartVmsToRun.add(vmId);
    }

    private boolean runVm(Guid vmId, EngineLock lock) {
        boolean succeeded = Backend.getInstance().runInternalAction(
                VdcActionType.RunVm,
                new RunVmParams(vmId),
                ExecutionHandler.createInternalJobContext(lock)).getSucceeded();

        if (!succeeded) {
            final AuditLogableBase event = new AuditLogableBase();
            event.setVmId(vmId);
            AuditLogDirector.log(event, AuditLogType.HA_VM_RESTART_FAILED);
        }

        return succeeded;
    }
}

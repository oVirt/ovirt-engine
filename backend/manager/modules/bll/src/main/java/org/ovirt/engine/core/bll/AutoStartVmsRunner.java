package org.ovirt.engine.core.bll;

import java.util.concurrent.ConcurrentLinkedQueue;

import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.RunVmParams;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.DateTime;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;
import org.ovirt.engine.core.utils.timer.OnTimerMethodAnnotation;

public class AutoStartVmsRunner {

    private static Log log = LogFactory.getLog(AutoStartVmsRunner.class);
    private static AutoStartVmsRunner instance = new AutoStartVmsRunner();
    private ConcurrentLinkedQueue<Pair<DateTime, Guid>> autoStartVmsToRun = new ConcurrentLinkedQueue<>();

    public static AutoStartVmsRunner getInstance() {
        return instance;
    }

    private AutoStartVmsRunner() {
    }

    @OnTimerMethodAnnotation("startFailedAutoStartVms")
    public void startFailedAutoStartVms() {
        DateTime now = DateTime.getNow();
        while (autoStartVmsToRun.peek() != null && now.compareTo(autoStartVmsToRun.peek().getFirst()) > 0) {
            runVm(autoStartVmsToRun.poll().getSecond());
        }
    }

    public void addVmToRun(Guid vmId) {
        autoStartVmsToRun.add(new Pair<>(DateTime.getNow(), vmId));
    }

    private void runVm(Guid vmId) {
        final VdcReturnValueBase result = Backend.getInstance().runInternalAction(VdcActionType.RunVm,
                new RunVmParams(vmId),
                ExecutionHandler.createInternalJobContext());

        // Alert if the restart fails:
        if (!result.getSucceeded()) {
            final AuditLogableBase event = new AuditLogableBase();
            event.setVmId(vmId);
            AuditLogDirector.log(event, AuditLogType.HA_VM_RESTART_FAILED);
            // should insert to autoStartVmsToRun again?
        }
    }
}

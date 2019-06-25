package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.ProcessDownVmParameters;
import org.ovirt.engine.core.common.action.VdsActionParameters;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmExitStatus;
import org.ovirt.engine.core.common.businessentities.comparators.VmsComparer;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.vdscommands.SetVmStatusVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.UpdateVdsVMsClearedVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.dao.VmDynamicDao;

@NonTransactiveCommandAttribute
public class ClearNonResponsiveVdsVmsCommand<T extends VdsActionParameters> extends VdsCommand<T> {

    @Inject
    private HaAutoStartVmsRunner haAutoStartVmsRunner;
    @Inject
    private VmDynamicDao vmDynamicDao;
    @Inject
    private VmDao vmDao;

    /**
     * Constructor for command creation when compensation is applied on startup
     */
    protected ClearNonResponsiveVdsVmsCommand(Guid commandId) {
        super(commandId);
    }

    public ClearNonResponsiveVdsVmsCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_CLEAR_UNKNOWN_VMS : AuditLogType.USER_FAILED_CLEAR_UNKNOWN_VMS;
    }

    @Override
    protected void executeCommand() {
        List<VM> vms = vmDao.getAllRunningForVds(getVdsId());
        Collections.sort(vms, Collections.reverseOrder(new VmsComparer()));
        List<Guid> autoStartVmIdsToRerun = new ArrayList<>();
        for (VM vm : vms) {
            if (vm.isAutoStartup()) {
                autoStartVmIdsToRerun.add(vm.getId());
            }
            VDSReturnValue returnValue = runVdsCommand(VDSCommandType.SetVmStatus,
                            new SetVmStatusVDSCommandParameters(vm.getId(), VMStatus.Down, VmExitStatus.Error));
            // Write that this VM was shut down by host reboot or manual fence
            if (returnValue != null && returnValue.getSucceeded()) {
                logSettingVmToDown(vm);
            }

            runInternalActionWithTasksContext(ActionType.ProcessDownVm, new ProcessDownVmParameters(vm.getId(), true));
        }

        runVdsCommand(VDSCommandType.UpdateVdsVMsCleared,
                        new UpdateVdsVMsClearedVDSCommandParameters(getVdsId()));
        if (!autoStartVmIdsToRerun.isEmpty()) {
            haAutoStartVmsRunner.addVmsToRun(autoStartVmIdsToRerun);
        }
        setSucceeded(true);
    }

    @Override
    protected boolean validate() {
        if (getVds() == null) {
            return failValidation(EngineMessage.VDS_INVALID_SERVER_ID);

        }

        if (vmDynamicDao.isAnyVmRunOnVds(getVdsId())
                && getVds().getStatus() != VDSStatus.NonResponsive
                && getVds().getStatus() != VDSStatus.Reboot
                && getVds().getStatus() != VDSStatus.Kdumping) {
            return failValidation(EngineMessage.VDS_CANNOT_CLEAR_VMS_WRONG_STATUS);
        }

        return true;
    }

}

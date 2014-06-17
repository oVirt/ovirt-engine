package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.IdParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdsActionParameters;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmExitStatus;
import org.ovirt.engine.core.common.businessentities.comparators.VmsComparer;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.vdscommands.SetVmStatusVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.UpdateVdsVMsClearedVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;

@NonTransactiveCommandAttribute
public class ClearNonResponsiveVdsVmsCommand<T extends VdsActionParameters> extends VdsCommand<T> {

    /**
     * Constructor for command creation when compensation is applied on startup
     *
     * @param commandId
     */
    protected ClearNonResponsiveVdsVmsCommand(Guid commandId) {
        super(commandId);
    }

    public ClearNonResponsiveVdsVmsCommand(T parameters) {
        super(parameters);
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
        List<VM> vms = getVmDAO().getAllRunningForVds(getVdsId());
        Collections.sort(vms, Collections.reverseOrder(new VmsComparer()));
        List<Guid> autoStartVmIdsToRerun = new ArrayList<>();
        for (VM vm : vms) {
            if (vm.isAutoStartup()) {
                autoStartVmIdsToRerun.add(vm.getId());
            }
            VDSReturnValue returnValue = Backend
                    .getInstance()
                    .getResourceManager()
                    .RunVdsCommand(VDSCommandType.SetVmStatus,
                            new SetVmStatusVDSCommandParameters(vm.getId(), VMStatus.Down, VmExitStatus.Error));
            // Write that this VM was shut down by host reboot or manual fence
            if (returnValue != null && returnValue.getSucceeded()) {
                LogSettingVmToDown(getVds().getId(), vm.getId());
            }

            runInternalActionWithTasksContext(VdcActionType.ProcessDownVm, new IdParameters(vm.getId()));
        }

        Backend.getInstance()
                .getResourceManager()
                .RunVdsCommand(VDSCommandType.UpdateVdsVMsCleared,
                        new UpdateVdsVMsClearedVDSCommandParameters(getVdsId()));
        if (!autoStartVmIdsToRerun.isEmpty()) {
            AutoStartVmsRunner.getInstance().addVmsToRun(autoStartVmIdsToRerun);
        }
        setSucceeded(true);
    }

    @Override
    protected boolean canDoAction() {
        if (getVds() == null) {
            return failCanDoAction(VdcBllMessages.VDS_INVALID_SERVER_ID);

        }

        if (hasVMs()
                && getVds().getStatus() != VDSStatus.NonResponsive
                && getVds().getStatus() != VDSStatus.Reboot
                && getVds().getStatus() != VDSStatus.Kdumping) {
            return failCanDoAction(VdcBllMessages.VDS_CANNOT_CLEAR_VMS_WRONG_STATUS);
        }

        return true;
    }

    private boolean hasVMs() {
        return !getVmDAO().getAllRunningForVds(getVdsId()).isEmpty();
    }
}

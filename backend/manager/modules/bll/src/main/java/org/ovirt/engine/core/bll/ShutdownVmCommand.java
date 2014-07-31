package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.ShutdownVmParameters;
import org.ovirt.engine.core.common.action.StopVmParameters;
import org.ovirt.engine.core.common.action.StopVmTypeEnum;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.vdscommands.DestroyVmVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;

@NonTransactiveCommandAttribute(forceCompensation=true)
public class ShutdownVmCommand<T extends ShutdownVmParameters> extends StopVmCommandBase<T> {

    protected ShutdownVmCommand(Guid commandId) {
        super(commandId);
    }

    public ShutdownVmCommand(T shutdownVmParamsData) {
        this(shutdownVmParamsData, null);
    }

    public ShutdownVmCommand(T shutdownVmParamsData, CommandContext commandContext) {
        super(shutdownVmParamsData, commandContext);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        if (getSuspendedVm()) {
            return getSucceeded() ? AuditLogType.USER_STOP_SUSPENDED_VM : AuditLogType.USER_STOP_SUSPENDED_VM_FAILED;
        } else {
            return getSucceeded() ? AuditLogType.USER_INITIATED_SHUTDOWN_VM : AuditLogType.USER_FAILED_SHUTDOWN_VM;
        }
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__SHUTDOWN);
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__VM);
    }

    @Override
    protected void perform() {
        log.infoFormat("Entered (VM {0}).", getVm().getName());

        VmHandler.updateVmGuestAgentVersion(getVm());

        if (canShutdownVm()) {
            // shutting down desktop and waiting for it in a separate thread to
            // become 'down':
            log.infoFormat("Sending shutdown command for VM {0}.", getVmName());

            int secondsToWait = getParameters().getWaitBeforeShutdown() ? Config
                    .<Integer> getValue(ConfigValues.VmGracefulShutdownTimeout) : 0;

            // sending a shutdown command to the VM:
            setActionReturnValue(runVdsCommand(VDSCommandType.DestroyVm,
                            new DestroyVmVDSCommandParameters(getVdsId(), getVmId(), getParameters().getStopReason(), false, true, secondsToWait))
                    .getReturnValue());
        }
        else {
            // cannot shutdown -> send a StopVm command instead ('destroy'):
            // don't log -> log will appear for the StopVmCommand we are about to run:
            setCommandShouldBeLogged(false);

            log.infoFormat("Cannot shutdown VM {0}, status is not up. Stopping instead.", getVmName());

            StopVmParameters stopVmParams = new StopVmParameters(getVmId(), StopVmTypeEnum.CANNOT_SHUTDOWN);
            stopVmParams.setStopReason(getParameters().getStopReason());
            // stopVmParams.ParametersCurrentUser = CurrentUser;
            stopVmParams.setSessionId(getParameters().getSessionId());
            runInternalAction(VdcActionType.StopVm, stopVmParams);
        }

        setSucceeded(true);
    }

    private boolean canShutdownVm() {
        return getVm().getStatus() == VMStatus.Up &&
                (Boolean.TRUE.equals(getVm().getAcpiEnable()) || getVm().getHasAgent());
    }

    private static final Log log = LogFactory.getLog(ShutdownVmCommand.class);
}

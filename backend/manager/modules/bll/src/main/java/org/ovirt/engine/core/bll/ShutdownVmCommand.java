package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.ShutdownVmParameters;
import org.ovirt.engine.core.common.action.StopVmParameters;
import org.ovirt.engine.core.common.action.StopVmTypeEnum;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.vdscommands.DestroyVmVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@NonTransactiveCommandAttribute(forceCompensation=true)
public class ShutdownVmCommand<T extends ShutdownVmParameters> extends StopVmCommandBase<T> {
    private static final Logger log = LoggerFactory.getLogger(ShutdownVmCommand.class);

    protected ShutdownVmCommand(Guid commandId) {
        super(commandId);
    }

    public ShutdownVmCommand(T shutdownVmParamsData, CommandContext commandContext) {
        super(shutdownVmParamsData, commandContext);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        if (shouldSkipCommandExecutionCached()) {
            return logCommandExecutionSkipped("Shutdown VM");
        }
        if (getSuspendedVm()) {
            return getSucceeded() ? AuditLogType.USER_STOP_SUSPENDED_VM : AuditLogType.USER_STOP_SUSPENDED_VM_FAILED;
        } else {
            return getSucceeded() ? AuditLogType.USER_INITIATED_SHUTDOWN_VM : AuditLogType.USER_FAILED_SHUTDOWN_VM;
        }
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__ACTION__SHUTDOWN);
        addValidationMessage(EngineMessage.VAR__TYPE__VM);
    }

    @Override
    protected void perform() {
        log.info("Entered (VM '{}').", getVm().getName());

        vmHandler.updateVmGuestAgentVersion(getVm());

        if (canShutdownVm()) {
            // shutting down desktop and waiting for it in a separate thread to
            // become 'down':
            log.info("Sending shutdown command for VM '{}'.", getVmName());

            // sending a shutdown command to the VM:
            setActionReturnValue(runVdsCommand(VDSCommandType.DestroyVm, buildDestroyVmVDSCommandParameters())
                    .getReturnValue());
        } else {
            // cannot shutdown -> send a StopVm command instead ('destroy'):
            // don't log -> log will appear for the StopVmCommand we are about to run:
            setCommandShouldBeLogged(false);

            log.info("Cannot shutdown VM '{}', status is not up. Stopping instead.", getVmName());

            StopVmParameters stopVmParams = new StopVmParameters(getVmId(), StopVmTypeEnum.CANNOT_SHUTDOWN);
            stopVmParams.setStopReason(getParameters().getStopReason());
            // stopVmParams.ParametersCurrentUser = CurrentUser;
            stopVmParams.setSessionId(getParameters().getSessionId());
            runInternalAction(ActionType.StopVm, stopVmParams);
        }

        setSucceeded(true);
    }

    private DestroyVmVDSCommandParameters buildDestroyVmVDSCommandParameters() {
        DestroyVmVDSCommandParameters parameters = new DestroyVmVDSCommandParameters(getVdsId(), getVmId());
        parameters.setReason(getParameters().getStopReason());
        parameters.setGracefully(true);
        parameters.setSecondsToWait(getParameters().getWaitBeforeShutdown() ?
                Config.getValue(ConfigValues.VmGracefulShutdownTimeout)
                : 0);
        return parameters;
    }

    private boolean canShutdownVm() {
        return (getVm().getStatus() == VMStatus.Up
                || getVm().getStatus().isMigrating() && getVmManager().getLastStatusBeforeMigration() == VMStatus.Up)
                && (Boolean.TRUE.equals(getVm().getAcpiEnable()) || getVm().getHasAgent());
    }
}

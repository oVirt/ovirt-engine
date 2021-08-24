package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.kubevirt.KubevirtMonitoring;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.RebootVmParameters;
import org.ovirt.engine.core.common.action.ShutdownVmParameters;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.VdsAndVmIDVDSParametersBase;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VmDynamicDao;
import org.ovirt.engine.core.vdsbroker.ResourceManager;
import org.ovirt.engine.core.vdsbroker.VmManager;

@NonTransactiveCommandAttribute(forceCompensation = true)
public class RebootVmCommand<T extends RebootVmParameters> extends VmOperationCommandBase<T> {

    @Inject
    private ResourceManager resourceManager;
    @Inject
    private KubevirtMonitoring kubevirt;
    @Inject
    private VmDynamicDao vmDynamicDao;

    public RebootVmCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    public RebootVmCommand(Guid commandId) {
        super(commandId);
    }

    @Override
    protected void setActionMessageParameters() {
        super.setActionMessageParameters();
        addValidationMessage(EngineMessage.VAR__ACTION__RESTART);
        addValidationMessage(EngineMessage.VAR__TYPE__VM);
    }

    @Override
    protected void perform() {
        if (!getVm().isManaged()) {
            kubevirt.restart(getVm());
            setSucceeded(true);
            return;
        }
        VmManager vmManager = resourceManager.getVmManager(getVmId());
        if (isColdReboot()) {
            vmManager.lockVm();
            try {
                ActionReturnValue returnValue =
                        runInternalAction(ActionType.ShutdownVm, new ShutdownVmParameters(getVmId(), false));
                setReturnValue(returnValue);
                setSucceeded(returnValue.getSucceeded());
                if (getSucceeded()) {
                    resourceManager.getVmManager(getVmId()).setColdReboot(true);
                    vmDynamicDao.updateStatus(getVm().getId(), VMStatus.RebootInProgress);
                }
            } finally {
                vmManager.unlockVm();
            }
        } else {
            VDSReturnValue returnValue =
                    runVdsCommand(VDSCommandType.RebootVm, new VdsAndVmIDVDSParametersBase(getVdsId(), getVmId()));
            setActionReturnValue(returnValue.getReturnValue());
            setSucceeded(returnValue.getSucceeded());
        }
    }

    @Override
    protected boolean validate() {
        if (getVm() == null) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_VM_NOT_FOUND);
        }

        if (!getVm().isManaged()) {
            return true;
        }

        if (isVmDuringBackup() && !getParameters().isForceStop()) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_VM_IS_DURING_BACKUP);
        }

        if (!canRunActionOnNonManagedVm()) {
            return false;
        }

        if (getVm().getStatus() != VMStatus.Up && getVm().getStatus() != VMStatus.PoweringUp) {
            return failVmStatusIllegal();
        }

        return true;
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_REBOOT_VM : AuditLogType.USER_FAILED_REBOOT_VM;
    }

    private boolean isColdReboot() {
        boolean coldReboot = (getVm().isRunOnce() && getVm().isVolatileRun()) || getVm().isNextRunConfigurationExists();

        log.info(
                "VM '{}' is performing {} reboot; run once: '{}', running as volatile: '{}', has next run configuration: '{}'",
                getVm().getName(),
                coldReboot ? "cold" : "warm",
                getVm().isRunOnce(),
                getVm().isVolatileRun(),
                getVm().isNextRunConfigurationExists());
        return coldReboot;
    }
}

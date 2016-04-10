package org.ovirt.engine.core.bll;

import java.util.Collections;
import java.util.Map;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.RemoveVmFromPoolParameters;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;

public class RemoveVmFromPoolCommand<T extends RemoveVmFromPoolParameters> extends VmPoolCommandBase<T> {

    public RemoveVmFromPoolCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
        setVmId(parameters.getVmId());
        if (getVm() != null) {
            setVmPoolId(getVm().getVmPoolId());
        }
    }

    @Override
    protected boolean validate() {
        if (getVm() == null) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_VM_NOT_FOUND);
        }

        if (getVmPoolId() == null) {
            return failValidation(EngineMessage.VM_POOL_CANNOT_DETACH_VM_NOT_ATTACHED_TO_POOL);
        }

        if (getVm().isRunningOrPaused() || getVm().getStatus() == VMStatus.Unknown) {
            return failValidation(EngineMessage.VM_POOL_CANNOT_REMOVE_RUNNING_VM_FROM_POOL);
        }

        return true;
    }

    @Override
    protected void executeCommand() {
        if (getVmPoolId() != null) {
            getVmPoolDao().removeVmFromVmPool(getVmId());

            if (getParameters().isUpdatePrestartedVms()) {
                getVmPoolDao().boundVmPoolPrestartedVms(getVmPoolId());
            }

            setSucceeded(true);
        }
    }

    @Override
    protected Map<String, Pair<String, String>> getSharedLocks() {
        return getParameters().isUpdatePrestartedVms()
                ? Collections.singletonMap(getVmPoolId().toString(),
                        LockMessagesMatchUtil.makeLockingPair(LockingGroup.VM_POOL, getVmBeingRemovedFromPoolMessage()))
                : null;
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(LockProperties.Scope.Execution);
    }

    private String getVmBeingRemovedFromPoolMessage() {
        StringBuilder builder = new StringBuilder(
                EngineMessage.ACTION_TYPE_FAILED_VM_IS_BEING_REMOVED_FROM_POOL.name());
        if (getVm() != null) {
            builder.append(String.format("$VmName %1$s", getVm().getName()));
        }
        if (getVmPool() != null) {
            builder.append(String.format("$VmPoolName %1$s", getVmPool().getName()));
        }
        return builder.toString();
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_REMOVE_VM_FROM_POOL : AuditLogType.USER_REMOVE_VM_FROM_POOL_FAILED;
    }

}

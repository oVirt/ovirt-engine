package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.AddVmToPoolParameters;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmPool;
import org.ovirt.engine.core.common.businessentities.VmPoolMap;
import org.ovirt.engine.core.common.errors.EngineMessage;

public class AddVmToPoolCommand<T extends AddVmToPoolParameters> extends VmPoolCommandBase<T> {

    public AddVmToPoolCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
        super.setVmId(parameters.getVmId());
    }


    @Override
    protected boolean validate() {
        if (getVm() == null) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_VM_NOT_FOUND);
        }

        if (getVm().isRunningOrPaused() || getVm().getStatus() == VMStatus.Unknown) {
            return failValidation(EngineMessage.VM_POOL_CANNOT_ADD_RUNNING_VM_TO_POOL);
        }

        if (getVm().getVmPoolId() != null) {
            return failValidation(EngineMessage.VM_POOL_CANNOT_ADD_VM_ATTACHED_TO_POOL);
        }

        if (getParameters().getVmPoolId() != null) {
            VmPool pool = getVmPoolDao().get(getParameters().getVmPoolId());
            if (pool != null && !pool.getClusterId().equals(getVm().getClusterId())) {
                return failValidation(EngineMessage.VM_POOL_CANNOT_ADD_VM_DIFFERENT_CLUSTER);
            }
        }

        return true;
    }

    @Override
    protected void executeCommand() {
        getVmPoolDao().addVmToPool(new VmPoolMap(getVmId(), getVmPoolId()));
        setSucceeded(true);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_ADD_VM_TO_POOL : AuditLogType.USER_ADD_VM_TO_POOL_FAILED;
    }

    @Override
    protected void endSuccessfully() {
        setSucceeded(true);
    }

    @Override
    protected void endWithFailure() {
        setSucceeded(true);
    }
}

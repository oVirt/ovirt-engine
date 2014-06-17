package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.AddVmToPoolParameters;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmPool;
import org.ovirt.engine.core.common.businessentities.VmPoolMap;
import org.ovirt.engine.core.common.errors.VdcBllMessages;

public class AddVmToPoolCommand<T extends AddVmToPoolParameters> extends VmPoolCommandBase<T> {
    public AddVmToPoolCommand(T parameters) {
        this(parameters, null);
    }

    public AddVmToPoolCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
        super.setVmId(parameters.getVmId());
    }


    @Override
    protected boolean canDoAction() {
        if (getVm() == null) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_VM_NOT_FOUND);
        }

        if (getVm().isRunningOrPaused() || getVm().getStatus() == VMStatus.Unknown) {
            return failCanDoAction(VdcBllMessages.VM_POOL_CANNOT_ADD_RUNNING_VM_TO_POOL);
        }

        if (getVm().getVmPoolId() != null) {
            return failCanDoAction(VdcBllMessages.VM_POOL_CANNOT_ADD_VM_ATTACHED_TO_POOL);
        }

        if (getParameters().getVmPoolId() != null) {
            VmPool pool = getVmPoolDAO().get(getParameters().getVmPoolId());
            if (pool != null && !pool.getVdsGroupId().equals(getVm().getVdsGroupId())) {
                return failCanDoAction(VdcBllMessages.VM_POOL_CANNOT_ADD_VM_DIFFERENT_CLUSTER);
            }
        }

        return true;
    }

    @Override
    protected void executeCommand() {
        getVmPoolDAO().addVmToPool(new VmPoolMap(getVmId(), getVmPoolId()));
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

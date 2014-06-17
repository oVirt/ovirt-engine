package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.RemoveVmFromPoolParameters;
import org.ovirt.engine.core.common.businessentities.VmPoolMap;
import org.ovirt.engine.core.common.errors.VdcBllMessages;

public class RemoveVmFromPoolCommand<T extends RemoveVmFromPoolParameters> extends VmPoolCommandBase<T> {
    public RemoveVmFromPoolCommand(T parameters) {
        super(parameters);
        setVmId(parameters.getVmId());
        VmPoolMap map = getVmPoolDAO().getVmPoolMapByVmGuid(parameters.getVmId());
        if (map != null) {
            setVmPoolId(map.getvm_pool_id());
        }
    }

    @Override
    protected boolean canDoAction() {
        if (getVm() == null) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_VM_NOT_FOUND);
        }

        if (getVmPoolDAO().getVmPoolMapByVmGuid(getVmId()) == null) {
            return failCanDoAction(VdcBllMessages.VM_POOL_CANNOT_DETACH_VM_NOT_ATTACHED_TO_POOL);
        }

        if (RemoveVmCommand.isVmRunning(getParameters().getVmId())) {
            return failCanDoAction(VdcBllMessages.VM_POOL_CANNOT_REMOVE_RUNNING_VM_FROM_POOL);
        }

        return true;
    }

    @Override
    protected void executeCommand() {
        if (getVmPoolId() != null) {
            getVmPoolDAO().removeVmFromVmPool(getVmId());
            setSucceeded(true);
        }
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_REMOVE_VM_FROM_POOL : AuditLogType.USER_REMOVE_VM_FROM_POOL_FAILED;
    }
}

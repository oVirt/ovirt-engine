package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.RemoveVmFromPoolParameters;
import org.ovirt.engine.core.common.businessentities.VmPoolMap;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class RemoveVmFromPoolCommand<T extends RemoveVmFromPoolParameters> extends VmPoolCommandBase<T> {
    public RemoveVmFromPoolCommand(T parameters) {
        super(parameters);
        super.setVmId(parameters.getVmId());
        VmPoolMap map = DbFacade.getInstance().getVmPoolDao().getVmPoolMapByVmGuid(parameters.getVmId());
        if (map != null) {
            setVmPoolId(map.getvm_pool_id());
        }
    }

    public static boolean canRemoveVmFromPool(Guid vmId, java.util.ArrayList<String> messages) {
        boolean returnValue = true;
        // Check if the vm is in a pool.
        if (DbFacade.getInstance().getVmPoolDao().getVmPoolMapByVmGuid(vmId) == null) {
            messages.add(VdcBllMessages.VM_POOL_CANNOT_DETACH_VM_NOT_ATTACHED_TO_POOL.toString());
            returnValue = false;
        }
        if (returnValue) {
            if (RemoveVmCommand.IsVmRunning(vmId)) {
                messages.add(VdcBllMessages.VM_POOL_CANNOT_REMOVE_RUNNING_VM_FROM_POOL.toString());
                returnValue = false;
            }
        }
        return returnValue;
    }

    @Override
    protected boolean canDoAction() {
        return canRemoveVmFromPool(getParameters().getVmId(), getReturnValue().getCanDoActionMessages());
    }

    @Override
    protected void executeCommand() {
        if (getVmPoolId() != null) {
            DbFacade.getInstance().getVmPoolDao().removeVmFromVmPool(getVmId());
            setSucceeded(true);
        }
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_REMOVE_VM_FROM_POOL : AuditLogType.USER_REMOVE_VM_FROM_POOL_FAILED;
    }
}

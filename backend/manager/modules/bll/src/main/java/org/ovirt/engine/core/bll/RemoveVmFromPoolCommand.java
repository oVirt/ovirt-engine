package org.ovirt.engine.core.bll;

import java.util.List;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.RemoveVmFromPoolParameters;
import org.ovirt.engine.core.common.businessentities.image_vm_pool_map;
import org.ovirt.engine.core.common.businessentities.vm_pool_map;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class RemoveVmFromPoolCommand<T extends RemoveVmFromPoolParameters> extends VmPoolCommandBase<T> {
    public RemoveVmFromPoolCommand(T parameters) {
        super(parameters);
        super.setVmId(parameters.getVmId());
        vm_pool_map map = DbFacade.getInstance().getVmPoolDAO().getVmPoolMapByVmGuid(parameters.getVmId());
        if (map != null) {
            setVmPoolId(map.getvm_pool_id());
        }
    }

    public static boolean CanRemoveVmFromPool(Guid vmId, java.util.ArrayList<String> messages) {
        boolean returnValue = false;
        // Check if the vm is in a pool.
        if (DbFacade.getInstance().getVmPoolDAO().getVmPoolMapByVmGuid(vmId) == null) {
            // Check if the vm is in a time leased pool.
            if (DbFacade.getInstance().getVmPoolDAO().getTimeLeasedVmPoolMapByIdForVmPool(vmId, Guid.Empty) == null) {
                messages.add(VdcBllMessages.VM_POOL_CANNOT_DETACH_VM_NOT_ATTACHED_TO_POOL.toString());
                returnValue = true;
            }
        } else {
            returnValue = RemoveVmCommand.IsVmRunning(vmId);
            if (returnValue) {
                messages.add(VdcBllMessages.VM_POOL_CANNOT_REMOVE_RUNNING_VM_FROM_POOL.toString());
            }
        }
        return !returnValue;
    }

    @Override
    protected boolean canDoAction() {
        return CanRemoveVmFromPool(getParameters().getVmId(), getReturnValue().getCanDoActionMessages());
    }

    @Override
    protected void executeCommand() {
        if (getVmPoolId() != null) {
            List<image_vm_pool_map> list = DbFacade.getInstance().getDiskImageDAO().getImageVmPoolMapByVmId(getVmId());
            for (image_vm_pool_map imageMap : list) {
                DbFacade.getInstance().getDiskImageDAO().removeImageVmPoolMap(imageMap.getimage_guid());
            }
            DbFacade.getInstance().getVmPoolDAO().removeVmFromVmPool(getVmId());
            setSucceeded(true);
        }
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_REMOVE_VM_FROM_POOL : AuditLogType.USER_REMOVE_VM_FROM_POOL_FAILED;
    }
}

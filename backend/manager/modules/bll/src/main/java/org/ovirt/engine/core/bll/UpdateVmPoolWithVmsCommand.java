package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.AddVmPoolWithVmsParameters;
import org.ovirt.engine.core.common.businessentities.vm_pools;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

@NonTransactiveCommandAttribute(forceCompensation = true)
public class UpdateVmPoolWithVmsCommand<T extends AddVmPoolWithVmsParameters> extends CommonVmPoolWithVmsCommand<T> {

    /**
     * Constructor for command creation when compensation is applied on startup
     *
     * @param commandId
     */
    protected UpdateVmPoolWithVmsCommand(Guid commandId) {
        super(commandId);
    }

    public UpdateVmPoolWithVmsCommand(T parameters) {
        super(parameters);
    }

    @Override
    protected Guid GetPoolId() {
        // List<vm_pool_map> list =
        // DbFacade.Instance.GetVmPoolsMapByVmPoolId(VmPool.vm_pool_id);
        // AddVmPoolWithVmsParametersData.VmStaticData =
        // DbFacade.Instance.GetVmStaticById(list[0].vm_guid);
        DbFacade.getInstance().getVmPoolDAO().update(getVmPool());
        return getVmPool().getvm_pool_id();
    }

    @Override
    protected boolean canDoAction() {
        boolean returnValue = super.canDoAction();
        vm_pools oldPool = DbFacade.getInstance().getVmPoolDAO().get(getVmPool().getvm_pool_id());
        if (returnValue && oldPool == null) {
            addCanDoActionMessage(VdcBllMessages.VM_POOL_CANNOT_UPDATE_POOL_NOT_FOUND);
            returnValue = false;
        } else if (returnValue && getParameters().getVmsCount() < 0) {
            addCanDoActionMessage(VdcBllMessages.VM_POOL_CANNOT_DECREASE_VMS_FROM_POOL);
            returnValue = false;
        }
        if (!returnValue) {
            addCanDoActionMessage(VdcBllMessages.VAR__ACTION__UPDATE);
        }
        return returnValue;
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getAddVmsSucceded() ? AuditLogType.USER_UPDATE_VM_POOL_WITH_VMS
                : AuditLogType.USER_UPDATE_VM_POOL_WITH_VMS_FAILED;
    }
}

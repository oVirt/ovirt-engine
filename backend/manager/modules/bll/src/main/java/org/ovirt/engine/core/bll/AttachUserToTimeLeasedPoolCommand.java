package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.AttachUserToTimeLeasedPoolParameters;
import org.ovirt.engine.core.common.businessentities.time_lease_vm_pool_map;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class AttachUserToTimeLeasedPoolCommand<T extends AttachUserToTimeLeasedPoolParameters> extends
        VmPoolUserCommandBase<T> {
    public AttachUserToTimeLeasedPoolCommand(T parameters) {
        super(parameters);
    }

    @Override
    protected boolean canDoAction() {
        boolean result = true;
        time_lease_vm_pool_map time_lease_vm_pool_map = DbFacade.getInstance().getVmPoolDAO().getTimeLeasedVmPoolMapByIdForVmPool(
                getAdUserId(), (Guid) getVmPoolId());
        if (time_lease_vm_pool_map != null) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_USER_ATTACHED_TO_POOL);
            result = false;
        }
        return result;

    }

    @Override
    protected void executeCommand() {
        initUser();
        DbFacade.getInstance().getVmPoolDAO().addTimeLeasedVmPoolMap(getParameters().getUserPoolMap());
        TimeLeasedVmPoolManager.getInstance().AddAction(getParameters().getUserPoolMap());
        setSucceeded(true);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_ATTACH_USER_TO_TIME_LEASED_POOL
                : AuditLogType.USER_ATTACH_USER_TO_TIME_LEASED_POOL_FAILED;
    }
}

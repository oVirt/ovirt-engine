package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.UpdateUserVmPoolParameters;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class UpdateUserToTimeLeasedPoolCommand<T extends UpdateUserVmPoolParameters> extends
        VmPoolSimpleUserCommandBase<T> {
    public UpdateUserToTimeLeasedPoolCommand(T parameters) {
        super(parameters);
        parameters.getUserPoolMap().oldMap = DbFacade.getInstance().getVmPoolDAO().getTimeLeasedVmPoolMapByIdForVmPool(
                parameters.getUserPoolMap().getid(), parameters.getUserPoolMap().getvm_pool_id());
    }

    @Override
    protected void executeCommand() {
        DbFacade.getInstance().getVmPoolDAO().updateTimeLeasedVmPoolMap(getParameters().getUserPoolMap());
        TimeLeasedVmPoolManager.getInstance().UpdateAction(getParameters().getUserPoolMap());
        setSucceeded(true);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_UPDATE_USER_TO_TIME_LEASED_POOL
                : AuditLogType.USER_UPDATE_USER_TO_TIME_LEASED_POOL_FAILED;
    }
}

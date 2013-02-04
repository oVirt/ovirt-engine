package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.VmPoolOperationParameters;
import org.ovirt.engine.core.common.businessentities.VmPool;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class AddVmPoolCommand<T extends VmPoolOperationParameters> extends VmPoolCommandBase<T> {

    /**
     * Constructor for command creation when compensation is applied on startup
     *
     * @param commandId
     */
    protected AddVmPoolCommand(Guid commandId) {
        super(commandId);
    }

    public AddVmPoolCommand(T parameters) {
        super(parameters);
        setVmPool(parameters.getVmPool());
    }

    @Override
    protected void executeCommand() {
        VmPool vmPool = getVmPool();

        DbFacade.getInstance().getVmPoolDao().save(vmPool);
        setActionReturnValue(vmPool.getvm_pool_id());
        setSucceeded(true);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_ADD_VM_POOL
                : AuditLogType.USER_ADD_VM_POOL_FAILED;
    }
}

package org.ovirt.engine.core.bll;

import java.util.List;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.VmPoolOperationParameters;
import org.ovirt.engine.core.common.businessentities.VmPool;
import org.ovirt.engine.core.common.validation.group.CreateEntity;
import org.ovirt.engine.core.compat.Guid;

public class AddVmPoolCommand<T extends VmPoolOperationParameters> extends VmPoolCommandBase<T> {

    /**
     * Constructor for command creation when compensation is applied on startup
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

        getVmPoolDAO().save(vmPool);
        setActionReturnValue(vmPool.getVmPoolId());
        setSucceeded(true);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_ADD_VM_POOL
                : AuditLogType.USER_ADD_VM_POOL_FAILED;
    }

    @Override
    protected List<Class<?>> getValidationGroups() {
        addValidationGroup(CreateEntity.class);
        return super.getValidationGroups();
    }


}

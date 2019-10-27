package org.ovirt.engine.core.bll.storage.domain;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.UnmanagedStorageDomainManagementParameter;
import org.ovirt.engine.core.compat.Guid;

public class AddUnmanagedStorageDomainCommand<T extends UnmanagedStorageDomainManagementParameter> extends AddStorageDomainCommand<T> {

    public AddUnmanagedStorageDomainCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }

    public AddUnmanagedStorageDomainCommand(Guid commandId) {
        super(commandId);
    }

    @Override
    protected boolean validate() {
        return true;
    }

    @Override
    protected boolean canAddDomain() {
        return true;
    }

    @Override
    protected void executeCommand() {
        updateStaticDataDefaults();
        addStorageDomainInDb();
        setSucceeded(true);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return AuditLogType.UNASSIGNED;
    }
}

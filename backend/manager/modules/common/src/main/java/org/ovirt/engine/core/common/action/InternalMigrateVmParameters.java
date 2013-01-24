package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.TransactionScopeOption;

public class InternalMigrateVmParameters extends VmOperationParameterBase {

    public InternalMigrateVmParameters() {
        setTransactionScopeOption(TransactionScopeOption.RequiresNew);
    }

    public InternalMigrateVmParameters(Guid vmId) {
        this();
        setVmId(vmId);
    }

    public InternalMigrateVmParameters(Guid vmId, VdcActionType parentCommand) {
        this(vmId);
        setParentCommand(parentCommand);
    }
}

package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.action.VmOperationParameterBase;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.TransactionScopeOption;

public class InternalMigrateVmParameters extends VmOperationParameterBase {

    public InternalMigrateVmParameters() {
        setTransactionScopeOption(TransactionScopeOption.RequiresNew);
    }

    public InternalMigrateVmParameters(Guid vmId) {
        super(vmId);
        setTransactionScopeOption(TransactionScopeOption.RequiresNew);
    }
}

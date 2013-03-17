package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.action.InternalMigrateVmParameters;
import org.ovirt.engine.core.common.action.MigrateVmParameters;
import org.ovirt.engine.core.common.businessentities.MigrationSupport;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.errors.VdcBllMessages;

@InternalCommandAttribute
public class InternalMigrateVmCommand<T extends InternalMigrateVmParameters> extends MigrateVmCommand<MigrateVmParameters> {

    public InternalMigrateVmCommand(T parameters) {
        super(new MigrateVmParameters(parameters));
    }

    @Override
    protected void executeCommand() {
        try {
            super.executeCommand();
            setActionReturnValue(true);
            setSucceeded(true);
        } catch (java.lang.Exception e) {
            setActionReturnValue(false);
        }
    }

    /**
     * Internal migrate command is initiated by server.
     * if the VM's migration support is not set to {@link MigrationSupport.MIGRATABLE},
     * the internal migration command should fail
     */
    @Override
    protected boolean canMigrateVm(VM vm) {
        if (vm.getMigrationSupport() == MigrationSupport.MIGRATABLE) {
            return super.canMigrateVm(vm);
        }
        return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_VM_IS_NON_MIGRTABLE);
    }
}

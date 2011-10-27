package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.action.MigrateVmParameters;
import org.ovirt.engine.core.common.businessentities.MigrationSupport;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.compat.Guid;

@InternalCommandAttribute
public class InternalMigrateVmCommand<T extends MigrateVmParameters> extends MigrateVmCommand<T> {

    public InternalMigrateVmCommand(T parameters) {
        super(parameters);
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

    @Override
    protected boolean canMigrateVm(Guid vmGuid, java.util.ArrayList<String> reasons) {
        boolean canMigrateVM = super.canMigrateVm(vmGuid, reasons);
        VM vm = getVm();
        // Internal migrate command is initiated by server, if migration support
        // is not set to "migratable" the internal migration
        // should fail

        canMigrateVM = vm.getMigrationSupport() == MigrationSupport.MIGRATABLE && canMigrateVM;
        return canMigrateVM;

    }
}

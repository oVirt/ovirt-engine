package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.bll.context.CommandContext;

import org.ovirt.engine.core.common.action.InternalMigrateVmParameters;
import org.ovirt.engine.core.common.action.MigrateVmParameters;
import org.ovirt.engine.core.common.businessentities.MigrationSupport;
import org.ovirt.engine.core.common.errors.VdcBllMessages;

@LockIdNameAttribute(isReleaseAtEndOfExecute = false)
@InternalCommandAttribute
@NonTransactiveCommandAttribute
public class InternalMigrateVmCommand<T extends InternalMigrateVmParameters> extends MigrateVmCommand<MigrateVmParameters> {

    public InternalMigrateVmCommand(T parameters, CommandContext cmdContext) {
        super(new MigrateVmParameters(parameters), cmdContext);
    }

    @Override
    protected void executeCommand() {
        try {
            super.executeCommand();
            setActionReturnValue(true);
            setSucceeded(true);
        } catch (Exception e) {
            setActionReturnValue(false);
        }
    }

    /**
     * Internal migrate command is initiated by server.
     * if the VM's migration support is not set to {@link MigrationSupport.MIGRATABLE},
     * the internal migration command should fail
     */
    @Override
    protected boolean canDoAction() {
        if (!super.canDoAction()) {
            return false;
        }

        if (getVm().getMigrationSupport() != MigrationSupport.MIGRATABLE) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_VM_IS_NON_MIGRTABLE);
        }

        return true;
    }
}

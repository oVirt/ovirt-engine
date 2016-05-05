package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.action.InternalMigrateVmParameters;
import org.ovirt.engine.core.common.action.MigrateVmParameters;
import org.ovirt.engine.core.common.businessentities.MigrationSupport;
import org.ovirt.engine.core.common.errors.EngineMessage;

@InternalCommandAttribute
@NonTransactiveCommandAttribute
public class InternalMigrateVmCommand<T extends InternalMigrateVmParameters> extends MigrateVmCommand<MigrateVmParameters> {

    public InternalMigrateVmCommand(T parameters, CommandContext cmdContext) {
        super(new MigrateVmParameters(parameters), cmdContext);
    }

    /**
     * Internal migrate command is initiated by server.
     * if the VM's migration support is not set to {@link MigrationSupport.MIGRATABLE},
     * the internal migration command should fail
     */
    @Override
    protected boolean validate() {
        if (!super.validate()) {
            return false;
        }

        if (getVm().getMigrationSupport() != MigrationSupport.MIGRATABLE) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_VM_IS_NON_MIGRTABLE);
        }

        return true;
    }
}

package org.ovirt.engine.core.common.action;

import java.util.ArrayList;

import org.ovirt.engine.core.compat.Guid;

/**
 * Base class for all migration commands parameter classes Includes a "force migration" flag that indicates that the
 * user requests to perform migration even if the VM is non migratable
 */
public class MigrateVmParameters extends VmOperationParameterBase {
    private static final long serialVersionUID = -7523728706659584319L;
    protected boolean forceMigrationForNonMigratableVM;
    ArrayList<Guid> initialHosts;

    public MigrateVmParameters() {
    }

    public MigrateVmParameters(boolean forceMigrationForNonMigratableVM, Guid vmId) {
        super(vmId);

        setForceMigrationForNonMigratableVM(forceMigrationForNonMigratableVM);
    }

    public MigrateVmParameters(InternalMigrateVmParameters internalMigrateVmParameters) {
        this(false, internalMigrateVmParameters.getVmId());

        setTransactionScopeOption(internalMigrateVmParameters.getTransactionScopeOption());
        setCorrelationId(internalMigrateVmParameters.getCorrelationId());
        setParentCommand(internalMigrateVmParameters.getParentCommand());
    }

    public boolean isForceMigrationForNonMigratableVM() {
        return forceMigrationForNonMigratableVM;
    }

    public void setForceMigrationForNonMigratableVM(boolean forceMigrationForNonMigratableVM) {
        this.forceMigrationForNonMigratableVM = forceMigrationForNonMigratableVM;
    }

    public ArrayList<Guid> getInitialHosts() {
        return initialHosts;
    }

    public void setInitialHosts(ArrayList<Guid> initialHosts) {
        this.initialHosts = initialHosts;
    }

}

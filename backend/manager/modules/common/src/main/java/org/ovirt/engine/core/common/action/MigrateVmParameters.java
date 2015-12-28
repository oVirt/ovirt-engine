package org.ovirt.engine.core.common.action;

import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

import org.ovirt.engine.core.compat.Guid;

/**
 * Base class for all migration commands parameter classes Includes a "force migration" flag that indicates that the
 * user requests to perform migration even if the VM is non migratable
 */
public class MigrateVmParameters extends VmOperationParameterBase {
    private static final long serialVersionUID = -7523728706659584319L;
    protected boolean forceMigrationForNonMigratableVm;
    ArrayList<Guid> initialHosts;
    // time that took the actual migration (from Engine point of view)
    protected Date startTime;
    // Total time for migration (including retries)
    protected Date totalMigrationTime;
    private Guid targetClusterId;

    public MigrateVmParameters() {
    }

    public MigrateVmParameters(boolean forceMigrationForNonMigratableVM, Guid vmId) {
        this(forceMigrationForNonMigratableVM, vmId, null);
    }

    public MigrateVmParameters(boolean forceMigrationForNonMigratableVM, Guid vmId, Guid targetClusterId) {
        super(vmId);

        this.targetClusterId = targetClusterId;
        setForceMigrationForNonMigratableVm(forceMigrationForNonMigratableVM);
    }

    public MigrateVmParameters(InternalMigrateVmParameters internalMigrateVmParameters) {
        this(false, internalMigrateVmParameters.getVmId());

        setTransactionScopeOption(internalMigrateVmParameters.getTransactionScopeOption());
        setCorrelationId(internalMigrateVmParameters.getCorrelationId());
        setParentCommand(internalMigrateVmParameters.getParentCommand());
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                getVmId(),
                forceMigrationForNonMigratableVm,
                targetClusterId
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof MigrateVmParameters)) {
            return false;
        }

        MigrateVmParameters other = (MigrateVmParameters) obj;
        return Objects.equals(getVmId(), other.getVmId())
                && forceMigrationForNonMigratableVm == other.forceMigrationForNonMigratableVm
                && Objects.equals(targetClusterId, other.targetClusterId);
    }

    public boolean isForceMigrationForNonMigratableVm() {
        return forceMigrationForNonMigratableVm;
    }

    public void setForceMigrationForNonMigratableVm(boolean forceMigrationForNonMigratableVm) {
        this.forceMigrationForNonMigratableVm = forceMigrationForNonMigratableVm;
    }

    public ArrayList<Guid> getInitialHosts() {
        return initialHosts;
    }

    public void setInitialHosts(ArrayList<Guid> initialHosts) {
        this.initialHosts = initialHosts;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
        if (this.totalMigrationTime == null) {
            this.totalMigrationTime = this.startTime;
        }
    }

    public Date getTotalMigrationTime() {
        return startTime;
    }

    public Guid getTargetClusterId() {
        return targetClusterId;
    }

    public void setTargetClusterId(Guid targetClusterId) {
        this.targetClusterId = targetClusterId;
    }
}

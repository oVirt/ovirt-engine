package org.ovirt.engine.core.common.action;

import java.util.ArrayList;
import java.util.Date;

import org.ovirt.engine.core.common.utils.ObjectUtils;
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
    private Guid targetVdsGroupId;

    public MigrateVmParameters() {
    }

    public MigrateVmParameters(boolean forceMigrationForNonMigratableVM, Guid vmId) {
        this(forceMigrationForNonMigratableVM, vmId, null);
    }

    public MigrateVmParameters(boolean forceMigrationForNonMigratableVM, Guid vmId, Guid targetVdsGroupId) {
        super(vmId);

        this.targetVdsGroupId = targetVdsGroupId;
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
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (forceMigrationForNonMigratableVm ? 1231 : 1237);
        result = prime * result + ((targetVdsGroupId == null) ? 0 : targetVdsGroupId.hashCode());
        return result;
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
        return forceMigrationForNonMigratableVm == other.forceMigrationForNonMigratableVm
                && ObjectUtils.objectsEqual(getVmId(), other.getVmId())
                && ObjectUtils.objectsEqual(targetVdsGroupId, other.targetVdsGroupId);
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

    public Guid getTargetVdsGroupId() {
        return targetVdsGroupId;
    }

    public void setTargetVdsGroupId(Guid targetVdsGroupId) {
        this.targetVdsGroupId = targetVdsGroupId;
    }
}

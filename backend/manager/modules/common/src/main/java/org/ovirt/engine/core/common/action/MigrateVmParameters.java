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

    private boolean forceMigrationForNonMigratableVm;
    private ArrayList<Guid> initialHosts;
    /** Start time of the actual migration (from Engine point of view) */
    private Date startTime;
    /** Start time of the whole migration process (including retries) */
    private Date totalMigrationTime;
    private Guid targetClusterId;
    private String reason;
    private boolean ignoreHardVmToVmAffinity;

    public MigrateVmParameters() {
    }

    public MigrateVmParameters(boolean forceMigrationForNonMigratableVm, Guid vmId) {
        this(forceMigrationForNonMigratableVm, vmId, null);
    }

    public MigrateVmParameters(boolean forceMigrationForNonMigratableVm, Guid vmId, Guid targetClusterId) {
        super(vmId);

        setForceMigrationForNonMigratableVm(forceMigrationForNonMigratableVm);
        setTargetClusterId(targetClusterId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                getVmId(),
                forceMigrationForNonMigratableVm,
                targetClusterId,
                reason
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
                && Objects.equals(targetClusterId, other.targetClusterId)
                && Objects.equals(reason, other.reason);
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
        if (this.startTime == null) {
            this.startTime = startTime;
        }
    }

    public void resetStartTime() {
        startTime = null;
    }

    public Date getTotalMigrationTime() {
        return totalMigrationTime;
    }

    public void setTotalMigrationTime(Date totalMigrationTime) {
        if (this.totalMigrationTime == null) {
            this.totalMigrationTime = totalMigrationTime;
        }
    }

    public Guid getTargetClusterId() {
        return targetClusterId;
    }

    public void setTargetClusterId(Guid targetClusterId) {
        this.targetClusterId = targetClusterId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public boolean isIgnoreHardVmToVmAffinity() {
        return ignoreHardVmToVmAffinity;
    }

    public void setIgnoreHardVmToVmAffinity(boolean ignoreHardVmToVmAffinity) {
        this.ignoreHardVmToVmAffinity = ignoreHardVmToVmAffinity;
    }
}

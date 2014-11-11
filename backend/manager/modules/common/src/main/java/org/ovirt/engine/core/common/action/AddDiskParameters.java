package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.compat.Guid;

public class AddDiskParameters extends VmDiskOperationParameterBase {
    private static final long serialVersionUID = -7832310521101821905L;
    private Guid vmSnapshotId;
    private Guid storageDomainId;
    private Boolean plugDiskToVm;
    private boolean shouldRemainIllegalOnFailedExecution;
    private boolean skipDomainCheck;

    public AddDiskParameters() {
        storageDomainId = Guid.Empty;
    }

    public AddDiskParameters(Guid vmId, Disk diskInfo) {
        super(vmId, diskInfo);
        storageDomainId = Guid.Empty;
    }

    public Guid getStorageDomainId() {
        return storageDomainId;
    }

    public void setStorageDomainId(Guid value) {
        storageDomainId = value;
    }

    public Guid getVmSnapshotId() {
        return vmSnapshotId;
    }

    public void setVmSnapshotId(Guid value) {
        vmSnapshotId = value;
    }

    public Boolean getPlugDiskToVm() {
        return plugDiskToVm;
    }

    public void setPlugDiskToVm(Boolean plugDiskToVm) {
        this.plugDiskToVm = plugDiskToVm;
    }

    public boolean isShouldRemainIllegalOnFailedExecution() {
        return shouldRemainIllegalOnFailedExecution;
    }

    public void setShouldRemainIllegalOnFailedExecution(boolean shouldRemainIllegalOnFailedExecution) {
        this.shouldRemainIllegalOnFailedExecution = shouldRemainIllegalOnFailedExecution;
    }

    public boolean isSkipDomainCheck() {
        return skipDomainCheck;
    }

    public void setSkipDomainCheck(boolean skipDomainCheck) {
        this.skipDomainCheck = skipDomainCheck;
    }
}

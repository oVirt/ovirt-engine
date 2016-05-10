package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskVmElement;
import org.ovirt.engine.core.compat.Guid;

public class AddDiskParameters extends VmDiskOperationParameterBase {
    private static final long serialVersionUID = -7832310521101821905L;
    private Guid vmSnapshotId;
    private Guid storageDomainId;
    private Boolean plugDiskToVm;
    private boolean shouldRemainIllegalOnFailedExecution;
    private boolean skipDomainCheck;
    private Guid vdsId;

    public AddDiskParameters() {
        storageDomainId = Guid.Empty;
    }

    // Used for floating disk creation
    public AddDiskParameters(Disk diskInfo) {
        this(new DiskVmElement(Guid.Empty, null), diskInfo);
    }

    public AddDiskParameters(DiskVmElement diskVmElement, Disk DiskInfo) {
        super(diskVmElement, DiskInfo);
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

    public Guid getVdsId() {
        return vdsId;
    }

    public void setVdsId(Guid vdsId) {
        this.vdsId = vdsId;
    }
}

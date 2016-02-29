package org.ovirt.engine.core.common.businessentities.storage;

import java.util.ArrayList;

import org.ovirt.engine.core.common.businessentities.IVdcQueryable;
import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.compat.Guid;

public class UnregisteredDisk implements IVdcQueryable {

    private DiskImage diskImage;
    private ArrayList<VmBase> vms;

    public UnregisteredDisk() {
        this(new DiskImage(), new ArrayList<VmBase>());
    }

    public UnregisteredDisk(DiskImage diskImage) {
        this(diskImage, new ArrayList<VmBase>());
    }

    public UnregisteredDisk(DiskImage diskImage, ArrayList<VmBase> vms) {
        this.diskImage = diskImage;
        this.vms = vms;
    }

    public Guid getId() {
        return getDiskImage().getId();
    }

    public void setId(Guid diskId) {
        getDiskImage().setId(diskId);
    }

    public String getDiskAlias() {
        return getDiskImage().getDiskAlias();
    }

    public void setDiskAlias(String diskAlias) {
        getDiskImage().setDiskAlias(diskAlias);
    }

    public String getDiskDescription() {
        return getDiskImage().getDiskDescription();
    }

    public void setDescription(String description) {
        getDiskImage().setDescription(description);
    }

    public Guid getStorageDomainId() {
        return getDiskImage().getStorageIds().get(0);
    }

    public void setStorageDomainId(Guid storageDomainId) {
        ArrayList<Guid> storageIds = new ArrayList<>();
        storageIds.add(storageDomainId);
        getDiskImage().setStorageIds(storageIds);
    }

    public DiskImage getDiskImage() {
        return diskImage;
    }

    public void setDiskImage(DiskImage diskImage) {
        this.diskImage = diskImage;
    }

    public ArrayList<VmBase> getVms() {
        return vms;
    }

    public void setVms(ArrayList<VmBase> vms) {
        this.vms = vms;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        UnregisteredDisk that = (UnregisteredDisk) o;

        if (!diskImage.equals(that.diskImage)) {
            return false;
        }
        return vms.equals(that.vms);
    }

    @Override
    public int hashCode() {
        int result = diskImage.hashCode();
        result = 31 * result + vms.hashCode();
        return result;
    }

    @Override
    public Object getQueryableId() {
        return getId();
    }
}

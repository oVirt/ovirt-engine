package org.ovirt.engine.core.common.businessentities.storage;

import java.util.ArrayList;
import java.util.Objects;

import org.ovirt.engine.core.common.businessentities.BusinessEntity;
import org.ovirt.engine.core.common.businessentities.Queryable;
import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.common.businessentities.comparators.BusinessEntityComparator;
import org.ovirt.engine.core.compat.Guid;

public class UnregisteredDisk implements Queryable, BusinessEntity<UnregisteredDiskId>, Comparable<UnregisteredDisk> {

    private static final long serialVersionUID = 4832875872161477672L;

    private DiskImage diskImage;
    private ArrayList<VmBase> vms;
    private UnregisteredDiskId id;

    public UnregisteredDisk() {
        this(new UnregisteredDiskId(), new DiskImage(), new ArrayList<VmBase>());
    }

    public UnregisteredDisk(DiskImage diskImage) {
        this(new UnregisteredDiskId(diskImage.getId(), diskImage.getStorageIds().get(0)),
                diskImage,
                new ArrayList<VmBase>());
    }

    public UnregisteredDisk(UnregisteredDiskId id, DiskImage diskImage, ArrayList<VmBase> vms) {
        this.diskImage = diskImage;
        this.vms = vms;
        this.id = id;
    }

    public UnregisteredDiskId getId() {
        return id;
    }

    public void setId(UnregisteredDiskId id) {
        this.id = id;
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

    public Guid getDiskId() {
        return getId().getDiskId();
    }

    public void setDiskId(Guid diskId) {
        getId().setDiskId(diskId);
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
        return Objects.equals(diskImage, that.diskImage) &&
                Objects.equals(vms, that.vms) &&
                Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(diskImage, vms, id);
    }

    @Override
    public Object getQueryableId() {
        return getId();
    }

    @Override
    public int compareTo(UnregisteredDisk o) {
        return BusinessEntityComparator.<UnregisteredDisk, UnregisteredDiskId>newInstance().compare(this, o);
    }
}

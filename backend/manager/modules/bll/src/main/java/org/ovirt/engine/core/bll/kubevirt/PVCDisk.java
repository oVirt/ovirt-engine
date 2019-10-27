package org.ovirt.engine.core.bll.kubevirt;

import java.util.Collections;

import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.storage.VolumeFormat;
import org.ovirt.engine.core.common.businessentities.storage.VolumeType;
import org.ovirt.engine.core.compat.Guid;

import com.google.common.base.Objects;

public class PVCDisk {

    private DiskImage diskImage;

    public PVCDisk() {
        diskImage = new PVCDiskImage();
        diskImage.setVolumeType(VolumeType.Sparse);
        diskImage.setVolumeFormat(VolumeFormat.RAW);
        diskImage.setSize(0);
        diskImage.setActive(true);
    }

    public PVCDisk(Guid sdId) {
        this();
        diskImage.setStorageIds(Collections.singletonList(sdId));
    }

    public PVCDisk(DiskImage diskImage) {
        this.diskImage = diskImage;
    }

    public void setNamespace(String namespace) {
        diskImage.setDiskDescription(namespace);
    }

    public String getNamespace() {
        return diskImage.getDiskDescription();
    }

    public void setName(String name) {
        diskImage.setDiskAlias(name);
    }

    public String getName() {
        return diskImage.getName();
    }

    public DiskImage toDisk() {
        return diskImage;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (other instanceof DiskImage) {
            PVCDisk otherPvc = new PVCDisk((DiskImage) other);
            return Objects.equal(otherPvc.diskImage.getStorageIds(), diskImage.getStorageIds()) &&
                    otherPvc.getName().equals(getName()) &&
                    otherPvc.getNamespace().equals(getNamespace());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return diskImage.hashCode();
    }

    private class PVCDiskImage extends DiskImage {
        @Override
        public DiskStorageType getDiskStorageType() {
            return DiskStorageType.KUBERNETES;
        }
    }
}

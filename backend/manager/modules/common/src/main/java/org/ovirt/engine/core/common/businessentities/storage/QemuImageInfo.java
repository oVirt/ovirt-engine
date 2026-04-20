package org.ovirt.engine.core.common.businessentities.storage;

import java.util.List;

import org.ovirt.engine.core.compat.Guid;

public class QemuImageInfo {

    private Guid imageId;
    private Guid imageGroupId;
    private Guid storageDomainId;
    private Guid storagePoolId;
    private QemuVolumeFormat qemuVolumeFormat;
    private long size;
    private long clusterSize;
    private String backingFile;
    private QcowCompat qcowCompat;
    private List<Qcow2BitmapInfo> qcow2bitmaps;

    public Guid getImageId() {
        return imageId;
    }

    public void setImageId(Guid imageId) {
        this.imageId = imageId;
    }

    public Guid getImageGroupId() {
        return imageGroupId;
    }

    public void setImageGroupId(Guid imageGroupId) {
        this.imageGroupId = imageGroupId;
    }

    public Guid getStorageDomainId() {
        return storageDomainId;
    }

    public void setStorageDomainId(Guid storageDomainId) {
        this.storageDomainId = storageDomainId;
    }

    public Guid getStoragePoolId() {
        return storagePoolId;
    }

    public void setStoragePoolId(Guid storagePoolId) {
        this.storagePoolId = storagePoolId;
    }

    public QemuVolumeFormat getQemuVolumeFormat() {
        return qemuVolumeFormat;
    }

    public void setQemuVolumeFormat(QemuVolumeFormat qemuVolumeFormat) {
        this.qemuVolumeFormat = qemuVolumeFormat;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public long getClusterSize() {
        return clusterSize;
    }

    public void setClusterSize(long clusterSize) {
        this.clusterSize = clusterSize;
    }

    public String getBackingFile() {
        return backingFile;
    }

    public void setBackingFile(String backingFile) {
        this.backingFile = backingFile;
    }

    public QcowCompat getQcowCompat() {
        return qcowCompat;
    }

    public void setQcowCompat(QcowCompat qcowCompat) {
        this.qcowCompat = qcowCompat;
    }

    public List<Qcow2BitmapInfo> getQcow2bitmaps() {
        return qcow2bitmaps;
    }

    public void setQcow2bitmaps(List<Qcow2BitmapInfo> qcow2bitmaps) {
        this.qcow2bitmaps = qcow2bitmaps;
    }
}

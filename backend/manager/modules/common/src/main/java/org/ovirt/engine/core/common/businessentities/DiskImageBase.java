package org.ovirt.engine.core.common.businessentities;

import javax.validation.Valid;

import org.ovirt.engine.core.compat.Guid;

public class DiskImageBase extends Disk {

    private static final long serialVersionUID = 4913899921353163969L;

    @Valid
    private Image image = new Image();

    /**
     * Transient field for GUI presentation purposes.
     */
    private String quotaName;

    public DiskImageBase() {
        getImage().setSize(0);
        getImage().setVolumeType(VolumeType.Sparse);
    }

    public Image getImage() {
        return image;
    }

    public void setImage(Image image) {
        this.image = image;
    }

    public VolumeType getvolume_type() {
        return getImage().getVolumeType();
    }

    public void setvolume_type(VolumeType value) {
        getImage().setVolumeType(value);
    }

    public VolumeFormat getvolume_format() {
        return getImage().getVolumeFormat();
    }

    public void setvolume_format(VolumeFormat value) {
        getImage().setVolumeFormat(value);
    }

    public Guid getQuotaId() {
        return getImage().getQuotaId();
    }

    public void setQuotaId(Guid quotaId) {
        getImage().setQuotaId(quotaId);
    }

    public long getsize() {
        return getImage().getSize();
    }

    public void setsize(long value) {
        getImage().setSize(value);
    }

    /**
     * disk size in GB
     */
    public long getSizeInGigabytes() {
        return getsize() / (1024 * 1024 * 1024);
    }

    public void setSizeInGigabytes(long value) {
        setsize(value * (1024 * 1024 * 1024));
    }

    public String getQuotaName() {
        return quotaName;
    }

    public void setQuotaName(String quotaName) {
        this.quotaName = quotaName;
    }

    @Override
    public DiskStorageType getDiskStorageType() {
        return DiskStorageType.IMAGE;
    }

    @Override
    public void setId(Guid id) {
        super.setId(id);
        getImage().setDiskId(id);
    }
}

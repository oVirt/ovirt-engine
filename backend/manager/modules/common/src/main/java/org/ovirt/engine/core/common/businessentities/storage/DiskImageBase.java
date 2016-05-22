package org.ovirt.engine.core.common.businessentities.storage;

import javax.validation.Valid;

import org.ovirt.engine.core.common.businessentities.QuotaEnforcementTypeEnum;
import org.ovirt.engine.core.compat.Guid;

public class DiskImageBase extends Disk {

    private static final long serialVersionUID = 4913899921353163969L;

    @Valid
    private Image image;

    /** Transient field for GUI presentation purposes. */
    private QuotaEnforcementTypeEnum quotaEnforcementType;

    public DiskImageBase() {
        image = new Image();
        getImage().setSize(0);
        getImage().setVolumeType(VolumeType.Sparse);
    }

    public Image getImage() {
        return image;
    }

    public void setImage(Image image) {
        this.image = image;
    }

    public VolumeType getVolumeType() {
        return getImage().getVolumeType();
    }

    public void setVolumeType(VolumeType volumeType) {
        getImage().setVolumeType(volumeType);
    }

    public VolumeFormat getVolumeFormat() {
        return getImage().getVolumeFormat();
    }

    public void setVolumeFormat(VolumeFormat volumeFormat) {
        getImage().setVolumeFormat(volumeFormat);
    }

    @Override
    public long getSize() {
        return getImage().getSize();
    }

    public void setSize(long size) {
        getImage().setSize(size);
    }

    /**
     * disk size in GB
     */
    public long getSizeInGigabytes() {
        return getSize() / (1024 * 1024 * 1024);
    }

    public void setSizeInGigabytes(long value) {
        setSize(value * (1024 * 1024 * 1024));
    }

    public QuotaEnforcementTypeEnum getQuotaEnforcementType() {
        return quotaEnforcementType;
    }

    public void setQuotaEnforcementType(QuotaEnforcementTypeEnum quotaEnforcementType) {
        this.quotaEnforcementType = quotaEnforcementType;
    }

    @Override
    public boolean isAllowSnapshot() {
        return !isShareable();
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

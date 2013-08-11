package org.ovirt.engine.core.common.businessentities;

import javax.validation.Valid;

import org.ovirt.engine.core.compat.Guid;

public class DiskImageBase extends Disk {

    private static final long serialVersionUID = 4913899921353163969L;

    @Valid
    private Image image;

    /** Transient field for GUI presentation purposes. */
    private String quotaName;

    /** Transient field for GUI presentation purposes. */
    private boolean isQuotaDefault;

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

    public void setvolumeFormat(VolumeFormat volumeFormat) {
        getImage().setVolumeFormat(volumeFormat);
    }

    public Guid getQuotaId() {
        return getImage().getQuotaId();
    }

    public void setQuotaId(Guid quotaId) {
        getImage().setQuotaId(quotaId);
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

    public String getQuotaName() {
        return quotaName;
    }

    public void setQuotaName(String quotaName) {
        this.quotaName = quotaName;
    }

    public boolean isQuotaDefault() {
        return isQuotaDefault;
    }

    public void setIsQuotaDefault(boolean isQuotaDefault) {
        this.isQuotaDefault = isQuotaDefault;
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

package org.ovirt.engine.core.common.businessentities;

public interface QuotaStorageProperties {

    /**
     * @return the storageLimitGigaByte
     */
    public Long getStorageSizeGB();

    /**
     * @param storageLimitGigaByte
     *            the storageLimitGigaByte to set
     */
    public void setStorageSizeGB(Long storageLimitGigaByte);

    /**
     * @return the storageLimitGigaByteUsage
     */
    public Double getStorageSizeGBUsage();

    /**
     * @param storageLimitGigaByteUsage
     *            the storageLimitGigaByteUsage to set
     */
    public void setStorageSizeGBUsage(Double storageLimitGigaByteUsage);
}

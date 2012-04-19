package org.ovirt.engine.core.common.businessentities;

import javax.validation.constraints.NotNull;

import org.ovirt.engine.core.common.validation.group.CreateEntity;
import org.ovirt.engine.core.common.validation.group.UpdateEntity;
import org.ovirt.engine.core.compat.Guid;

public class DiskImageBase extends Disk {

    private static final long serialVersionUID = 4913899921353163969L;

    @NotNull(message = "VALIDATION.VOLUME_TYPE.NOT_NULL", groups = { CreateEntity.class, UpdateEntity.class })
    private VolumeType volumeType = VolumeType.Sparse;
    private boolean boot;
    private long size = 0L;

    @NotNull(message = "VALIDATION.VOLUME_FORMAT.NOT_NULL", groups = { CreateEntity.class, UpdateEntity.class })
    private VolumeFormat volumeFormat;

    private Boolean plugged;

    /**
     * The quota id the image consumes from.
     */
    private Guid quotaId;

    /**
     * Transient field for GUI presentation purposes.
     */
    private String quotaName;

    public DiskImageBase() {
        size = 0;
        volumeType = VolumeType.Sparse;
    }

    public VolumeType getvolume_type() {
        return volumeType;
    }

    public void setvolume_type(VolumeType value) {
        volumeType = value;
    }

    public VolumeFormat getvolume_format() {
        return volumeFormat;
    }

    public void setvolume_format(VolumeFormat value) {
        volumeFormat = value;
    }

    public Guid getQuotaId() {
        return this.quotaId;
    }

    public void setQuotaId(Guid quotaId) {
        this.quotaId = quotaId;
    }

    public long getsize() {
        return this.size;
    }

    public void setsize(long value) {
        this.size = value;
    }

    public String getinternal_drive_mapping() {
        return Integer.toString(getInternalDriveMapping());
    }

    public void setinternal_drive_mapping(String value) {
        if (value != null) {
            setInternalDriveMapping(Integer.parseInt(value));
        }
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

    public boolean getboot() {
        return boot;
    }

    public void setboot(boolean value) {
        boot = value;
    }

    public Boolean getPlugged() {
        return plugged;
    }

    public void setPlugged(Boolean plugged) {
        this.plugged = plugged;
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
}

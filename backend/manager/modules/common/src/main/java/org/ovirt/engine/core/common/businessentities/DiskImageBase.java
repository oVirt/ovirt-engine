package org.ovirt.engine.core.common.businessentities;

import javax.validation.constraints.NotNull;

import org.ovirt.engine.core.common.validation.group.CreateEntity;
import org.ovirt.engine.core.common.validation.group.UpdateEntity;
import org.ovirt.engine.core.compat.Guid;

public class DiskImageBase extends Disk {

    private static final long serialVersionUID = 4913899921353163969L;

    @NotNull(message = "VALIDATION.VOLUME_TYPE.NOT_NULL", groups = { CreateEntity.class, UpdateEntity.class })
    private VolumeType volumeType = VolumeType.Sparse;
    private long size = 0L;

    @NotNull(message = "VALIDATION.VOLUME_FORMAT.NOT_NULL", groups = { CreateEntity.class, UpdateEntity.class })
    private VolumeFormat volumeFormat;

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
}

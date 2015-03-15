package org.ovirt.engine.core.common.businessentities.storage;

import java.util.Date;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.ovirt.engine.core.common.businessentities.BusinessEntitiesDefinitions;
import org.ovirt.engine.core.common.businessentities.BusinessEntity;
import org.ovirt.engine.core.common.businessentities.IVdcQueryable;
import org.ovirt.engine.core.common.utils.ObjectUtils;
import org.ovirt.engine.core.common.validation.annotation.ValidDescription;
import org.ovirt.engine.core.common.validation.annotation.ValidI18NName;
import org.ovirt.engine.core.common.validation.group.CreateEntity;
import org.ovirt.engine.core.common.validation.group.UpdateEntity;
import org.ovirt.engine.core.compat.Guid;

/**
 * The disk represents a drive in the VM/Template.<br>
 * <br>
 * The disk data consists of this entity which holds the immutable fields of the drive, and the DiskImage entity which
 * represents the drive's actual data, and contains the mutable fields.<br>
 * Each drive can have several of these "images" associated to it, which represent the drive's snapshots - a backup of
 * the drive's data at a certain point in time. An image of a snapshot is immutable, and there is usually (in case of a
 * VM) one or more mutable images which the VM can run with.<br>
 * <br>
 * Due to this, the {@link BaseDisk} entity always points to the active mutable image that the VM will run with (or the
 * image the Template represents).<br>
 * The active image can also be <code>null</code>, in case that it's missing but should be there.
 */
public class BaseDisk extends IVdcQueryable implements BusinessEntity<Guid> {

    /**
     * Needed for java serialization/deserialization mechanism.
     */
    private static final long serialVersionUID = 5883196978129104663L;

    /**
     * The disk ID uniquely identifies a disk in the system.
     */
    private Guid id;

    /**
     * The alias name of the disk.
     */
    @Size(min = 0, max = BusinessEntitiesDefinitions.GENERAL_NAME_SIZE, groups = { CreateEntity.class })
    @ValidI18NName(message = "VALIDATION.DISK.ALIAS.INVALID", groups = { CreateEntity.class, UpdateEntity.class })
    private String diskAlias = "";

    /**
     * The description of the disk.
     */
    @ValidDescription(message = "VALIDATION.DISK.DESCRIPTION.INVALID", groups = { CreateEntity.class
            , UpdateEntity.class })
    private String diskDescription;

    /**
     * A boolean indiaction whether the disk is shareable.
     */
    private boolean shareable;

    /**
     * The disk interface (IDE/SCSI/etc).
     */
    private DiskInterface diskInterface;

    /**
     * Should the disk be wiped after it's deleted.
     */
    private Boolean wipeAfterDelete;

    /**
     * Should disk errors be propagated to the guest?
     */
    private PropagateErrors propagateErrors;

    private boolean boot;

    private ScsiGenericIO sgio;

    private DiskAlignment alignment;

    private Date lastAlignmentScan;

    public BaseDisk() {
        propagateErrors = PropagateErrors.Off;
        alignment = DiskAlignment.Unknown;
    }

    @Override
    public Object getQueryableId() {
        return getId();
    }

    @Override
    public Guid getId() {
        return id;
    }

    @Override
    public void setId(Guid id) {
        this.id = id;
    }

    @NotNull(message = "VALIDATION.DISK_INTERFACE.NOT_NULL", groups = { CreateEntity.class, UpdateEntity.class })
    public DiskInterface getDiskInterface() {
        return diskInterface;
    }

    public void setDiskInterface(DiskInterface diskInterface) {
        this.diskInterface = diskInterface;
    }

    public boolean isWipeAfterDelete() {
        if (isWipeAfterDeleteSet()) {
            return wipeAfterDelete;
        }
        return false;
    }

    public boolean isWipeAfterDeleteSet() {
        return wipeAfterDelete != null;
    }

    public void setWipeAfterDelete(boolean wipeAfterDelete) {
        this.wipeAfterDelete = wipeAfterDelete;
    }

    public PropagateErrors getPropagateErrors() {
        return propagateErrors;
    }

    public void setPropagateErrors(PropagateErrors propagateErrors) {
        this.propagateErrors = propagateErrors;
    }

    public String getDiskDescription() {
        return diskDescription;
    }

    public void setDiskDescription(String diskDescription) {
        this.diskDescription = diskDescription;
    }

    public String getDiskAlias() {
        return diskAlias;
    }

    public void setDiskAlias(String diskAlias) {
        this.diskAlias = diskAlias == null ? "" : diskAlias;
    }

    public boolean isShareable() {
        return shareable;
    }

    public void setShareable(boolean shareable) {
        this.shareable = shareable;
    }

    public boolean isBoot() {
        return boot;
    }

    public void setBoot(boolean value) {
        boot = value;
    }

    public ScsiGenericIO getSgio() {
        return sgio;
    }

    public void setSgio(ScsiGenericIO sgio) {
        this.sgio = sgio;
    }

    public boolean isScsiPassthrough() {
        return getSgio() != null;
    }

    public DiskAlignment getAlignment() {
        return alignment;
    }

    public void setAlignment(DiskAlignment value) {
        alignment = value;
    }

    public Date getLastAlignmentScan() {
        return (lastAlignmentScan != null) ? (Date) lastAlignmentScan.clone() : null;
    }

    public void setLastAlignmentScan(Date value) {
        lastAlignmentScan = (value != null) ? (Date) value.clone() : null;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((diskAlias == null) ? 0 : diskAlias.hashCode());
        result = prime * result + ((diskDescription == null) ? 0 : diskDescription.hashCode());
        result = prime * result + ((diskInterface == null) ? 0 : diskInterface.hashCode());
        result = prime * result + ((propagateErrors == null) ? 0 : propagateErrors.hashCode());
        result = prime * result + (shareable ? 1231 : 1237);
        result = prime * result + (isWipeAfterDelete() ? 1231 : 1237);
        result = prime * result + (boot ? 1231 : 1237);
        result = prime * result + ((sgio == null) ? 0 : sgio.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        BaseDisk other = (BaseDisk) obj;
        return (ObjectUtils.objectsEqual(id, other.id)
                && ObjectUtils.objectsEqual(diskAlias, other.diskAlias)
                && ObjectUtils.objectsEqual(diskDescription, other.diskDescription)
                && diskInterface == other.diskInterface
                && propagateErrors == other.propagateErrors
                && shareable == other.shareable
                && isWipeAfterDelete() == other.isWipeAfterDelete()
                && boot == other.boot)
                && sgio == other.sgio;
    }
}

package org.ovirt.engine.core.common.businessentities.storage;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.ovirt.engine.core.common.businessentities.BusinessEntitiesDefinitions;
import org.ovirt.engine.core.common.businessentities.BusinessEntity;
import org.ovirt.engine.core.common.businessentities.Nameable;
import org.ovirt.engine.core.common.businessentities.ProgressEntity;
import org.ovirt.engine.core.common.businessentities.Queryable;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.validation.annotation.ValidDescription;
import org.ovirt.engine.core.common.validation.annotation.ValidI18NName;
import org.ovirt.engine.core.common.validation.group.CreateEntity;
import org.ovirt.engine.core.common.validation.group.UpdateEntity;
import org.ovirt.engine.core.compat.Guid;

import com.fasterxml.jackson.annotation.JsonIgnore;

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
public class BaseDisk implements Queryable, BusinessEntity<Guid>, ProgressEntity, Nameable {

    /**
     * Needed for java serialization/deserialization mechanism.
     */
    private static final long serialVersionUID = 982554837117443062L;

    /**
     * The disk ID uniquely identifies a disk in the system.
     */
    private Guid id;

    /**
     * The alias name of the disk.
     */
    @Size(min = 0, max = BusinessEntitiesDefinitions.GENERAL_NAME_SIZE,
            groups = { CreateEntity.class, UpdateEntity.class })
    @ValidI18NName(message = "VALIDATION_DISK_ALIAS_INVALID", groups = { CreateEntity.class, UpdateEntity.class })
    private String diskAlias = "";

    /**
     * The description of the disk.
     */
    @Size(min = 0, max = BusinessEntitiesDefinitions.DISK_DESCRIPTION_MAX_SIZE,
            groups = { CreateEntity.class, UpdateEntity.class })
    @ValidDescription(message = "VALIDATION_DISK_DESCRIPTION_INVALID", groups = { CreateEntity.class
            , UpdateEntity.class })
    @NotNull
    private String diskDescription;

    /**
     * A boolean indication whether the disk is shareable.
     */
    private boolean shareable;

    private Map<Guid, DiskVmElement> diskVmElementsMap = Collections.emptyMap();

    private Integer progress;

    private DiskContentType contentType;

    /**
     * @return an unmodifiable collection, we don't want anyone messing with the values without going through the setter
     */
    @JsonIgnore
    public Collection<DiskVmElement> getDiskVmElements() {
        return Collections.unmodifiableCollection(diskVmElementsMap.values());
    }

    public void setDiskVmElements(Collection<DiskVmElement> diskVmElements) {
        diskVmElementsMap = diskVmElements.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(d -> d.getId().getVmId(), Function.identity()));
    }

    @JsonIgnore
    public DiskVmElement getDiskVmElementForVm(Guid vmId) {
        return diskVmElementsMap.get(vmId);
    }

    /**
     * Should the disk be wiped after it's deleted.
     */
    private Boolean wipeAfterDelete;

    /**
     * Should disk errors be propagated to the guest?
     */
    private PropagateErrors propagateErrors;

    private ScsiGenericIO sgio;

    @Deprecated
    private String cinderVolumeType;

    private DiskBackup backup;

    private DiskBackupMode backupMode;

    public BaseDisk() {
        propagateErrors = PropagateErrors.Off;
        contentType = DiskContentType.DATA;
        backup = DiskBackup.None;
        diskDescription = "";
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
        if ((Boolean)(Config.getValue(ConfigValues.PropagateDiskErrors))) {
            return PropagateErrors.On;
        }
        return propagateErrors;
    }

    public void setPropagateErrors(PropagateErrors propagateErrors) {
        this.propagateErrors = propagateErrors;
    }

    public String getDiskDescription() {
        return diskDescription;
    }

    public void setDiskDescription(String diskDescription) {
        this.diskDescription =  diskDescription == null ? "" : diskDescription;
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

    public ScsiGenericIO getSgio() {
        return sgio;
    }

    public void setSgio(ScsiGenericIO sgio) {
        this.sgio = sgio;
    }

    public boolean isScsiPassthrough() {
        return getSgio() != null;
    }

    public String getCinderVolumeType() {
        return cinderVolumeType;
    }

    public void setCinderVolumeType(String cinderVolumeType) {
        this.cinderVolumeType = cinderVolumeType;
    }

    @Override
    public Integer getProgress() {
        return progress;
    }

    @Override
    public void setProgress(Integer progress) {
        this.progress = progress;
    }

    /**
     * @return The type of underlying storage implementation.
     */
    public DiskStorageType getDiskStorageType() {
        // should be implemented in the sub-classes
        return null;
    }

    @Override
    public boolean isManaged() {
        return getDiskStorageType() != DiskStorageType.KUBERNETES;
    }

    public DiskContentType getContentType() {
        return contentType;
    }

    public void setContentType(DiskContentType contentType) {
        this.contentType = contentType;
    }

    public DiskBackup getBackup() {
        return backup;
    }

    public void setBackup(DiskBackup backup) {
        this.backup = backup;
    }

    public DiskBackupMode getBackupMode() {
        return backupMode;
    }

    public void setBackupMode(DiskBackupMode backupMode) {
        this.backupMode = backupMode;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                id,
                diskAlias,
                diskDescription,
                propagateErrors,
                shareable,
                wipeAfterDelete,
                sgio,
                cinderVolumeType,
                contentType,
                backup,
                backupMode
        );
    }

    @Override
    public String getName() {
        return getDiskAlias();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof BaseDisk)) {
            return false;
        }
        BaseDisk other = (BaseDisk) obj;
        return Objects.equals(id, other.id)
                && Objects.equals(diskAlias, other.diskAlias)
                && Objects.equals(diskDescription, other.diskDescription)
                && propagateErrors == other.propagateErrors
                && shareable == other.shareable
                && isWipeAfterDelete() == other.isWipeAfterDelete()
                && sgio == other.sgio
                && Objects.equals(cinderVolumeType, other.cinderVolumeType)
                && contentType == other.contentType
                && backup == other.backup
                && backupMode == other.backupMode;
    }
}

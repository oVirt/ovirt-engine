package org.ovirt.engine.core.common.businessentities;

import java.util.Objects;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.common.validation.annotation.ValidDescription;
import org.ovirt.engine.core.common.validation.annotation.ValidName;
import org.ovirt.engine.core.common.validation.group.CreateEntity;
import org.ovirt.engine.core.common.validation.group.UpdateEntity;
import org.ovirt.engine.core.compat.Guid;

public class StorageDomainStatic implements BusinessEntity<Guid>, Nameable {
    private static final long serialVersionUID = 8635263021145935458L;

    private Guid id;

    @Size(min = 1, max = BusinessEntitiesDefinitions.STORAGE_SIZE)
    private String storage;

    // TODO storage name needs to be made unique
    @ValidName(message = "VALIDATION_STORAGE_DOMAIN_NAME_INVALID", groups = { CreateEntity.class, UpdateEntity.class })
    @Size(min = 1, max = BusinessEntitiesDefinitions.STORAGE_NAME_SIZE)
    private String name;

    @ValidDescription(message = "VALIDATION_STORAGE_DOMAIN_DESCRIPTION_INVALID", groups = { CreateEntity.class,
            UpdateEntity.class })
    @Size(min = 0, max = BusinessEntitiesDefinitions.GENERAL_MAX_SIZE,
            message = "VALIDATION_STORAGE_DOMAIN_DESCRIPTION_MAX",
            groups = { CreateEntity.class, UpdateEntity.class })
    @NotNull
    private String description;

    @NotNull
    private String comment;

    private StorageDomainType storageType;

    private StorageType storagePoolType;

    private StorageServerConnections connection;

    private StorageFormatType storageFormat;

    private StorageBlockSize blockSize;

    private boolean autoRecoverable;

    private SANState sanState;

    /*
    Represents the first device of the domain metadata LV for block domains.
     */
    private String firstMetadataDevice;

    /*
    The device of the domain used for the vg metadata for block domains.
     */
    private String vgMetadataDevice;

    private transient long lastTimeUsedAsMaster;

    private Boolean wipeAfterDelete;

    private Boolean discardAfterDelete;

    private boolean backup;

    @Min(value = 0, message = "VALIDATION_STORAGE_DOMAIN_WARNING_LOW_SPACE_INDICATOR_RANGE")
    @Max(value = 100, message = "VALIDATION_STORAGE_DOMAIN_WARNING_LOW_SPACE_INDICATOR_RANGE")
    private Integer warningLowSpaceIndicator;

    @Min(value = 0, message = "VALIDATION_STORAGE_DOMAIN_CRITICAL_SPACE_ACTION_BLOCKER_RANGE")
    @Max(value = Integer.MAX_VALUE, message = "VALIDATION_STORAGE_DOMAIN_CRITICAL_SPACE_ACTION_BLOCKER_RANGE")
    private Integer criticalSpaceActionBlocker;

    @Min(value = 0, message = "VALIDATION_STORAGE_DOMAIN_WARNING_LOW_CONFIRMED_SPACE_INDICATOR_RANGE")
    @Max(value = 100, message = "VALIDATION_STORAGE_DOMAIN_WARNING_LOW_CONFIRMED_SPACE_INDICATOR_RANGE")
    private Integer warningLowConfirmedSpaceIndicator;

    public StorageDomainStatic() {
        id = Guid.Empty;
        storageType = StorageDomainType.Master;
        storagePoolType = StorageType.UNKNOWN;
        autoRecoverable = true;
        name = "";
        description = "";
        comment = "";
        blockSize = StorageBlockSize.BLOCK_512;
    }

    @Override
    public Guid getId() {
        return this.id;
    }

    @Override
    public void setId(Guid id) {
        this.id = id;
    }

    public String getStorage() {
        return storage;
    }

    public void setStorage(String storage) {
        this.storage = storage;
    }

    public StorageDomainType getStorageDomainType() {
        return storageType;
    }

    public void setStorageDomainType(StorageDomainType storageType) {
        this.storageType = storageType;
    }

    public StorageType getStorageType() {
        return storagePoolType;
    }

    public void setStorageType(StorageType storagePoolType) {
        this.storagePoolType = storagePoolType;
    }

    public String getStorageName() {
        return name;
    }

    public void setStorageName(String name) {
        this.name = name;
    }

    public StorageServerConnections getConnection() {
        return connection;
    }

    public void setConnection(StorageServerConnections connection) {
        this.connection = connection;
    }

    public StorageFormatType getStorageFormat() {
        return storageFormat;
    }

    public void setStorageFormat(StorageFormatType storageFormat) {
        this.storageFormat = storageFormat;
    }

    public StorageBlockSize getBlockSize() {
        return blockSize;
    }

    public void setBlockSize(StorageBlockSize blockSize) {
        this.blockSize = blockSize;
    }

    public boolean isAutoRecoverable() {
        return autoRecoverable;
    }

    public void setAutoRecoverable(boolean autoRecoverable) {
        this.autoRecoverable = autoRecoverable;
    }

    public long getLastTimeUsedAsMaster() {
        return lastTimeUsedAsMaster;
    }

    public void setLastTimeUsedAsMaster(long lastTimeUsedAsMaster) {
        this.lastTimeUsedAsMaster = lastTimeUsedAsMaster;
    }

    public Boolean getWipeAfterDelete() {
        return wipeAfterDelete;
    }

    public void setWipeAfterDelete(Boolean wipeAfterDelete) {
        this.wipeAfterDelete = wipeAfterDelete;
    }

    public Boolean getDiscardAfterDelete() {
        return discardAfterDelete;
    }

    public void setDiscardAfterDelete(Boolean discardAfterDelete) {
        this.discardAfterDelete = discardAfterDelete;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description == null ? "" : description;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String value) {
        comment = value == null ? "" : value;
    }

    public SANState getSanState() {
        return sanState;
    }

    public void setSanState(SANState sanState) {
        this.sanState = sanState;
    }

    public Integer getWarningLowSpaceIndicator() {
        return warningLowSpaceIndicator;
    }

    public void setWarningLowSpaceIndicator(Integer warningLowSpaceIndicator) {
        this.warningLowSpaceIndicator = warningLowSpaceIndicator;
    }

    public Integer getWarningLowConfirmedSpaceIndicator() {
        return warningLowConfirmedSpaceIndicator;
    }

    public void setWarningLowConfirmedSpaceIndicator(Integer warningLowConfirmedSpaceIndicator) {
        this.warningLowConfirmedSpaceIndicator = warningLowConfirmedSpaceIndicator;
    }

    public Integer getCriticalSpaceActionBlocker() {
        return criticalSpaceActionBlocker;
    }

    public void setCriticalSpaceActionBlocker(Integer criticalSpaceActionBlocker) {
        this.criticalSpaceActionBlocker = criticalSpaceActionBlocker;
    }

    public String getFirstMetadataDevice() {
        return firstMetadataDevice;
    }

    public void setFirstMetadataDevice(String firstMetadataDevice) {
        this.firstMetadataDevice = firstMetadataDevice;
    }


    public String getVgMetadataDevice() {
        return vgMetadataDevice;
    }

    public void setVgMetadataDevice(String vgMetadataDevice) {
        this.vgMetadataDevice = vgMetadataDevice;
    }

    public boolean isBackup() {
        return backup;
    }

    public void setBackup(boolean backup) {
        this.backup = backup;
    }

    @Override
    public String getName() {
        return getStorageName();
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                id,
                autoRecoverable,
                connection,
                name,
                storage,
                storageFormat,
                blockSize,
                storagePoolType,
                storageType,
                description,
                sanState,
                wipeAfterDelete,
                discardAfterDelete,
                firstMetadataDevice,
                vgMetadataDevice,
                warningLowSpaceIndicator,
                criticalSpaceActionBlocker,
                warningLowConfirmedSpaceIndicator,
                backup
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof StorageDomainStatic)) {
            return false;
        }
        StorageDomainStatic other = (StorageDomainStatic) obj;
        return Objects.equals(id, other.id)
                && autoRecoverable == other.autoRecoverable
                && Objects.equals(connection, other.connection)
                && Objects.equals(name, other.name)
                && Objects.equals(storage, other.storage)
                && storageFormat == other.storageFormat
                && blockSize == other.blockSize
                && storagePoolType == other.storagePoolType
                && storageType == other.storageType
                && sanState == other.sanState
                && Objects.equals(wipeAfterDelete, other.wipeAfterDelete)
                && Objects.equals(discardAfterDelete, other.discardAfterDelete)
                && Objects.equals(firstMetadataDevice, other.firstMetadataDevice)
                && Objects.equals(vgMetadataDevice, other.vgMetadataDevice)
                && Objects.equals(description, other.description)
                && Objects.equals(warningLowSpaceIndicator, other.warningLowSpaceIndicator)
                && Objects.equals(criticalSpaceActionBlocker, other.criticalSpaceActionBlocker)
                && Objects.equals(warningLowConfirmedSpaceIndicator, other.warningLowConfirmedSpaceIndicator)
                && Objects.equals(backup, other.backup);
    }

    @Override
    public String toString() {
        return ToStringBuilder.forInstance(this)
                .append("name", getName())
                .append("id", getId())
                .build();
    }
}

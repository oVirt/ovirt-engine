package org.ovirt.engine.core.common.businessentities;

import java.util.Objects;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
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
    @Size(min = 1, max = BusinessEntitiesDefinitions.GENERAL_MAX_SIZE,
            message = "VALIDATION_STORAGE_DOMAIN_DESCRIPTION_MAX",
            groups = { CreateEntity.class, UpdateEntity.class })
    private String description;

    private String comment;

    private StorageDomainType storageType;

    private StorageType storagePoolType;

    private StorageServerConnections connection;

    private StorageFormatType storageFormat;

    private boolean autoRecoverable;

    private SANState sanState;

    private transient long lastTimeUsedAsMaster;

    private Boolean wipeAfterDelete;

    @Min(value = 0, message = "VALIDATION_STORAGE_DOMAIN_WARNING_LOW_SPACE_INDICATOR_RANGE")
    @Max(value = 100, message = "VALIDATION_STORAGE_DOMAIN_WARNING_LOW_SPACE_INDICATOR_RANGE")
    private Integer warningLowSpaceIndicator;

    @Min(value = 0, message = "VALIDATION_STORAGE_DOMAIN_CRITICAL_SPACE_ACTION_BLOCKER_RANGE")
    @Max(value = Integer.MAX_VALUE, message = "VALIDATION_STORAGE_DOMAIN_CRITICAL_SPACE_ACTION_BLOCKER_RANGE")
    private Integer criticalSpaceActionBlocker;

    public StorageDomainStatic() {
        id = Guid.Empty;
        storageType = StorageDomainType.Master;
        storagePoolType = StorageType.UNKNOWN;
        autoRecoverable = true;
        name = "";
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String value) {
        comment = value;
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

    public Integer getCriticalSpaceActionBlocker() {
        return criticalSpaceActionBlocker;
    }

    public void setCriticalSpaceActionBlocker(Integer criticalSpaceActionBlocker) {
        this.criticalSpaceActionBlocker = criticalSpaceActionBlocker;
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
                storagePoolType,
                storageType,
                description,
                sanState,
                wipeAfterDelete,
                warningLowSpaceIndicator,
                criticalSpaceActionBlocker
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
                && storagePoolType == other.storagePoolType
                && storageType == other.storageType
                && sanState == other.sanState
                && Objects.equals(wipeAfterDelete, other.wipeAfterDelete)
                && Objects.equals(description, other.description)
                && Objects.equals(warningLowSpaceIndicator, other.warningLowSpaceIndicator)
                && Objects.equals(criticalSpaceActionBlocker, other.criticalSpaceActionBlocker);
    }

    @Override
    public String toString() {
        return ToStringBuilder.forInstance(this)
                .append("name", getName())
                .append("id", getId())
                .build();
    }
}

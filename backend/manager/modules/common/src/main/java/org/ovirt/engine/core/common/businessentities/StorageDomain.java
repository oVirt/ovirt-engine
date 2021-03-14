package org.ovirt.engine.core.common.businessentities;

import java.util.Objects;
import java.util.Set;

import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.utils.SizeConverter;
import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.compat.Guid;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class StorageDomain implements Queryable, BusinessEntityWithStatus<Guid, StorageDomainStatus>, Nameable, Commented, Managed {
    private static final long serialVersionUID = 1043048758366348870L;

    private Boolean supportsDiscard;

    public StorageDomain() {
        staticData = new StorageDomainStatic();
        dynamicData = new StorageDomainDynamic();
        setStoragePoolIsoMapData(new StoragePoolIsoMap());
        storageDomainSharedStatus = StorageDomainSharedStatus.Unattached;
        hostedEngineStorage = false;
    }

    private Set<EngineError> alerts;

    /**
     * @return the alerts
     */
    public Set<EngineError> getAlerts() {
        return alerts;
    }

    /**
     * @param alerts the alerts to set
     */
    public void setAlerts(Set<EngineError> alerts) {
        this.alerts = alerts;
    }

    private StoragePoolIsoMap storagePoolIsoMapData;

    public StoragePoolIsoMap getStoragePoolIsoMapData() {
        return storagePoolIsoMapData;
    }

    public void setStoragePoolIsoMapData(StoragePoolIsoMap storagePoolIsoMap) {
        storagePoolIsoMapData = storagePoolIsoMap;
    }

    private StorageDomainStatic staticData;

    public StorageDomainStatic getStorageStaticData() {
        return staticData;
    }

    public void setStorageStaticData(StorageDomainStatic staticData) {
        this.staticData = staticData;
    }

    private StorageDomainDynamic dynamicData;

    public StorageDomainDynamic getStorageDynamicData() {
        return dynamicData;
    }

    public void setStorageDynamicData(StorageDomainDynamic dynamicData) {
        this.dynamicData = dynamicData;
    }

    @Override
    public Guid getId() {
        return getStorageStaticData() == null ? null : getStorageStaticData().getId();
    }

    @Override
    public void setId(Guid id) {
        getStorageStaticData().setId(id);
        getStorageDynamicData().setId(id);
        getStoragePoolIsoMapData().setStorageId(id);
    }

    public String getStorage() {
        return getStorageStaticData().getStorage();
    }

    public void setStorage(String storage) {
        getStorageStaticData().setStorage(storage);
    }

    /**
     * Returns the first device of the domain metadata LV for block domains.
     * Currently its required that the metadata first extent will be 0, therefore
     * modifications of that device should be inspected carefully.
     */
    public String getFirstMetadataDevice() {
        return getStorageStaticData().getFirstMetadataDevice();
    }

    public void setFirstMetadataDevice(String firstMetadataDevice) {
        getStorageStaticData().setFirstMetadataDevice(firstMetadataDevice);
    }

    public String getVgMetadataDevice() {
        return getStorageStaticData().getVgMetadataDevice();
    }

    public void setVgMetadataDevice(String vgMetadataDevice) {
        getStorageStaticData().setVgMetadataDevice(vgMetadataDevice);
    }

    public boolean isBackup() {
        return getStorageStaticData().isBackup();
    }

    public void setBackup(boolean backup) {
        getStorageStaticData().setBackup(backup);
    }

    public StorageBlockSize getBlockSize() {
        return getStorageStaticData().getBlockSize();
    }

    public void setBlockSize(StorageBlockSize blockSize) {
        getStorageStaticData().setBlockSize(blockSize);
    }

    @JsonIgnore
    @Override
    public String getName() {
        return getStorageName();
    }

    public String getStorageName() {
        return getStorageStaticData().getStorageName();
    }

    public void setStorageName(String storageName) {
        getStorageStaticData().setStorageName(storageName);
    }

    public String getDescription() {
        return getStorageStaticData().getDescription();
    }

    public void setDescription(String description) {
        getStorageStaticData().setDescription(description);
    }

    @Override
    public String getComment() {
        return getStorageStaticData().getComment();
    }

    @Override
    public void setComment(String value) {
        getStorageStaticData().setComment(value);
    }

    public Guid getStoragePoolId() {
        return getStoragePoolIsoMapData().getStoragePoolId();
    }

    public void setStoragePoolId(Guid storagePoolId) {
        getStoragePoolIsoMapData().setStoragePoolId(storagePoolId);
    }

    public Integer getAvailableDiskSize() {
        return getStorageDynamicData().getAvailableDiskSize();
    }

    @JsonIgnore
    public Long getAvailableDiskSizeInBytes() {
        Integer availableSize = getAvailableDiskSize();
        return availableSize != null ? availableSize * SizeConverter.BYTES_IN_GB : null;
    }

    public Integer getWarningLowSpaceIndicator() {
        return staticData == null ? null : staticData.getWarningLowSpaceIndicator();
    }

    public void setWarningLowSpaceIndicator(Integer warningLowSpaceIndicator) {
        staticData.setWarningLowSpaceIndicator(warningLowSpaceIndicator);
    }

    public Integer getWarningLowConfirmedSpaceIndicator() {
        return staticData == null ? null : staticData.getWarningLowConfirmedSpaceIndicator();
    }

    public void setWarningLowConfirmedSpaceIndicator(Integer warningLowConfirmedSpaceIndicator) {
        staticData.setWarningLowConfirmedSpaceIndicator(warningLowConfirmedSpaceIndicator);
    }

    public int getWarningLowSpaceSize() {
        return getWarningLowSpaceIndicator() == null || getTotalDiskSize() == null ?
                0 : getTotalDiskSize() * getWarningLowSpaceIndicator() / 100;
    }

    public Integer getCriticalSpaceActionBlocker() {
        return staticData == null ? null : staticData.getCriticalSpaceActionBlocker();
    }

    public void setCriticalSpaceActionBlocker(Integer criticalSpaceActionBlocker) {
        staticData.setCriticalSpaceActionBlocker(criticalSpaceActionBlocker);
    }

    public void setAvailableDiskSize(Integer availableDiskSize) {
        getStorageDynamicData().setAvailableDiskSize(availableDiskSize);
        updateTotalDiskSize();
        updateOverCommitPercent();
    }

    public Integer getConfirmedAvailableDiskSize() {
        return getStorageDynamicData().getConfirmedAvailableDiskSize();
    }

    public void setConfirmedAvailableDiskSize(Integer confirmedAvailableDiskSize) {
        getStorageDynamicData().setConfirmedAvailableDiskSize(confirmedAvailableDiskSize);
    }

    public Integer getVdoSavings() {
        return getStorageDynamicData().getVdoSavings();
    }

    public void setVdoSavings(Integer vdoSavings) {
        getStorageDynamicData().setVdoSavings(vdoSavings);
    }

    private void updateOverCommitPercent() {
        if (getAvailableDiskSize() == null || getAvailableDiskSize() == 0) {
            setStorageDomainOverCommitPercent(0);
        } else {
            setStorageDomainOverCommitPercent((getCommittedDiskSize() - getActualImagesSize()) * 100
                    / getAvailableDiskSize());
        }
    }

    private int storageDomainOverCommitPercent;

    public int getStorageDomainOverCommitPercent() {
        return storageDomainOverCommitPercent;
    }

    public void setStorageDomainOverCommitPercent(int storageDomainOverCommitPercent) {
        this.storageDomainOverCommitPercent = storageDomainOverCommitPercent;
    }

    private int committedDiskSize;

    public int getCommittedDiskSize() {
        return committedDiskSize;
    }

    public void setCommittedDiskSize(int committedDiskSize) {
        this.committedDiskSize = committedDiskSize;
        updateOverCommitPercent();
    }

    private int actualImagesSize;

    public int getActualImagesSize() {
        return actualImagesSize;
    }

    public void setActualImagesSize(int actualImagesSize) {
        this.actualImagesSize = actualImagesSize;
        updateOverCommitPercent();
    }

    public Integer getUsedDiskSize() {
        return getStorageDynamicData().getUsedDiskSize();
    }

    public void setUsedDiskSize(Integer usedDiskSize) {
        getStorageDynamicData().setUsedDiskSize(usedDiskSize);
        updateTotalDiskSize();
    }

    private void updateTotalDiskSize() {
        Integer available = getStorageDynamicData() == null ? null : getStorageDynamicData().getAvailableDiskSize();
        Integer used = getStorageDynamicData() == null ? null : getStorageDynamicData().getUsedDiskSize();

        if (available != null && used != null) {
            setTotalDiskSize(available + used);
        } else {
            setTotalDiskSize(null);
        }
    }

    private Integer totalDiskSize;

    public Integer getTotalDiskSize() {
        updateTotalDiskSize();
        return totalDiskSize;
    }

    public void setTotalDiskSize(Integer value) {
        totalDiskSize = value;
    }

    @Override
    public StorageDomainStatus getStatus() {
        return getStoragePoolIsoMapData().getStatus();
    }

    @Override
    public void setStatus(StorageDomainStatus status) {
        getStoragePoolIsoMapData().setStatus(status);
    }

    public ExternalStatus getExternalStatus() {
        return dynamicData.getExternalStatus();
    }

    public void setExternalStatus(ExternalStatus externalStatus) {
        dynamicData.setExternalStatus(externalStatus);
    }

    private String storagePoolName;

    public String getStoragePoolName() {
        return storagePoolName;
    }

    public void setStoragePoolName(String storagePoolName) {
        this.storagePoolName = storagePoolName;
    }

    public StorageType getStorageType() {
        return getStorageStaticData().getStorageType();
    }

    public void setStorageType(StorageType storageType) {
        getStorageStaticData().setStorageType(storageType);
    }

    public boolean isLocal() {
        return getStorageType().isLocal();
    }

    public boolean isShared() {
        return getStorageType().isShared();
    }

    private StorageDomainSharedStatus storageDomainSharedStatus;

    public StorageDomainSharedStatus getStorageDomainSharedStatus() {
        return storageDomainSharedStatus;
    }

    public void setStorageDomainSharedStatus(StorageDomainSharedStatus storageDomainSharedStatus) {
        this.storageDomainSharedStatus = storageDomainSharedStatus;
    }

    public StorageDomainType getStorageDomainType() {
        return getStorageStaticData().getStorageDomainType();
    }

    public void setStorageDomainType(StorageDomainType storageDomainType) {
        getStorageStaticData().setStorageDomainType(storageDomainType);
    }

    public StorageFormatType getStorageFormat() {
        return getStorageStaticData().getStorageFormat();
    }

    public void setStorageFormat(StorageFormatType storageFormatType) {
        getStorageStaticData().setStorageFormat(storageFormatType);
    }

    @Override
    public Object getQueryableId() {
        return getId();
    }

    public boolean isAutoRecoverable() {
        return staticData.isAutoRecoverable();
    }

    public void setAutoRecoverable(boolean autoRecoverable) {
        staticData.setAutoRecoverable(autoRecoverable);
    }

    public boolean isContainsUnregisteredEntities() {
        return dynamicData.isContainsUnregisteredEntities();
    }

    public void setContainsUnregisteredEntities(boolean containsUnregisteredEntities) {
        dynamicData.setContainsUnregisteredEntities(containsUnregisteredEntities);
    }

    public long getLastTimeUsedAsMaster() {
        return staticData.getLastTimeUsedAsMaster();
    }

    public void setLastTimeUsedAsMaster(long lastTimeUsedAsMaster) {
        staticData.setLastTimeUsedAsMaster(lastTimeUsedAsMaster);
    }

    public Boolean getWipeAfterDelete() {
        return staticData.getWipeAfterDelete();
    }

    public void setWipeAfterDelete(Boolean wipeAfterDelete) {
        staticData.setWipeAfterDelete(wipeAfterDelete);
    }

    public Boolean getDiscardAfterDelete() {
        return staticData.getDiscardAfterDelete();
    }

    public void setDiscardAfterDelete(boolean discardAfterDelete) {
        staticData.setDiscardAfterDelete(discardAfterDelete);
    }

    public Boolean getSupportsDiscard() {
        return supportsDiscard;
    }

    public void setSupportsDiscard(Boolean supportsDiscard) {
        this.supportsDiscard = supportsDiscard;
    }

    private boolean hostedEngineStorage;

    public boolean isHostedEngineStorage() {
        return hostedEngineStorage;
    }

    public void setHostedEngineStorage(boolean hostedEngineStorage) {
        this.hostedEngineStorage = hostedEngineStorage;
    }

    @Override
    public boolean isManaged() {
        return getStorageDomainType() != StorageDomainType.Unmanaged;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                getId(),
                committedDiskSize,
                dynamicData,
                staticData,
                storageDomainSharedStatus,
                storageDomainOverCommitPercent,
                storagePoolIsoMapData,
                totalDiskSize,
                supportsDiscard,
                hostedEngineStorage
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof StorageDomain)) {
            return false;
        }
        StorageDomain other = (StorageDomain) obj;
        return Objects.equals(getId(), other.getId())
                && committedDiskSize == other.committedDiskSize
                && storageDomainSharedStatus == other.storageDomainSharedStatus
                && storageDomainOverCommitPercent == other.storageDomainOverCommitPercent
                && Objects.equals(totalDiskSize, other.totalDiskSize)
                && Objects.equals(supportsDiscard, other.supportsDiscard)
                && hostedEngineStorage == other.hostedEngineStorage;
    }

    @Override
    public String toString() {
        // Since the static data arrives from external source it's not guarenteed not to be null so a null check is
        // mandatory in order to avoid NPE when invoking toString by the logger
        return ToStringBuilder.forInstance(this)
                .append("domainName", staticData == null ? null : staticData.getName())
                .append("domainId", staticData == null ? null : staticData.getId())
                .build();
    }
}

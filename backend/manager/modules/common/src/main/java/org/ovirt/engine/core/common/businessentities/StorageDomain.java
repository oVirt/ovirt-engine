package org.ovirt.engine.core.common.businessentities;

import java.util.Set;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.common.utils.ObjectUtils;
import org.ovirt.engine.core.common.utils.SizeConverter;
import org.ovirt.engine.core.compat.Guid;

public class StorageDomain extends IVdcQueryable implements BusinessEntityWithStatus<Guid, StorageDomainStatus>, Nameable, Commented {
    private static final long serialVersionUID = -6162192446628804305L;

    public StorageDomain() {
        staticData = new StorageDomainStatic();
        dynamicData = new StorageDomainDynamic();
        setStoragePoolIsoMapData(new StoragePoolIsoMap());
        storageDomainSharedStatus = StorageDomainSharedStatus.Unattached;
    }

    //this member is in use only by the Frontend project
    private String vdcQueryableId;

    private Set<VdcBllErrors> alerts;

    /**
     * @return the alerts
     */
    public Set<VdcBllErrors> getAlerts() {
        return alerts;
    }

    /**
     * @param alerts the alerts to set
     */
    public void setAlerts(Set<VdcBllErrors> alerts) {
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
        return this.getStorageStaticData().getId();
    }

    @Override
    public void setId(Guid id) {
        getStorageStaticData().setId(id);
        getStorageDynamicData().setId(id);
        getStoragePoolIsoMapData().setstorage_id(id);
    }

    public String getStorage() {
        return getStorageStaticData().getStorage();
    }

    public void setStorage(String storage) {
        getStorageStaticData().setStorage(storage);
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
        return getStoragePoolIsoMapData().getstorage_pool_id();
    }

    public void setStoragePoolId(Guid storagePoolId) {
        getStoragePoolIsoMapData().setstorage_pool_id(storagePoolId);
    }

    public Integer getAvailableDiskSize() {
        return getStorageDynamicData().getAvailableDiskSize();
    }

    @JsonIgnore
    public Long getAvailableDiskSizeInBytes() {
        Integer availableSize = getAvailableDiskSize();
        return availableSize != null ? availableSize * SizeConverter.BYTES_IN_GB : null;
    }

    public void setAvailableDiskSize(Integer availableDiskSize) {
        getStorageDynamicData().setAvailableDiskSize(availableDiskSize);
        UpdateTotalDiskSize();
        updateOverCommitPercent();
    }

    private void updateOverCommitPercent() {
        if (getAvailableDiskSize() == null || getAvailableDiskSize() == 0) {
            setStorageDomainOverCommitPercent(0);
        }
        else {
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
        UpdateTotalDiskSize();
    }

    private void UpdateTotalDiskSize() {
        Integer available = getStorageDynamicData().getAvailableDiskSize();
        Integer used = getStorageDynamicData().getUsedDiskSize();

        if (available != null && used != null) {
            setTotalDiskSize(available + used);
        } else {
            setTotalDiskSize(null);
        }
    }

    private Integer totalDiskSize;

    public Integer getTotalDiskSize() {
        UpdateTotalDiskSize();
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
        if(vdcQueryableId == null){
            return getId();
        }
        //used only by the Frontend project
        return vdcQueryableId;
    }

    // this setter is in use only by Frontend project
    public void setQueryableId(String value) {
        this.vdcQueryableId = value;
    }

    public boolean isAutoRecoverable() {
        return staticData.isAutoRecoverable();
    }

    public void setAutoRecoverable(boolean autoRecoverable) {
        staticData.setAutoRecoverable(autoRecoverable);
    }

    public boolean isContainsUnregisteredEntities() {
        return staticData.isContainsUnregisteredEntities();
    }

    public void setContainsUnregisteredEntities(boolean containsUnregisteredEntities) {
        staticData.setContainsUnregisteredEntities(containsUnregisteredEntities);
    }

    public long getLastTimeUsedAsMaster() {
        return staticData.getLastTimeUsedAsMaster();
    }

    public void setLastTimeUsedAsMaster(long lastTimeUsedAsMaster) {
        staticData.setLastTimeUsedAsMaster(lastTimeUsedAsMaster);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + committedDiskSize;
        result = prime * result + ((dynamicData == null) ? 0 : dynamicData.hashCode());
        result = prime * result + ((staticData == null) ? 0 : staticData.hashCode());
        result = prime * result + ((storageDomainSharedStatus == null) ? 0 : storageDomainSharedStatus.hashCode());
        result = prime * result + storageDomainOverCommitPercent;
        result = prime * result + ((storagePoolIsoMapData == null) ? 0 : storagePoolIsoMapData.hashCode());
        result = prime * result + ((totalDiskSize == null) ? 0 : totalDiskSize.hashCode());
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
        StorageDomain other = (StorageDomain) obj;
        return (ObjectUtils.objectsEqual(getId(), other.getId())
                && committedDiskSize == other.committedDiskSize
                && storageDomainSharedStatus == other.storageDomainSharedStatus
                && storageDomainOverCommitPercent == other.storageDomainOverCommitPercent
                && ObjectUtils.objectsEqual(totalDiskSize, other.totalDiskSize));
    }

    @Override
    public String toString() {
        // Since the static data arrives from external source it's not guarenteed not to be null so a null check is
        // mandatory in order to avoid NPE when invoking toString by the logger
        String domainName = staticData == null ? "null" : staticData.getName();
        Guid domainId = staticData == null ? null : staticData.getId();
        return "StorageDomain[" + domainName + ", " + domainId + "]";
    }
}

package org.ovirt.engine.core.common.businessentities;

import java.util.Set;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.common.utils.ObjectUtils;
import org.ovirt.engine.core.compat.Guid;

public class StorageDomain extends IVdcQueryable implements BusinessEntity<Guid>, Nameable, Commented {
    private static final long serialVersionUID = -6162192446628804305L;

    public StorageDomain() {
        staticData = new StorageDomainStatic();
        dynamicData = new StorageDomainDynamic();
        setStoragePoolIsoMapData(new StoragePoolIsoMap());
        totalDiskSize = 0;
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

    public String getComment() {
        return getStorageStaticData().getComment();
    }

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

    public void setAvailableDiskSize(Integer availableDiskSize) {
        getStorageDynamicData().setAvailableDiskSize(availableDiskSize);
        UpdateTotalDiskSize();
        updateOverCommitPercent();
    }

    private void updateOverCommitPercent() {
        setStorageDomainOverCommitPercent(getAvailableDiskSize() != null && getAvailableDiskSize() > 0 ? getCommittedDiskSize()
                * 100 / getAvailableDiskSize()
                : 0);
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
            setTotalDiskSize(0); // GREGM prevents NPEs
        }
    }

    private Integer totalDiskSize;

    public Integer getTotalDiskSize() {
        UpdateTotalDiskSize();
        return totalDiskSize;
    }

    public void setTotalDiskSize(Integer value) {
        value = (value == null) ? Integer.valueOf(0) : value;
        if (!totalDiskSize.equals(value)) {
            totalDiskSize = value;
        }
    }

    public StorageDomainStatus getStatus() {
        return getStoragePoolIsoMapData().getstatus();
    }

    public void setStatus(StorageDomainStatus status) {
        getStoragePoolIsoMapData().setstatus(status);
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
}

package org.ovirt.engine.core.common.businessentities;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;

import org.ovirt.engine.core.common.utils.ObjectUtils;
import org.ovirt.engine.core.common.validation.annotation.ValidName;
import org.ovirt.engine.core.common.validation.group.CreateEntity;
import org.ovirt.engine.core.common.validation.group.UpdateEntity;
import org.ovirt.engine.core.compat.Guid;

/**
 * Quota business entity which reflects the <code>Quota</code> limitations for storage pool. <BR/>
 * The limitation are separated to two different types
 * <ul>
 * <li>General Limitation - Indicates the general limitation of the quota cross all the storage pool</li>
 * <li>Specific Limitation - Indicates specific limitation of the quota for specific storage or vds group</li>
 * </ul>
 * <BR/>
 * Quota entity encapsulate the specific limitations of the storage pool with lists, general limitations are configured
 * in the field members.<BR/>
 * <BR/>
 * Take in notice there can not be general limitation and specific limitation on the same resource type.
 */
public class Quota extends IVdcQueryable implements BusinessEntity<Guid>, Nameable {

    /**
     * Automatic generated serial version ID.
     */
    private static final long serialVersionUID = 6637198348072059199L;

    /**
     * The quota id.
     */
    private Guid id;

    /**
     * The storage pool id the quota is enforced on.
     */
    private Guid storagePoolId;

    /**
     * The storage pool name the quota is enforced on, for GUI use.
     */
    private String storagePoolName;

    /**
     * The quota name.
     */
    @Size(min = 1, max = BusinessEntitiesDefinitions.QUOTA_NAME_SIZE)
    @ValidName(message = "VALIDATION.QUOTA.NAME.INVALID", groups = { CreateEntity.class, UpdateEntity.class })
    private String quotaName;

    /**
     * The quota description.
     */
    @Size(min = 1, max = BusinessEntitiesDefinitions.QUOTA_DESCRIPTION_SIZE)
    private String description;

    /**
     * The threshold of vds group in percentages.
     */
    @Min(0)
    @Max(100)
    private int thresholdVdsGroupPercentage;

    /**
     * The threshold of storage in percentages.
     */
    @Min(0)
    @Max(100)
    private int thresholdStoragePercentage;

    /**
     * The grace of vds group in percentages.
     */
    @Min(0)
    @Max(100)
    private int graceVdsGroupPercentage;

    /**
     * The grace of storage in percentages.
     */
    @Min(0)
    @Max(100)
    private int graceStoragePercentage;

    /**
     * The global quota vds group limit.
     */
    private QuotaVdsGroup globalQuotaVdsGroup;

    /**
     * The global quota storage limit.
     */
    private QuotaStorage globalQuotaStorage;

    /**
     * The quota enforcement type.
     */
    private QuotaEnforcementTypeEnum quotaEnforcementType;

    /**
     * List of all the specific VdsGroups limitations.
     */
    private List<QuotaVdsGroup> quotaVdsGroupList;

    /**
     * List of all the specific storage limitations.
     */
    private List<QuotaStorage> quotaStorageList;

    /**
     * Default constructor of Quota, which initialize empty lists for specific limitations, and no user assigned.
     */
    public Quota() {
        setQuotaStorages(new ArrayList<QuotaStorage>());
        setQuotaVdsGroups(new ArrayList<QuotaVdsGroup>());
        id = Guid.Empty;
    }

    /**
     * @return the quota id.
     */
    public Guid getId() {
        return id;
    }

    /**
     * @param id
     *            the quota Id to set.
     */
    public void setId(Guid id) {
        this.id = id;
    }

    /**
     * @return the thresholdVdsGroupPercentage
     */
    public int getThresholdVdsGroupPercentage() {
        return thresholdVdsGroupPercentage;
    }

    /**
     * @param thresholdVdsGroupPercentage
     *            the thresholdVdsGroupPercentage to set
     */
    public void setThresholdVdsGroupPercentage(int thresholdVdsGroupPercentage) {
        this.thresholdVdsGroupPercentage = thresholdVdsGroupPercentage;
    }

    /**
     * @return the thresholdStoragePercentage
     */
    public int getThresholdStoragePercentage() {
        return thresholdStoragePercentage;
    }

    /**
     * @param thresholdStoragePercentage
     *            the thresholdStoragePercentage to set
     */
    public void setThresholdStoragePercentage(int thresholdStoragePercentage) {
        this.thresholdStoragePercentage = thresholdStoragePercentage;
    }

    /**
     * @return the graceVdsGroupPercentage
     */
    public int getGraceVdsGroupPercentage() {
        return graceVdsGroupPercentage;
    }

    /**
     * @param graceVdsGroupPercentage
     *            the graceVdsGroupPercentage to set
     */
    public void setGraceVdsGroupPercentage(int graceVdsGroupPercentage) {
        this.graceVdsGroupPercentage = graceVdsGroupPercentage;
    }

    /**
     * @return the graceStoragePercentage
     */
    public int getGraceStoragePercentage() {
        return graceStoragePercentage;
    }

    /**
     * @param graceStoragePercentage
     *            the graceStoragePercentage to set
     */
    public void setGraceStoragePercentage(int graceStoragePercentage) {
        this.graceStoragePercentage = graceStoragePercentage;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description
     *            the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return the quotaName
     */
    public String getQuotaName() {
        return quotaName;
    }

    @Override
    public String getName() {
        return getQuotaName();
    }

    /**
     * @param quotaName
     *            the quotaName to set
     */
    public void setQuotaName(String quotaName) {
        this.quotaName = quotaName;
    }

    /**
     * @return the storagePoolId
     */
    public Guid getStoragePoolId() {
        return storagePoolId;
    }

    /**
     * @param storagePoolId
     *            the storagePoolId to set
     */
    public void setStoragePoolId(Guid storagePoolId) {
        this.storagePoolId = storagePoolId;
    }

    /**
     * @return the storagePoolName
     */
    public String getStoragePoolName() {
        return storagePoolName;
    }

    /**
     * @param storagePoolName
     *            the storagePoolName to set
     */
    public void setStoragePoolName(String storagePoolName) {
        this.storagePoolName = storagePoolName;
    }

    /**
     * @return the quotaStorageList
     */
    public List<QuotaStorage> getQuotaStorages() {
        return quotaStorageList;
    }

    /**
     * @param quotaStorages
     *            the quotaStorages to set
     */
    public void setQuotaStorages(List<QuotaStorage> quotaStorages) {
        this.quotaStorageList = quotaStorages;
    }

    /**
     * @return the quotaVdsGroups
     */
    public List<QuotaVdsGroup> getQuotaVdsGroups() {
        return quotaVdsGroupList;
    }

    /**
     * @param quotaVdsGroups
     *            the quotaVdsGroups to set
     */
    public void setQuotaVdsGroups(List<QuotaVdsGroup> quotaVdsGroups) {
        this.quotaVdsGroupList = quotaVdsGroups;
    }

    /**
     * @return the quotaEnforcementType
     */
    public QuotaEnforcementTypeEnum getQuotaEnforcementType() {
        return this.quotaEnforcementType;
    }

    /**
     * @param quotaEnforcementType
     *            the quotaEnforcementType to set
     */
    public void setQuotaEnforcementType(QuotaEnforcementTypeEnum quotaEnforcementType) {
        this.quotaEnforcementType = quotaEnforcementType;
    }

    @Override
    public Object getQueryableId() {
        return getId();
    }

    /**
     * @return If this there is a global storage limitation in the quota, returns true.
     */
    public boolean isGlobalStorageQuota() {
        return globalQuotaStorage != null;
    }

    /**
     * @return If this there is a global vds group limitation in the quota, returns true.
     */
    public boolean isGlobalVdsGroupQuota() {
        return globalQuotaVdsGroup != null;
    }

    /**
     * @return If the storage quota is empty, returns true.
     */
    public boolean isEmptyStorageQuota() {
        return globalQuotaStorage == null && (getQuotaStorages() == null || getQuotaStorages().isEmpty());
    }

    /**
     * @return If the vdsGroup quota is empty, returns true.
     */
    public boolean isEmptyVdsGroupQuota() {
        return globalQuotaVdsGroup == null && (getQuotaVdsGroups() == null || getQuotaVdsGroups().isEmpty());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((description == null) ? 0 : description.hashCode());
        result = prime * result + ((globalQuotaStorage == null) ? 0 : globalQuotaStorage.hashCode());
        result = prime * result + ((globalQuotaVdsGroup == null) ? 0 : globalQuotaVdsGroup.hashCode());
        result = prime * result + graceStoragePercentage;
        result = prime * result + graceVdsGroupPercentage;
        result = prime * result + ((quotaName == null) ? 0 : quotaName.hashCode());
        result = prime * result + ((quotaStorageList == null) ? 0 : quotaStorageList.hashCode());
        result = prime * result + ((quotaVdsGroupList == null) ? 0 : quotaVdsGroupList.hashCode());
        result = prime * result + ((storagePoolId == null) ? 0 : storagePoolId.hashCode());
        result = prime * result + thresholdStoragePercentage;
        result = prime * result + thresholdVdsGroupPercentage;
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
        Quota other = (Quota) obj;
        return (ObjectUtils.objectsEqual(id, other.id)
                && ObjectUtils.objectsEqual(description, other.description)
                && ObjectUtils.objectsEqual(globalQuotaStorage, other.globalQuotaStorage)
                && ObjectUtils.objectsEqual(globalQuotaVdsGroup, other.globalQuotaVdsGroup)
                && graceStoragePercentage == other.graceStoragePercentage
                && graceVdsGroupPercentage == other.graceVdsGroupPercentage
                && ObjectUtils.objectsEqual(quotaName, other.quotaName)
                && ObjectUtils.objectsEqual(quotaStorageList, other.quotaStorageList)
                && ObjectUtils.objectsEqual(quotaVdsGroupList, other.quotaVdsGroupList)
                && ObjectUtils.objectsEqual(storagePoolId, other.storagePoolId)
                && thresholdStoragePercentage == other.thresholdStoragePercentage
                && thresholdVdsGroupPercentage == other.thresholdVdsGroupPercentage);
    }

    public QuotaVdsGroup getGlobalQuotaVdsGroup() {
        return globalQuotaVdsGroup;
    }

    public void setGlobalQuotaVdsGroup(QuotaVdsGroup globalQuotaVdsGroup) {
        this.globalQuotaVdsGroup = globalQuotaVdsGroup;
    }

    public QuotaStorage getGlobalQuotaStorage() {
        return globalQuotaStorage;
    }

    public void setGlobalQuotaStorage(QuotaStorage globalQuotaStorage) {
        this.globalQuotaStorage = globalQuotaStorage;
    }
}

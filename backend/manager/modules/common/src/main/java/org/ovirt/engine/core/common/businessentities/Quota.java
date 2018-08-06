package org.ovirt.engine.core.common.businessentities;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

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
public class Quota implements Queryable, BusinessEntity<Guid>, Nameable {

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
    @NotNull
    private Guid storagePoolId;

    /**
     * The storage pool name the quota is enforced on, for GUI use.
     */
    private String storagePoolName;

    /**
     * Flag if this quota is default for storage pool
     */
    private boolean isDefault;

    /**
     * The quota name.
     */
    @Size(min = 1, max = BusinessEntitiesDefinitions.QUOTA_NAME_SIZE)
    @ValidName(message = "VALIDATION_QUOTA_NAME_INVALID", groups = { CreateEntity.class, UpdateEntity.class })
    private String quotaName;

    /**
     * The quota description.
     */
    @Size(min = 0, max = BusinessEntitiesDefinitions.QUOTA_DESCRIPTION_SIZE)
    @NotNull
    private String description;

    /**
     * The threshold of vds group in percentages.
     */
    @Min(0)
    @Max(100)
    private int thresholdClusterPercentage;

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
    private int graceClusterPercentage;

    /**
     * The grace of storage in percentages.
     */
    @Min(0)
    @Max(100)
    private int graceStoragePercentage;

    /**
     * The global quota vds group limit.
     */
    private QuotaCluster globalQuotaCluster;

    /**
     * The global quota storage limit.
     */
    private QuotaStorage globalQuotaStorage;

    /**
     * The quota enforcement type.
     */
    private QuotaEnforcementTypeEnum quotaEnforcementType;

    /**
     * List of all the specific Cluster limitations.
     */
    private List<QuotaCluster> quotaClusterList;

    /**
     * List of all the specific storage limitations.
     */
    private List<QuotaStorage> quotaStorageList;

    /**
     * Default constructor of Quota, which initialize empty lists for specific limitations, and no user assigned.
     */
    public Quota() {
        setQuotaStorages(new ArrayList<QuotaStorage>());
        setQuotaClusters(new ArrayList<QuotaCluster>());
        id = Guid.Empty;
        description = "";
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
     * @return the thresholdClusterPercentage
     */
    public int getThresholdClusterPercentage() {
        return thresholdClusterPercentage;
    }

    /**
     * @param thresholdClusterPercentage
     *            the thresholdClusterPercentage to set
     */
    public void setThresholdClusterPercentage(int thresholdClusterPercentage) {
        this.thresholdClusterPercentage = thresholdClusterPercentage;
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
     * @return the graceClusterPercentage
     */
    public int getGraceClusterPercentage() {
        return graceClusterPercentage;
    }

    /**
     * @param graceClusterPercentage
     *            the graceClusterPercentage to set
     */
    public void setGraceClusterPercentage(int graceClusterPercentage) {
        this.graceClusterPercentage = graceClusterPercentage;
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
        this.description = description == null ? "" : description;
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

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean value) {
        this.isDefault = value;
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
     * @return the quotaClusters
     */
    public List<QuotaCluster> getQuotaClusters() {
        return quotaClusterList;
    }

    /**
     * @param quotaClusters
     *            the quotaClusters to set
     */
    public void setQuotaClusters(List<QuotaCluster> quotaClusters) {
        this.quotaClusterList = quotaClusters;
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
    public boolean isGlobalClusterQuota() {
        return globalQuotaCluster != null;
    }

    /**
     * @return If the storage quota is empty, returns true.
     */
    public boolean isEmptyStorageQuota() {
        return globalQuotaStorage == null && (getQuotaStorages() == null || getQuotaStorages().isEmpty());
    }

    /**
     * @return If the cluster quota is empty, returns true.
     */
    public boolean isEmptyClusterQuota() {
        return globalQuotaCluster == null && (getQuotaClusters() == null || getQuotaClusters().isEmpty());
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                id,
                description,
                globalQuotaStorage,
                globalQuotaCluster,
                graceStoragePercentage,
                graceClusterPercentage,
                quotaName,
                quotaStorageList,
                quotaClusterList,
                storagePoolId,
                thresholdStoragePercentage,
                thresholdClusterPercentage
        );
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Quota)) {
            return false;
        }
        Quota other = (Quota) obj;
        return Objects.equals(id, other.id)
                && Objects.equals(description, other.description)
                && Objects.equals(globalQuotaStorage, other.globalQuotaStorage)
                && Objects.equals(globalQuotaCluster, other.globalQuotaCluster)
                && graceStoragePercentage == other.graceStoragePercentage
                && graceClusterPercentage == other.graceClusterPercentage
                && Objects.equals(quotaName, other.quotaName)
                && Objects.equals(quotaStorageList, other.quotaStorageList)
                && Objects.equals(quotaClusterList, other.quotaClusterList)
                && Objects.equals(storagePoolId, other.storagePoolId)
                && thresholdStoragePercentage == other.thresholdStoragePercentage
                && thresholdClusterPercentage == other.thresholdClusterPercentage;
    }

    public QuotaCluster getGlobalQuotaCluster() {
        return globalQuotaCluster;
    }

    public void setGlobalQuotaCluster(QuotaCluster globalQuotaCluster) {
        this.globalQuotaCluster = globalQuotaCluster;
    }

    public QuotaStorage getGlobalQuotaStorage() {
        return globalQuotaStorage;
    }

    public void setGlobalQuotaStorage(QuotaStorage globalQuotaStorage) {
        this.globalQuotaStorage = globalQuotaStorage;
    }
}

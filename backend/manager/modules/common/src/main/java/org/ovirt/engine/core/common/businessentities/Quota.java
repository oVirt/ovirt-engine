package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;

import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.INotifyPropertyChanged;

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
public class Quota extends IVdcQueryable implements INotifyPropertyChanged, Serializable, QuotaVdsGroupProperties, QuotaStorageProperties {

    /**
     * Automatic generated serial version ID.
     */
    private static final long serialVersionUID = 6637198348072059199L;

    /**
     * The quota id.
     */
    private Guid id = new Guid();

    /**
     * The storage pool id the quota is enforced on.
     */
    private Guid storagePoolId;

    /**
     * The storage pool name the quota is enforced on, for GUI use.
     */
    private transient String storagePoolName;

    /**
     * The quota name.
     */
    @Size(min = 1, max = BusinessEntitiesDefinitions.QUOTA_NAME_SIZE)
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
     * The global storage limit in Giga bytes.
     */
    @Min(-1)
    private Long storageSizeGB;

    /**
     * The global storage usage in Giga bytes for Quota.
     */
    private transient Double storageSizeGBUsage;

    /**
     * The global virtual CPU limitations.
     */
    @Min(-1)
    private Integer virtualCpu;

    /**
     * The global virtual CPU usage for Quota.
     */
    private transient Integer virtualCpuUsage;

    /**
     * The global virtual memory limitations for Quota.
     */
    @Min(-1)
    private Long memSizeMB;

    /**
     * The global virtual memory usage for Quota.
     */
    private transient Long memSizeMBUsage;

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
     * @return the memSizeMBUsage
     */
    public Long getMemSizeMBUsage() {
        return memSizeMBUsage;
    }

    /**
     * @param memSizeMBUsage
     *            the memSizeMBUsage to set
     */
    public void setMemSizeMBUsage(Long memSizeMBUsage) {
        this.memSizeMBUsage = memSizeMBUsage;
    }

    /**
     * @return the memSizeMB
     */
    public Long getMemSizeMB() {
        return memSizeMB;
    }

    /**
     * @param memSizeMB
     *            the memSizeMB to set
     */
    public void setMemSizeMB(Long memSizeMB) {
        this.memSizeMB = memSizeMB;
    }

    /**
     * @return the virtualCpuUsage
     */
    public Integer getVirtualCpuUsage() {
        return virtualCpuUsage;
    }

    /**
     * @param virtualCpuUsage
     *            the virtualCpuUsage to set
     */
    public void setVirtualCpuUsage(Integer virtualCpuUsage) {
        this.virtualCpuUsage = virtualCpuUsage;
    }

    /**
     * @return the virtualCpu
     */
    public Integer getVirtualCpu() {
        return virtualCpu;
    }

    /**
     * @param virtualCpu
     *            the virtualCpu to set
     */
    public void setVirtualCpu(Integer virtualCpu) {
        this.virtualCpu = virtualCpu;
    }

    /**
     * @return the storageSizeGBUsage
     */
    public Double getStorageSizeGBUsage() {
        return storageSizeGBUsage;
    }

    /**
     * @param storageSizeGBUsage
     *            the storageSizeGBUsage to set
     */
    public void setStorageSizeGBUsage(Double storageSizeGBUsage) {
        this.storageSizeGBUsage = storageSizeGBUsage;
    }

    /**
     * @return the storageSizeGB
     */
    public Long getStorageSizeGB() {
        return storageSizeGB;
    }

    /**
     * @param storageSizeGB
     *            the storageSizeGB to set
     */
    public void setStorageSizeGB(Long storageSizeGB) {
        this.storageSizeGB = storageSizeGB;
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((description == null) ? 0 : description.hashCode());
        result = prime * result + graceStoragePercentage;
        result = prime * result + graceVdsGroupPercentage;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((quotaName == null) ? 0 : quotaName.hashCode());
        result = prime * result + ((quotaStorageList == null) ? 0 : quotaStorageList.hashCode());
        result = prime * result + ((quotaVdsGroupList == null) ? 0 : quotaVdsGroupList.hashCode());
        result = prime * result + ((storageSizeGB == null) ? 0 : storageSizeGB.hashCode());
        result = prime * result + ((storagePoolId == null) ? 0 : storagePoolId.hashCode());
        result = prime * result + thresholdStoragePercentage;
        result = prime * result + thresholdVdsGroupPercentage;
        result = prime * result + ((virtualCpu == null) ? 0 : virtualCpu.hashCode());
        result = prime * result + ((memSizeMB == null) ? 0 : memSizeMB.hashCode());
        return result;
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Quota other = (Quota) obj;
        if (description == null) {
            if (other.description != null)
                return false;
        } else if (!description.equals(other.description))
            return false;
        if (graceStoragePercentage != other.graceStoragePercentage)
            return false;
        if (graceVdsGroupPercentage != other.graceVdsGroupPercentage)
            return false;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (quotaName == null) {
            if (other.quotaName != null)
                return false;
        } else if (!quotaName.equals(other.quotaName))
            return false;
        if (quotaStorageList == null) {
            if (other.quotaStorageList != null)
                return false;
        } else if (!quotaStorageList.equals(other.quotaStorageList))
            return false;
        if (quotaVdsGroupList == null) {
            if (other.quotaVdsGroupList != null)
                return false;
        } else if (!quotaVdsGroupList.equals(other.quotaVdsGroupList))
            return false;
        if (storageSizeGB == null) {
            if (other.storageSizeGB != null)
                return false;
        } else if (!storageSizeGB.equals(other.storageSizeGB))
            return false;
        if (storagePoolId == null) {
            if (other.storagePoolId != null)
                return false;
        } else if (!storagePoolId.equals(other.storagePoolId))
            return false;
        if (thresholdStoragePercentage != other.thresholdStoragePercentage)
            return false;
        if (thresholdVdsGroupPercentage != other.thresholdVdsGroupPercentage)
            return false;
        if (virtualCpu == null) {
            if (other.virtualCpu != null)
                return false;
        } else if (!virtualCpu.equals(other.virtualCpu))
            return false;
        if (memSizeMB == null) {
            if (other.memSizeMB != null)
                return false;
        } else if (!memSizeMB.equals(other.memSizeMB))
            return false;
        return true;
    }
}

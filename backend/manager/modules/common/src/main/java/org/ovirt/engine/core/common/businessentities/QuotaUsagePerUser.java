package org.ovirt.engine.core.common.businessentities;
import org.ovirt.engine.core.compat.Guid;

import java.io.Serializable;

public class QuotaUsagePerUser extends IVdcQueryable implements Serializable {

    private Guid quotaId;
    private String quotaName;
    private double storageLimit;
    private double storageTotalUsage;
    private double storageUsageForUserGB = 0;
    private int vcpuLimit;
    private int vcpuTotalUsage;
    private int vcpuUsageForUser = 0;
    private long memoryLimit;
    private long memoryTotalUsage;
    private long memoryUsageForUser = 0;

    public QuotaUsagePerUser(){

    }

    public QuotaUsagePerUser(Guid quotaId, String quotaName, double storageLimit, double storageTotalUsage, int vcpuLimit, int vcpuTotalUsage, long memoryLimit, long memoryTotalUsage) {
        this.quotaId = quotaId;
        this.quotaName = quotaName;
        this.storageLimit = storageLimit;
        this.storageTotalUsage = storageTotalUsage;
        this.vcpuLimit = vcpuLimit;
        this.vcpuTotalUsage = vcpuTotalUsage;
        this.memoryLimit = memoryLimit;
        this.memoryTotalUsage = memoryTotalUsage;
    }


    public double getStorageUsageForUser() {
        return storageUsageForUserGB;
    }

    public void setStorageUsageForUser(double storageUsageForUserGB) {
        this.storageUsageForUserGB = storageUsageForUserGB;
    }

    public int getVcpuUsageForUser() {
        return vcpuUsageForUser;
    }

    public void setVcpuUsageForUser(int vcpuUsageForUser) {
        this.vcpuUsageForUser = vcpuUsageForUser;
    }

    public long getMemoryUsageForUser() {
        return memoryUsageForUser;
    }

    public void setMemoryUsageForUser(long memoryUsageForUser) {
        this.memoryUsageForUser = memoryUsageForUser;
    }

    public Guid getQuotaId() {
        return quotaId;
    }

    public double getStorageLimit() {
        return storageLimit;
    }

    public double getStorageTotalUsage() {
        return storageTotalUsage;
    }

    public int getVcpuLimit() {
        return vcpuLimit;
    }

    public int getVcpuTotalUsage() {
        return vcpuTotalUsage;
    }

    public long getMemoryLimit() {
        return memoryLimit;
    }

    public long getMemoryTotalUsage() {
        return memoryTotalUsage;
    }

    public double getUserStorageUsagePercentage() {
        return storageUsageForUserGB*100/storageLimit;
    }

    public double getUserVcpuUsagePercentage() {
        return (double)vcpuUsageForUser*100/vcpuLimit;
    }

    public double getUserMemoryUsagePercentage() {
        return (double)memoryUsageForUser*100/memoryLimit;
    }

    public double getOthersStorageUsagePercentage() {
        return (storageTotalUsage-storageUsageForUserGB)*100/storageLimit;
    }

    public double getOthersVcpuUsagePercentage() {
        return (double)(vcpuTotalUsage-vcpuUsageForUser)*100/vcpuLimit;
    }

    public double getOthersMemoryUsagePercentage() {
        return (double)(memoryTotalUsage-memoryUsageForUser)*100/memoryLimit;
    }

    public int getFreeVcpu() {
        return vcpuLimit-vcpuTotalUsage;
    }

    public long getFreeMemory() {
        return memoryLimit-memoryTotalUsage;
    }

    public double getFreeStorage() {
        return storageLimit-storageTotalUsage;
    }

    @Override
    public Object getQueryableId() {
        return getQuotaId();
    }

    public String getQuotaName() {
        return quotaName;
    }

    public void setStorageLimit(double storageLimit) {
        this.storageLimit = storageLimit;
    }

    public void setStorageTotalUsage(double storageTotalUsage) {
        this.storageTotalUsage = storageTotalUsage;
    }

    public void setVcpuLimit(int vcpuLimit) {
        this.vcpuLimit = vcpuLimit;
    }

    public void setVcpuTotalUsage(int vcpuTotalUsage) {
        this.vcpuTotalUsage = vcpuTotalUsage;
    }

    public void setMemoryLimit(long memoryLimit) {
        this.memoryLimit = memoryLimit;
    }

    public void setMemoryTotalUsage(long memoryTotalUsage) {
        this.memoryTotalUsage = memoryTotalUsage;
    }

    public boolean isUnlimitedVcpu() {
        return vcpuLimit == QuotaVdsGroup.UNLIMITED_VCPU;
    }

    public boolean isUnlimitedMemory() {
        return memoryLimit == QuotaVdsGroup.UNLIMITED_MEM;
    }

    public boolean isUnlimitedStorage() {
        return storageLimit == QuotaStorage.UNLIMITED;
    }
}

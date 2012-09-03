package org.ovirt.engine.core.bll.quota;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.QuotaEnforcementTypeEnum;
import org.ovirt.engine.core.common.businessentities.QuotaStorage;
import org.ovirt.engine.core.common.businessentities.QuotaVdsGroup;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.dao.QuotaDAO;
import org.ovirt.engine.core.utils.Pair;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;

public class QuotaManager {
    private final static QuotaManager INSTANCE = new QuotaManager();
    public final static Long UNLIMITED = -1L;
    private final static ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final static Log log = LogFactory.getLog(QuotaManager.class);
    private final static DecimalFormat percentageFormatter = new DecimalFormat("#.##");
    private final ConcurrentHashMap<Guid, Map<Guid, Quota>> storagePoolQuotaMap =
            new ConcurrentHashMap<Guid, Map<Guid, Quota>>();

    public static QuotaManager getInstance() {
        return INSTANCE;
    }

    private static QuotaDAO getQuotaDAO() {
        return DbFacade.getInstance()
                .getQuotaDAO();
    }

    private AuditLogableBase getLoggableQuotaStorageParams(String quotaName,
            double storageUsagePercentage,
            double storageRequestedPercentage) {
        AuditLogableBase logable = new AuditLogableBase();
        logable.AddCustomValue("QuotaName", quotaName);
        logable.AddCustomValue("CurrentStorage", percentageFormatter.format(storageUsagePercentage));
        logable.AddCustomValue("Requested", percentageFormatter.format(storageRequestedPercentage));

        return logable;
    }

    private AuditLogableBase getLogableQuotaVdsGroupParams(String quotaName,
            double cpuCurrentPercentage,
            double cpuRequestPercentage,
            double memCurrentPercentage,
            double memRequestPercentage,
            boolean cpuOverLimit,
            boolean memOverLimit) {

        AuditLogableBase logable = new AuditLogableBase();
        logable.AddCustomValue("QuotaName", quotaName);

        StringBuilder currentUtilization = new StringBuilder();
        if (cpuOverLimit) {
            currentUtilization.append("vcpu:").append(percentageFormatter.format(cpuCurrentPercentage)).append("% ");
        }
        if (memOverLimit) {
            currentUtilization.append("mem:").append(percentageFormatter.format(memCurrentPercentage)).append("%");
        }

        StringBuilder request = new StringBuilder();
        if (cpuOverLimit) {
            request.append("vcpu:").append(percentageFormatter.format(cpuRequestPercentage)).append("% ");
        }
        if (memOverLimit) {
            request.append("mem:").append(percentageFormatter.format(memRequestPercentage)).append("%");
        }

        logable.AddCustomValue("Utilization", currentUtilization.toString());
        logable.AddCustomValue("Requested", request.toString());

        return logable;
    }

    public boolean validateAndSetStorageQuota(storage_pool storagePool,
            List<StorageQuotaValidationParameter> parameters,
            ArrayList<String> canDoActionMessages) {
        lock.readLock().lock();
        try {
            return validateAndSetStorageQuotaHelper(storagePool, parameters, canDoActionMessages, true);
        } finally {
            lock.readLock().unlock();
        }
    }

    public void rollbackQuota(storage_pool storagePool, List<Guid> quotaList) {
        rollbackQuota(storagePool.getId(), quotaList);
    }

    private void rollbackQuota(Guid storagePoolId, List<Guid> quotaList) {
        lock.readLock().lock();
        try {
            if (!storagePoolQuotaMap.containsKey(storagePoolId)) {
                return;
            }
            synchronized (storagePoolQuotaMap.get(storagePoolId)) {
                Map<Guid, Quota> map = storagePoolQuotaMap.get(storagePoolId);
                for (Guid quotaId : quotaList) {
                    map.remove(quotaId);
                }
            }
        } finally {
            lock.readLock().unlock();
        }
    }

    public void decreaseStorageQuota(storage_pool storagePool,
            List<StorageQuotaValidationParameter> parameters) {
        lock.readLock().lock();
        try {
            if (!storagePoolQuotaMap.containsKey(storagePool.getId())) {
                return;
            }
            synchronized (storagePoolQuotaMap.get(storagePool.getId())) {
                if (!validateAndSetStorageQuotaHelper(storagePool, parameters, new ArrayList<String>(), false)) {
                    log.errorFormat("Couldn't rollback old quota (when decreasing storage pool {0} quota storage size in cache), removing from cache",
                            storagePool.getId());
                    for (StorageQuotaValidationParameter parameter : parameters) {
                        storagePoolQuotaMap.get(storagePool.getId()).remove(parameter.getQuotaId());
                    }
                }
            }
        } finally {
            lock.readLock().unlock();
        }
    }

    private boolean validateAndSetStorageQuotaHelper(storage_pool storagePool,
            List<StorageQuotaValidationParameter> parameters,
            ArrayList<String> canDoActionMessages, boolean isIncrease) {
        Pair<AuditLogType, AuditLogableBase> logPair = new Pair<AuditLogType, AuditLogableBase>();
        lock.readLock().lock();
        try {
            if (QuotaEnforcementTypeEnum.DISABLED.equals(storagePool.getQuotaEnforcementType())) {
                return true;
            }
            for (StorageQuotaValidationParameter param : parameters) {
                if (param.getQuotaId() == null || param.getStorageDomainId() == null) {
                    canDoActionMessages.add(VdcBllMessages.ACTION_TYPE_FAILED_QUOTA_IS_NOT_VALID.toString());
                    return false;
                }
            }

            if (!storagePoolQuotaMap.containsKey(storagePool.getId())) {
                storagePoolQuotaMap.putIfAbsent(storagePool.getId(), new HashMap<Guid, Quota>());
            }

            synchronized (storagePoolQuotaMap.get(storagePool.getId())) {
                if (storagePoolQuotaMap.get(storagePool.getId()) == null) {
                    return false;
                }
                Map<Guid, Quota> quotaMap = storagePoolQuotaMap.get(storagePool.getId());
                Map<Guid, Map<Guid, Double>> desiredStorageSizeQuotaMap = new HashMap<Guid, Map<Guid, Double>>();

                Map<Guid, Double> newUsedGlobalStorageSize = new HashMap<Guid, Double>();
                Map<Guid, Map<Guid, Double>> newUsedSpecificStorageSize = new HashMap<Guid, Map<Guid, Double>>();

                for (StorageQuotaValidationParameter param : parameters) {
                    Quota quota;
                    if (!quotaMap.containsKey(param.getQuotaId())) {
                        quota = getQuotaDAO().getById(param.getQuotaId());
                        if (quota == null) {
                            canDoActionMessages.add(VdcBllMessages.ACTION_TYPE_FAILED_QUOTA_IS_NOT_VALID.toString());
                            return false;
                        }
                        quotaMap.put(quota.getId(), quota);
                    } else {
                        quota = quotaMap.get(param.getQuotaId());
                    }
                    if (!desiredStorageSizeQuotaMap.containsKey(quota.getId())) {
                        desiredStorageSizeQuotaMap.put(quota.getId(), new HashMap<Guid, Double>());
                    }
                    Map<Guid, Double> quotaStorageMap = desiredStorageSizeQuotaMap.get(quota.getId());
                    if (!quotaStorageMap.containsKey(param.getStorageDomainId())) {
                        quotaStorageMap.put(param.getStorageDomainId(), 0.0);
                    }
                    quotaStorageMap.put(param.getStorageDomainId(), quotaStorageMap.get(param.getStorageDomainId())
                            + param.getSize());

                }

                for (Guid quotaId : desiredStorageSizeQuotaMap.keySet()) {
                    Quota quota = quotaMap.get(quotaId);
                    if (quota.getGlobalQuotaStorage() != null) { // global storage quota
                        if (quota.getGlobalQuotaStorage().getStorageSizeGB() != UNLIMITED) {
                            double sum = 0.0;
                            for (Double size : desiredStorageSizeQuotaMap.get(quotaId).values()) {
                                sum += size;
                            }

                            double storageUsagePercentage = quota.getGlobalQuotaStorage().getStorageSizeGBUsage()
                                    / quota.getGlobalQuotaStorage().getStorageSizeGB() * 100;
                            double storageRequestPercentage = sum
                                    / quota.getGlobalQuotaStorage().getStorageSizeGB() * 100;

                            if (!checkQuotaStorageLimits(storagePool.getQuotaEnforcementType(),
                                    quota,
                                    quota.getGlobalQuotaStorage().getStorageSizeGB(),
                                    storageUsagePercentage, storageRequestPercentage,
                                    canDoActionMessages,
                                    logPair)) {
                                return false;
                            }
                            newUsedGlobalStorageSize.put(quotaId, sum
                                    + quota.getGlobalQuotaStorage().getStorageSizeGBUsage());
                        }
                    } else {
                        newUsedSpecificStorageSize.put(quotaId, new HashMap<Guid, Double>());
                        for (Guid storageId : desiredStorageSizeQuotaMap.get(quotaId).keySet()) {
                            boolean hasStorageId = false;
                            for (QuotaStorage quotaStorage : quota.getQuotaStorages()) {
                                if (quotaStorage.getStorageId().equals(storageId)) {
                                    hasStorageId = true;
                                    if (quotaStorage.getStorageSizeGB() != UNLIMITED) {
                                        double storageUsagePercentage = quotaStorage.getStorageSizeGBUsage()
                                                / quotaStorage.getStorageSizeGB() * 100;
                                        double storageRequestPercentage =
                                                desiredStorageSizeQuotaMap.get(quotaId)
                                                        .get(storageId)
                                                        / quotaStorage.getStorageSizeGB() * 100;

                                        if (!checkQuotaStorageLimits(storagePool.getQuotaEnforcementType(),
                                                quota,
                                                quotaStorage.getStorageSizeGB(),
                                                storageUsagePercentage, storageRequestPercentage,
                                                canDoActionMessages,
                                                logPair)) {
                                            return false;
                                        }
                                        newUsedSpecificStorageSize.get(quotaId).put(storageId,
                                                quotaStorage.getStorageSizeGBUsage()
                                                        + desiredStorageSizeQuotaMap.get(quotaId).get(storageId));
                                        break;
                                    }
                                }
                            }
                            if (!hasStorageId) {
                                canDoActionMessages.add(VdcBllMessages.ACTION_TYPE_FAILED_NO_QUOTA_SET_FOR_DOMAIN.toString());
                                return false;
                            }
                        }
                    }
                }
                // cache new storage size.
                for (Guid quotaId : newUsedGlobalStorageSize.keySet()) {
                    Quota quota = quotaMap.get(quotaId);
                    double value = newUsedGlobalStorageSize.get(quotaId);
                    if (value < 0) {
                        log.errorFormat("Quota id {0} cached storage size is negative, removing from cache", quotaId);
                        quotaMap.remove(quotaId);
                        continue;
                    }
                    quota.getGlobalQuotaStorage().setStorageSizeGBUsage(value);
                }
                for (Guid quotaId : newUsedSpecificStorageSize.keySet()) {
                    Quota quota = quotaMap.get(quotaId);
                    for (QuotaStorage quotaStorage : quota.getQuotaStorages()) {
                        if (newUsedSpecificStorageSize.get(quotaId).containsKey(quotaStorage.getStorageId())) {
                            double value = newUsedSpecificStorageSize.get(quotaId)
                                    .get(quotaStorage.getStorageId());
                            if (value < 0) {
                                log.errorFormat("Quota id {0} cached storage size is negative, removing from cache",
                                        quotaId);
                                quotaMap.remove(quotaId);
                                continue;
                            }
                            quotaStorage.setStorageSizeGBUsage(value);
                        }
                    }
                }
            }
            return true;
        } finally {
            lock.readLock().unlock();
            if (logPair.getFirst() != null) {
                AuditLogDirector.log(logPair.getSecond(), logPair.getFirst());
            }
        }
    }

    private boolean checkQuotaStorageLimits(QuotaEnforcementTypeEnum quotaEnforcementTypeEnum,
            Quota quota,
            double limit,
            double storageUsagePercentage,
            double storageRequestPercentage,
            ArrayList<String> canDoActionMessages,
            Pair<AuditLogType, AuditLogableBase> log) {
        double storageTotalPercentage = storageUsagePercentage + storageRequestPercentage;

        if (limit == UNLIMITED || storageTotalPercentage <= quota.getThresholdStoragePercentage()) {
            return true;
        } else if (storageTotalPercentage <= 100) {
            log.setFirst(AuditLogType.USER_EXCEEDED_QUOTA_STORAGE_THRESHOLD);
            log.setSecond(getLoggableQuotaStorageParams(quota.getQuotaName(),
                    storageUsagePercentage + storageRequestPercentage,
                    storageRequestPercentage));
        } else if (storageTotalPercentage <= quota.getGraceStoragePercentage() + 100) {
            log.setFirst(AuditLogType.USER_EXCEEDED_QUOTA_STORAGE_LIMIT);
            log.setSecond(getLoggableQuotaStorageParams(quota.getQuotaName(),
                    storageUsagePercentage + storageRequestPercentage,
                    storageRequestPercentage));
        } else {
            log.setFirst(AuditLogType.USER_EXCEEDED_QUOTA_STORAGE_GRACE_LIMIT);
            log.setSecond(getLoggableQuotaStorageParams(quota.getQuotaName(),
                    storageUsagePercentage,
                    storageRequestPercentage));
            if (QuotaEnforcementTypeEnum.HARD_ENFORCEMENT.equals(quotaEnforcementTypeEnum)) {
                canDoActionMessages.add(VdcBllMessages.ACTION_TYPE_FAILED_QUOTA_STORAGE_LIMIT_EXCEEDED.toString());
                return false;
            }
        }
        return true;
    }

    private boolean checkQuotaClusterLimits(QuotaEnforcementTypeEnum quotaEnforcementTypeEnum,
            Quota quota,
            QuotaVdsGroup quotaVdsGroup,
            long memToAdd,
            int vcpuToAdd,
            ArrayList<String> canDoActionMessages,
            Pair<AuditLogType, AuditLogableBase> log) {
        if (quotaVdsGroup.getVirtualCpu() == 0 || quotaVdsGroup.getMemSizeMB() == 0) {
            return false;
        }

        double vcpuToAddPercentage = (double) vcpuToAdd / (double) quotaVdsGroup.getVirtualCpu() * 100;
        double vcpuCurrentPercentage =
                (double) quotaVdsGroup.getVirtualCpuUsage() / (double) quotaVdsGroup.getVirtualCpu() * 100;
        double newVcpuPercent = vcpuToAddPercentage + vcpuCurrentPercentage;
        double memToAddPercentage = (double) memToAdd / (double) quotaVdsGroup.getMemSizeMB() * 100;
        double memCurrentPercentage =
                (double) quotaVdsGroup.getMemSizeMBUsage() / (double) quotaVdsGroup.getMemSizeMB() * 100;
        double newMemoryPercent = memToAddPercentage + memCurrentPercentage;
        long newMemory = memToAdd + quotaVdsGroup.getMemSizeMBUsage();
        int newVcpu = vcpuToAdd + quotaVdsGroup.getVirtualCpuUsage();

        long memLimit = quotaVdsGroup.getMemSizeMB();
        int cpuLimit = quotaVdsGroup.getVirtualCpu();

        if (memLimit == UNLIMITED && cpuLimit == UNLIMITED) { // if both cpu and mem are unlimited
            // cache
            cacheNewValues(quotaVdsGroup, newMemory, newVcpu);
            return true;
        } else if (newVcpuPercent <= quota.getThresholdVdsGroupPercentage() // if cpu and mem usages are under the limit
                && newMemoryPercent <= quota.getThresholdVdsGroupPercentage()) {
            // cache
            cacheNewValues(quotaVdsGroup, newMemory, newVcpu);
            return true;
        } else if (newVcpuPercent <= 100
                && newMemoryPercent <= 100) { // passed the threshold (not the quota limit)
            log.setFirst(AuditLogType.USER_EXCEEDED_QUOTA_VDS_GROUP_THRESHOLD);
            log.setSecond(getLogableQuotaVdsGroupParams(quota.getQuotaName(),
                    vcpuCurrentPercentage + vcpuToAddPercentage,
                    vcpuToAddPercentage,
                    memCurrentPercentage + memToAddPercentage,
                    memToAddPercentage,
                    newVcpuPercent > quota.getThresholdVdsGroupPercentage(),
                    newMemoryPercent > quota.getThresholdVdsGroupPercentage()));
        } else if (newVcpuPercent <= quota.getGraceVdsGroupPercentage() + 100
                && newMemoryPercent <= quota.getGraceVdsGroupPercentage() + 100) { // passed the quota limit (not the
            // grace)
            log.setFirst(AuditLogType.USER_EXCEEDED_QUOTA_VDS_GROUP_LIMIT);
            log.setSecond(getLogableQuotaVdsGroupParams(quota.getQuotaName(),
                    vcpuCurrentPercentage + vcpuToAddPercentage,
                    vcpuToAddPercentage,
                    memCurrentPercentage + memToAddPercentage,
                    memToAddPercentage,
                    newVcpuPercent > 100,
                    newMemoryPercent > 100));
        } else {
            log.setFirst(AuditLogType.USER_EXCEEDED_QUOTA_VDS_GROUP_GRACE_LIMIT); // passed the grace
            log.setSecond(getLogableQuotaVdsGroupParams(quota.getQuotaName(),
                    vcpuCurrentPercentage,
                    vcpuToAddPercentage,
                    memCurrentPercentage,
                    memToAddPercentage,
                    newVcpuPercent > quota.getGraceVdsGroupPercentage() + 100,
                    newMemoryPercent > quota.getGraceVdsGroupPercentage() + 100));
            if (QuotaEnforcementTypeEnum.HARD_ENFORCEMENT.equals(quotaEnforcementTypeEnum)) {
                canDoActionMessages.add(VdcBllMessages.ACTION_TYPE_FAILED_QUOTA_VDS_GROUP_LIMIT_EXCEEDED.toString());
                return false;
            }
        }
        // cache
        cacheNewValues(quotaVdsGroup, newMemory, newVcpu);
        return true;
    }

    private void cacheNewValues(QuotaVdsGroup quotaVdsGroup, long newMemory, int newVcpu) {
        quotaVdsGroup.setVirtualCpuUsage(newVcpu);
        quotaVdsGroup.setMemSizeMBUsage(newMemory);
    }

    public boolean validateQuotaForStoragePool(storage_pool storagePool,
            Guid vdsGroupId,
            Guid quotaId,
            ArrayList<String> canDoActionMessages) {
        lock.readLock().lock();
        try {
            if (QuotaEnforcementTypeEnum.DISABLED.equals(storagePool.getQuotaEnforcementType())) {
                return true;
            }

            if (vdsGroupId == null || vdsGroupId.equals(Guid.Empty) || quotaId == null) {
                canDoActionMessages.add(VdcBllMessages.ACTION_TYPE_FAILED_QUOTA_IS_NOT_VALID.toString());
                return false;
            }

            if (!storagePoolQuotaMap.containsKey(storagePool.getId())) {
                storagePoolQuotaMap.putIfAbsent(storagePool.getId(), new HashMap<Guid, Quota>());
            }

            synchronized (storagePoolQuotaMap.get(storagePool.getId())) {
                Map<Guid, Quota> quotaMap = storagePoolQuotaMap.get(storagePool.getId());
                if (quotaMap == null) {
                    return false;
                }

                Quota quota = null;
                if (!quotaMap.containsKey(quotaId)) {
                    quota = getQuotaDAO().getById(quotaId);
                    if (quota == null) {
                        canDoActionMessages.add(VdcBllMessages.ACTION_TYPE_FAILED_QUOTA_IS_NO_LONGER_AVAILABLE_IN_SYSTEM.toString());
                        log.errorFormat("The quota id {0} is not found in backend and DB.", quotaId.toString());
                        return false;
                    }
                    quotaMap.put(quota.getId(), quota);
                } else {
                    quota = quotaMap.get(quotaId);
                }
                if (quota.getGlobalQuotaVdsGroup() != null) { // global cluster quota
                    return true;
                } else {
                    boolean hasVdsGroup = false;
                    for (QuotaVdsGroup vdsGroup : quota.getQuotaVdsGroups()) {
                        if (vdsGroup.getVdsGroupId().equals(vdsGroupId)) {
                            hasVdsGroup = true;
                            break;
                        }
                    }
                    if (hasVdsGroup) {
                        return true;
                    }
                }
            }

            canDoActionMessages.add(VdcBllMessages.ACTION_TYPE_FAILED_QUOTA_IS_NOT_VALID.toString());
            return false;
        } finally {
            lock.readLock().unlock();
        }

    }

    public boolean validateAndSetClusterQuota(storage_pool storagePool,
            Guid vdsGroupId,
            Guid quotaId,
            int vcpu,
            long mem,
            ArrayList<String> canDoActionMessages) {
        Pair<AuditLogType, AuditLogableBase> logPair = new Pair<AuditLogType, AuditLogableBase>();
        try {
            if (QuotaEnforcementTypeEnum.DISABLED.equals(storagePool.getQuotaEnforcementType())) {
                return true;
            }
            if (!storagePoolQuotaMap.containsKey(storagePool.getId())) {
                storagePoolQuotaMap.putIfAbsent(storagePool.getId(), new HashMap<Guid, Quota>());
            }

            synchronized (storagePoolQuotaMap.get(storagePool.getId())) {
                if (!validateQuotaForStoragePool(storagePool, vdsGroupId, quotaId, canDoActionMessages)) {
                    return false;
                }
                Quota quota = storagePoolQuotaMap.get(storagePool.getId()).get(quotaId);
                if (quota == null) {
                    canDoActionMessages.add(VdcBllMessages.ACTION_TYPE_FAILED_QUOTA_IS_NOT_VALID.toString());
                    return false;
                }
                QuotaVdsGroup quotaVdsGroup = null;
                if (quota.getGlobalQuotaVdsGroup() != null) { // global cluster quota
                    quotaVdsGroup = quota.getGlobalQuotaVdsGroup();
                } else {
                    for (QuotaVdsGroup vdsGroup : quota.getQuotaVdsGroups()) {
                        if (vdsGroup.getVdsGroupId().equals(vdsGroupId)) {
                            quotaVdsGroup = vdsGroup;
                            break;
                        }
                    }
                }
                if (quotaVdsGroup == null) {
                    canDoActionMessages.add(VdcBllMessages.ACTION_TYPE_FAILED_QUOTA_IS_NOT_VALID.toString());
                    return false;
                }

                boolean success = checkQuotaClusterLimits(storagePool.getQuotaEnforcementType(),
                        quota,
                        quotaVdsGroup,
                        mem,
                        vcpu,
                        canDoActionMessages,
                        logPair);
                if (!success) {
                    return false;
                }
            }
            return true;
        } finally {
            if (logPair.getFirst() != null) {
                AuditLogDirector.log(logPair.getSecond(), logPair.getFirst());
            }
        }

    }

    public void removeQuotaFromCache(Guid storagePoolId, Guid quotaId) {
        lock.readLock().lock();
        try {
            if (!storagePoolQuotaMap.containsKey(storagePoolId)) {
                return;
            }
            synchronized (storagePoolQuotaMap.get(storagePoolId)) {
                storagePoolQuotaMap.get(storagePoolId).remove(quotaId);
            }
        } finally {
            lock.readLock().unlock();
        }
    }

    public void removeStoragePoolFromCache(Guid storagePoolId) {
        lock.writeLock().lock();
        try {
            storagePoolQuotaMap.remove(storagePoolId);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public List<Guid> getQuotaListFromParameters(List<StorageQuotaValidationParameter> storageQuotaListParameters) {
        List<Guid> list = new ArrayList<Guid>();
        for (StorageQuotaValidationParameter param : storageQuotaListParameters) {
            list.add(param.getQuotaId());
        }
        return list;
    }

    public void rollbackQuota(Guid vmId) {
        VM vm = DbFacade.getInstance().getVmDAO().get(vmId);
        if (vm != null) {
            rollbackQuota(vm.getstorage_pool_id(), Arrays.asList(vm.getQuotaId()));
        }
    }
}

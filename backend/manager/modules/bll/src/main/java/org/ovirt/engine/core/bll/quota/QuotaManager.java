package org.ovirt.engine.core.bll.quota;

import java.util.ArrayList;
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
    private final ConcurrentHashMap<Guid, Map<Guid, Quota>> storagePoolQuotaMap =
            new ConcurrentHashMap<Guid, Map<Guid, Quota>>();

    public static QuotaManager getInstance() {
        return INSTANCE;
    }

    private static QuotaDAO getQuotaDAO() {
        return DbFacade.getInstance()
                .getQuotaDAO();
    }

    private AuditLogableBase getLoggableQuotaStorageParams(String quotaName, Double storageUsagePercentage) {
        AuditLogableBase logable = new AuditLogableBase();
        logable.AddCustomValue("QuotaName", quotaName);
        logable.AddCustomValue("storageUsage", storageUsagePercentage.toString());
        return logable;
    }

    private AuditLogableBase getLoggableQuotaVdsGroupParams(String quotaName,
            Double value,
            boolean isMemory) {
        AuditLogableBase logable = new AuditLogableBase();
        logable.AddCustomValue("QuotaName", quotaName);
        String str = isMemory ? "memPercentage" : "VCPUPercentage";
        logable.AddCustomValue(str, value.toString());
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
        lock.readLock().lock();
        try {
            if (!storagePoolQuotaMap.containsKey(storagePool.getId())) {
                return;
            }
            synchronized (storagePoolQuotaMap.get(storagePool.getId())) {
                Map<Guid, Quota> map = storagePoolQuotaMap.get(storagePool.getId());
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
                    if (quota.getGlobalQuotaStorage() != null) { //global storage quota
                        if (quota.getGlobalQuotaStorage().getStorageSizeGB() > UNLIMITED) {
                            double sum = 0.0;
                            for (Double size : desiredStorageSizeQuotaMap.get(quotaId).values()) {
                                sum += size;
                            }
                            if (isIncrease) {
                                sum += quota.getGlobalQuotaStorage().getStorageSizeGBUsage();
                            } else {
                                sum = quota.getGlobalQuotaStorage().getStorageSizeGBUsage() - sum;
                            }
                            double storageUsagePercentage = sum
                                    / quota.getGlobalQuotaStorage().getStorageSizeGB() * 100;
                            if (!checkQuotaStorageLimits(storagePool.getQuotaEnforcementType(),
                                    quota,
                                    quota.getGlobalQuotaStorage().getStorageSizeGB(),
                                    storageUsagePercentage,
                                    canDoActionMessages,
                                    logPair)) {
                                return false;
                            }
                            newUsedGlobalStorageSize.put(quotaId, sum);
                        }
                    } else {
                        newUsedSpecificStorageSize.put(quotaId, new HashMap<Guid, Double>());
                        for (Guid storageId : desiredStorageSizeQuotaMap.get(quotaId).keySet()) {
                            boolean hasStorageId = false;
                            for (QuotaStorage quotaStorage : quota.getQuotaStorages()) {
                                if (quotaStorage.getStorageId().equals(storageId)) {
                                    hasStorageId = true;
                                    if (quotaStorage.getStorageSizeGB() > UNLIMITED) {
                                        double sum = 0;
                                        if (isIncrease) {
                                            sum =
                                                    desiredStorageSizeQuotaMap.get(quotaId).get(storageId)
                                                            + quotaStorage.getStorageSizeGBUsage();
                                        } else {
                                            sum =
                                                    quotaStorage.getStorageSizeGBUsage()
                                                            - desiredStorageSizeQuotaMap.get(quotaId)
                                                                    .get(storageId);
                                        }
                                        double storageUsagePercentage = sum
                                                / quotaStorage.getStorageSizeGB() * 100;
                                        if (!checkQuotaStorageLimits(storagePool.getQuotaEnforcementType(),
                                                quota,
                                                quotaStorage.getStorageSizeGB(),
                                                storageUsagePercentage,
                                                canDoActionMessages,
                                                logPair)) {
                                            return false;
                                        }
                                        newUsedSpecificStorageSize.get(quotaId).put(storageId, sum);
                                        break;
                                    }
                                }
                            }
                            if (!hasStorageId) {
                                canDoActionMessages.add(VdcBllMessages.ACTION_TYPE_FAILED_QUOTA_IS_NOT_VALID.toString());
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
            ArrayList<String> canDoActionMessages,
            Pair<AuditLogType, AuditLogableBase> log) {
        if (limit == UNLIMITED || storageUsagePercentage <= quota.getThresholdStoragePercentage()) {
            return true;
        } else if (storageUsagePercentage <= 100) {
            log.setFirst(AuditLogType.USER_EXCEEDED_QUOTA_STORAGE_THRESHOLD);
            log.setSecond(getLoggableQuotaStorageParams(quota.getQuotaName(),
                            storageUsagePercentage));
        } else if (storageUsagePercentage <= quota.getGraceStoragePercentage()) {
            log.setFirst(AuditLogType.USER_EXCEEDED_QUOTA_STORAGE_LIMIT);
            log.setSecond(getLoggableQuotaStorageParams(quota.getQuotaName(),
                            storageUsagePercentage));
        } else {
            log.setFirst(AuditLogType.USER_EXCEEDED_QUOTA_STORAGE_GRACE_LIMIT);
            log.setSecond(getLoggableQuotaStorageParams(quota.getQuotaName(),
                            storageUsagePercentage));
            if (QuotaEnforcementTypeEnum.HARD_ENFORCEMENT.equals(quotaEnforcementTypeEnum)) {
                canDoActionMessages.add(VdcBllMessages.ACTION_TYPE_FAILED_QUOTA_STORAGE_LIMIT_EXCEEDED.toString());
                return false;
            }
        }
        return true;
    }

    private boolean checkQuotaVdsGroupLimits(QuotaEnforcementTypeEnum quotaEnforcementTypeEnum,
            Quota quota,
            double limit,
            double percentage,
            boolean isMemory,
            ArrayList<String> canDoActionMessages,
            Pair<AuditLogType, AuditLogableBase> log) {
        if (limit == UNLIMITED || percentage <= quota.getThresholdVdsGroupPercentage()) {
            return true;
        } else if (percentage <= 100) {
            log.setFirst(AuditLogType.USER_EXCEEDED_QUOTA_VDS_GROUP_THRESHOLD);
            log.setSecond(getLoggableQuotaVdsGroupParams(quota.getQuotaName(),
                    percentage,
                    isMemory));
        } else if (percentage <= quota.getGraceVdsGroupPercentage()) {
            log.setFirst(AuditLogType.USER_EXCEEDED_QUOTA_VDS_GROUP_LIMIT);
            log.setSecond(getLoggableQuotaVdsGroupParams(quota.getQuotaName(),
                    percentage,
                            isMemory));
        } else {
            log.setFirst(AuditLogType.USER_EXCEEDED_QUOTA_VDS_GROUP_GRACE_LIMIT);
            log.setSecond(getLoggableQuotaVdsGroupParams(quota.getQuotaName(),
                    percentage,
                            isMemory));
            if (QuotaEnforcementTypeEnum.HARD_ENFORCEMENT.equals(quotaEnforcementTypeEnum)) {
                canDoActionMessages.add(VdcBllMessages.ACTION_TYPE_FAILED_QUOTA_VDS_GROUP_LIMIT_EXCEEDED.toString());
                return false;
            }
        }

        return true;
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
                        canDoActionMessages.add(VdcBllMessages.ACTION_TYPE_FAILED_QUOTA_IS_NOT_VALID.toString());
                        return false;
                    }
                    quotaMap.put(quota.getId(), quota);
                } else {
                    quota = quotaMap.get(quotaId);
                }
                if (quota.getGlobalQuotaVdsGroup() != null) { //global cluster quota
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
                if (quota.getGlobalQuotaVdsGroup() != null) { //global cluster quota
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

                int newVcpu = vcpu + quotaVdsGroup.getVirtualCpuUsage();
                double newVcpuPercent = (double) newVcpu / (double) quotaVdsGroup.getVirtualCpu() * 100;
                boolean success = checkQuotaVdsGroupLimits(storagePool.getQuotaEnforcementType(),
                        quota,
                        quotaVdsGroup.getVirtualCpu(),
                        newVcpuPercent,
                        false,
                        canDoActionMessages,
                        logPair);
                long newMemory = mem + quotaVdsGroup.getMemSizeMBUsage();
                double newMemoryPercent = (double) newMemory / (double) quotaVdsGroup.getMemSizeMB() * 100;
                success &= checkQuotaVdsGroupLimits(storagePool.getQuotaEnforcementType(),
                        quota,
                        quotaVdsGroup.getVirtualCpu(),
                        newMemoryPercent,
                        true,
                        canDoActionMessages,
                        logPair);
                if (!success) {
                    return false;
                }
                // cache
                quotaVdsGroup.setVirtualCpuUsage(newVcpu);
                quotaVdsGroup.setMemSizeMBUsage(newMemory);
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
}
